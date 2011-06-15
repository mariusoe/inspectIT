package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.communication.DefaultData;

import java.lang.ref.SoftReference;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * The default implementation of the {@link IAgentStorageService} interface. Uses an implementation
 * of the {@link DefaultDataDao} interface to save and retrieve the data objects from the database.
 * 
 * @author Patrice Bouillet
 * 
 */
public class AgentStorageService implements IAgentStorageService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AgentStorageService.class);

	/**
	 * Maximum number of threads working, so we protect against bad configuration.
	 */
	private static final int MAX_THREADS = 5;

	/**
	 * Queue capacity for incoming data.
	 */
	private static final int QUEUE_CAPACITY = 50;

	/**
	 * Amount of milliseconds after which the data is thrown away if queue is full.
	 */
	private static final long DATA_THROW_TIMEOUT_MILLIS = 10;
	/**
	 * The default data DAO.
	 */
	private DefaultDataDao defaultDataDao;

	/**
	 * Queue to store and remove list of data that has to be processed.
	 */
	private ArrayBlockingQueue<SoftReference<List<DefaultData>>> dataObjectsBlockingQueue = new ArrayBlockingQueue<SoftReference<List<DefaultData>>>(QUEUE_CAPACITY);

	/**
	 * Count of thread to process data.
	 */
	private int threadCount;

	/**
	 * Count of dropped data due to high volume of incoming data objects.
	 */
	private int droppedDataCount = 0;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addDataObjects(final List dataObjects) throws RemoteException {
		SoftReference<List<DefaultData>> softReference = new SoftReference<List<DefaultData>>(dataObjects);
		try {
			boolean added = dataObjectsBlockingQueue.offer(softReference, DATA_THROW_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
			if (!added) {
				int droppedSize = dataObjects.size();
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Data dropped on the CMR due to the high volume of incoming data from Agent(s). Dropped data objects count: " + droppedSize);
				}
				droppedDataCount += droppedSize;
			}
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Returns the number of data objects that have been dropped on the CMR, due to the high
	 * incoming load.
	 * 
	 * @return Returns the number of data objects that have been dropped on the CMR, due to the high
	 *         incoming load.
	 */
	public int getDroppedDataCount() {
		return droppedDataCount;
	}

	public void setDefaultDataDao(DefaultDataDao defaultDataDao) {
		this.defaultDataDao = defaultDataDao;
	}

	/**
	 * @param threadCount
	 *            the threadCount to set
	 */
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (threadCount <= 0) {
			threadCount = 1;
		} else if (threadCount > MAX_THREADS) {
			threadCount = MAX_THREADS;
		}

		for (int i = 0; i < threadCount; i++) {
			new ProcessDataThread().start();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Agent Storage Service active...");
		}

	}

	/**
	 * Thread class that is processing the data coming to the Agent service and invoking the
	 * {@link DefaultDataDao}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ProcessDataThread extends Thread {

		/**
		 * Default constructor.
		 */
		public ProcessDataThread() {
			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			while (true) {
				SoftReference<List<DefaultData>> softReference = null;
				try {
					softReference = dataObjectsBlockingQueue.take();
				} catch (InterruptedException e) {
					this.interrupt();
					return;
				}

				List<DefaultData> defaultDataList = softReference.get();
				if (defaultDataList != null) {
					for (DefaultData data : defaultDataList) {
						data.finalizeData();
					}

					long time = 0;
					if (LOGGER.isDebugEnabled()) {
						time = System.nanoTime();
					}

					defaultDataDao.saveAll(defaultDataList);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Data Objects count: " + defaultDataList.size() + " Save duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
					}
				}
			}
		}
	}
}
