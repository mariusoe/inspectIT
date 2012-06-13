package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.spring.logger.Logger;

import java.lang.ref.SoftReference;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The default implementation of the {@link IAgentStorageService} interface. Uses an implementation
 * of the {@link DefaultDataDao} interface to save and retrieve the data objects from the database.
 * 
 * @author Patrice Bouillet
 * 
 */
@Service
public class AgentStorageService implements IAgentStorageService {

	/** The logger of this class. */
	@Logger
	Log log;

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
	@Autowired
	private DefaultDataDao defaultDataDao;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider platformIdentDateSaver;

	/**
	 * Queue to store and remove list of data that has to be processed.
	 */
	private ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>> dataObjectsBlockingQueue = new ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>>(QUEUE_CAPACITY);

	/**
	 * Count of thread to process data.
	 */
	@Value("${cmr.agentStorageServiceThreadCount}")
	private int threadCount;

	/**
	 * Count of dropped data due to high volume of incoming data objects.
	 */
	private int droppedDataCount = 0;

	/**
	 * Default constructor.
	 */
	public AgentStorageService() {
	}

	/**
	 * Constructor that can be used in testing for suppling the queue.
	 * 
	 * @param dataObjectsBlockingQueue
	 *            Queue.
	 */
	AgentStorageService(ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>> dataObjectsBlockingQueue) {
		this.dataObjectsBlockingQueue = dataObjectsBlockingQueue;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addDataObjects(final List<? extends DefaultData> dataObjects) throws RemoteException {
		SoftReference<List<? extends DefaultData>> softReference = new SoftReference<List<? extends DefaultData>>(dataObjects);
		if (!dataObjects.isEmpty()) {
			platformIdentDateSaver.registerDataSent(dataObjects.get(0).getPlatformIdent());
		}
		try {
			boolean added = dataObjectsBlockingQueue.offer(softReference, DATA_THROW_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
			if (!added) {
				int droppedSize = dataObjects.size();
				if (log.isTraceEnabled()) {
					log.trace("Data dropped on the CMR due to the high volume of incoming data from Agent(s). Dropped data objects count: " + droppedSize);
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

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (threadCount <= 0) {
			threadCount = 1;
		} else if (threadCount > MAX_THREADS) {
			threadCount = MAX_THREADS;
		}

		for (int i = 0; i < threadCount; i++) {
			new ProcessDataThread().start();
		}

		if (log.isInfoEnabled()) {
			log.info("|-Agent Storage Service active...");
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
				SoftReference<List<? extends DefaultData>> softReference = null;
				try {
					softReference = dataObjectsBlockingQueue.take();
				} catch (InterruptedException e) {
					this.interrupt();
					return;
				}

				List<? extends DefaultData> defaultDataList = softReference.get();
				if (defaultDataList != null) {
					for (DefaultData data : defaultDataList) {
						data.finalizeData();
					}

					long time = 0;
					if (log.isDebugEnabled()) {
						time = System.nanoTime();
					}

					defaultDataDao.saveAll(defaultDataList);

					if (log.isDebugEnabled()) {
						log.debug("Data Objects count: " + defaultDataList.size() + " Save duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
					}
				}
			}
		}
	}
}
