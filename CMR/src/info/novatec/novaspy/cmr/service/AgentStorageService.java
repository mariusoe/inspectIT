package info.novatec.novaspy.cmr.service;

import info.novatec.novaspy.cmr.dao.DefaultDataDao;
import info.novatec.novaspy.cmr.util.Converter;
import info.novatec.novaspy.communication.DefaultData;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * The default implementation of the {@link IAgentStorageService} interface.
 * Uses an implementation of the {@link DefaultDataDao} interface to save and
 * retrieve the data objects from the database.
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
	 * The default data DAO.
	 */
	private DefaultDataDao defaultDataDao;

	/**
	 * This executor is used to asynchronously save the data from the Agents.
	 */
	private ExecutorService saveExecutor = Executors.newCachedThreadPool();

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void addDataObjects(final List dataObjects) throws RemoteException {
		Runnable runnable = new Runnable() {
			public void run() {
				List<DefaultData> finalizedObjects = new ArrayList<DefaultData>(dataObjects.size());

				for (DefaultData data : (List<DefaultData>) dataObjects) {
					finalizedObjects.add(data.finalizeData());
				}

				long time = 0;
				if (LOGGER.isDebugEnabled()) {
					time = System.nanoTime();
				}

				defaultDataDao.saveAll(finalizedObjects);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Data Objects count: " + dataObjects.size() + " Save duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
				}
			}
		};

		saveExecutor.execute(runnable);
	}

	public void setDefaultDataDao(DefaultDataDao defaultDataDao) {
		this.defaultDataDao = defaultDataDao;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Agent Storage Service active...");
		}
	}

}
