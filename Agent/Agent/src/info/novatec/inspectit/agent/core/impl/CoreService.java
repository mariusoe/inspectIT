package info.novatec.inspectit.agent.core.impl;

import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IObjectStorage;
import info.novatec.inspectit.agent.core.ListListener;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.agent.sensor.platform.IPlatformSensor;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.picocontainer.Startable;

/**
 * Default implementation of the {@link ICoreService} interface.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public class CoreService implements ICoreService, Startable {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CoreService.class.getName());

	/**
	 * The configuration storage. Used to access the platform sensor types.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	private final IConnection connection;

	/**
	 * Already used data objects which can be used directly on the CMR to
	 * persist.
	 */
	private Map sensorDataObjects = new Hashtable();

	/**
	 * Contains object storage instances which will be initialized when sending.
	 */
	private Map objectStorages = new Hashtable();

	/**
	 * Used as second hash table for the measurements when processed before
	 * sending.
	 */
	private Map measurementsProcessing = new Hashtable();

	/**
	 * Used as second hash table for the object storages when processed before
	 * sending.
	 */
	private Map objectStoragesProcessing = new Hashtable();

	/**
	 * Temporary Map to switch the references of the active hash table with the
	 * processed one.
	 */
	private Map temp;

	/**
	 * The registered list listeners.
	 */
	private List listListeners = new ArrayList();

	/**
	 * The available and registered sending strategies.
	 */
	private List sendingStrategies = new ArrayList();

	/**
	 * The selected buffer strategy to store the list of value objects.
	 */
	private IBufferStrategy bufferStrategy;

	/**
	 * The default refresh time.
	 */
	private static final long DEFAULT_REFRESH_TIME = 1000L;

	/**
	 * The refresh time for the platformSensorRefresher thread in ms.
	 */
	private long platformSensorRefreshTime = DEFAULT_REFRESH_TIME;

	/**
	 * The platformSensorRefresher is a thread which updates the platform
	 * informations after the specified platformSensorRefreshTime.
	 */
	private volatile PlatformSensorRefresher platformSensorRefresher;

	/**
	 * The sending thread used to execute the sending process of the measurement
	 * in a separate process.
	 */
	private volatile SendingThread sendingThread;

	/**
	 * The default constructor which needs 4 parameters.
	 * 
	 * @param configurationStorage
	 *            The configuration storage.
	 * @param connection
	 *            The connection.
	 * @param bufferStrategy
	 *            The used buffer strategy.
	 * @param sendingStrategies
	 *            The {@link List} of sending strategies.
	 */
	public CoreService(IConfigurationStorage configurationStorage, IConnection connection, IBufferStrategy bufferStrategy, List sendingStrategies) {
		if (null == configurationStorage) {
			throw new IllegalArgumentException("Configuration Storage cannot be null!");
		}

		if (null == connection) {
			throw new IllegalArgumentException("Connection cannot be null!");
		}

		if (null == bufferStrategy) {
			throw new IllegalArgumentException("Buffer strategy cannot be null!");
		}

		if ((null == sendingStrategies) || (0 == sendingStrategies.size())) {
			throw new IllegalArgumentException("At least one sending strategy has to be defined!");
		}

		this.configurationStorage = configurationStorage;
		this.connection = connection;
		this.bufferStrategy = bufferStrategy;
		this.sendingStrategies = sendingStrategies;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		for (Iterator iterator = sendingStrategies.iterator(); iterator.hasNext();) {
			ISendingStrategy strategy = (ISendingStrategy) iterator.next();
			strategy.start(this);
		}

		sendingThread = new SendingThread();
		sendingThread.start();

		platformSensorRefresher = new PlatformSensorRefresher();
		platformSensorRefresher.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		for (Iterator iterator = sendingStrategies.iterator(); iterator.hasNext();) {
			ISendingStrategy strategy = (ISendingStrategy) iterator.next();
			strategy.stop();
		}

		synchronized (sendingThread) {
			sendingThread.interrupt();
		}

		Thread temp = platformSensorRefresher;
		platformSensorRefresher = null;
		synchronized (temp) {
			temp.interrupt();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendData() {
		// notify the sending thread. if it is currently sending something,
		// nothing should happen
		synchronized (sendingThread) {
			sendingThread.notify();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix, MethodSensorData methodSensorData) {
		StringBuffer buffer = new StringBuffer();
		if (null != prefix) {
			buffer.append(prefix);
			buffer.append(".");
		}
		buffer.append(methodIdent);
		buffer.append(".");
		buffer.append(sensorTypeIdent);
		sensorDataObjects.put(buffer.toString(), methodSensorData);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodSensorData getMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix) {
		StringBuffer buffer = new StringBuffer();
		if (null != prefix) {
			buffer.append(prefix);
			buffer.append(".");
		}
		buffer.append(methodIdent);
		buffer.append(".");
		buffer.append(sensorTypeIdent);
		return (MethodSensorData) sensorDataObjects.get(buffer.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorData(long sensorTypeIdent, SystemSensorData systemSensorData) {
		sensorDataObjects.put(new Long(sensorTypeIdent), systemSensorData);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemSensorData getPlatformSensorData(long sensorTypeIdent) {
		return (SystemSensorData) sensorDataObjects.get(new Long(sensorTypeIdent));
	}

	/**
	 * {@inheritDoc}
	 */
	public void addObjectStorage(long sensorTypeIdent, long methodIdent, String prefix, IObjectStorage objectStorage) {
		StringBuffer buffer = new StringBuffer();
		if (null != prefix) {
			buffer.append(prefix);
			buffer.append(".");
		}
		buffer.append(methodIdent);
		buffer.append(".");
		buffer.append(sensorTypeIdent);
		objectStorages.put(buffer.toString(), objectStorage);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public IObjectStorage getObjectStorage(long sensorTypeIdent, long methodIdent, String prefix) {
		StringBuffer buffer = new StringBuffer();
		if (null != prefix) {
			buffer.append(prefix);
			buffer.append(".");
		}
		buffer.append(methodIdent);
		buffer.append(".");
		buffer.append(sensorTypeIdent);
		return (IObjectStorage) objectStorages.get(buffer.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListListener(ListListener listener) {
		if (!listListeners.contains(listener)) {
			listListeners.add(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListListener(ListListener listener) {
		listListeners.remove(listener);
	}

	/**
	 * Notify all registered listeners that a change occurred in the lists.
	 */
	private void notifyListListeners() {
		if (listListeners.size() > 0) {
			List temp = new ArrayList(sensorDataObjects.values());
			temp.addAll(objectStorages.values());
			for (int i = 0; i < listListeners.size(); i++) {
				ListListener listListener = (ListListener) listListeners.get(i);
				listListener.contentChanged(temp);
			}
		}
	}

	/**
	 * The PlatformSensorRefresher is a {@link Thread} which waits the specified
	 * platformSensorRefreshTime and then updates the platform informations.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private class PlatformSensorRefresher extends Thread {

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			Thread thisThread = Thread.currentThread();
			while (platformSensorRefresher == thisThread) {
				try {
					synchronized (this) {
						wait(platformSensorRefreshTime);
					}
				} catch (InterruptedException e) {
					LOGGER.info("Platform sensor refresher was interrupted!");
				}

				// iterate the platformSensors and update the information
				for (Iterator iterator = configurationStorage.getPlatformSensorTypes().iterator(); iterator.hasNext();) {
					PlatformSensorTypeConfig platformSensorTypeConfig = (PlatformSensorTypeConfig) iterator.next();
					IPlatformSensor platformSensor = (IPlatformSensor) platformSensorTypeConfig.getSensorType();
					if (platformSensor.automaticUpdate()) {
						platformSensor.update(CoreService.this, platformSensorTypeConfig.getId());
					}
				}
			}
		}
	}

	/**
	 * Returns the current refresh time of the platform sensors.
	 * 
	 * @return The platform sensor refresh time.
	 */
	public long getPlatformSensorRefreshTime() {
		return platformSensorRefreshTime;
	}

	/**
	 * Sets the platform sensor refresh time.
	 * 
	 * @param platformSensorRefreshTime
	 *            The platform sensor refresh time to set.
	 */
	public void setPlatformSensorRefreshTime(long platformSensorRefreshTime) {
		this.platformSensorRefreshTime = platformSensorRefreshTime;
	}

	/**
	 * This implementation of a {@link Thread} is used to start the sending of
	 * the data and value objects to the CMR.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private class SendingThread extends Thread {
		/**
		 * {@inheritDoc}
		 */
		public void run() {
			for (;;) {
				// wait for activation
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						LOGGER.info("Sending thread interrupted!");
					}
				}

				// We got a request from one of the send strategies. Now create
				// all the value objects from the object storages and generate a
				// list containing all the value objects.

				// check if measurements are added in the last interval
				if (!sensorDataObjects.isEmpty() || !objectStorages.isEmpty()) {
					// switch the references so that new data is stored
					// while sending
					temp = sensorDataObjects;
					sensorDataObjects = measurementsProcessing;
					measurementsProcessing = temp;

					temp = objectStorages;
					objectStorages = objectStoragesProcessing;
					objectStoragesProcessing = temp;

					// copy the measurements values to a new list
					List tempList = new ArrayList(measurementsProcessing.values());
					measurementsProcessing.clear();

					// iterate the object storages and get the value
					// objects which will be stored in the same list.
					for (Iterator i = objectStoragesProcessing.values().iterator(); i.hasNext();) {
						IObjectStorage objectStorage = (IObjectStorage) i.next();
						tempList.add(objectStorage.finalizeDataObject());
					}
					objectStoragesProcessing.clear();

					// Now give the strategy the list
					bufferStrategy.addMeasurements(tempList);

					try {
						while (bufferStrategy.hasNext()) {
							connection.sendDataObjects((List) bufferStrategy.next());
							bufferStrategy.remove();
						}
					} catch (Throwable e) {
						LOGGER.severe("Connection problem appeared, stopping sending actual data!");
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode, ExceptionSensorData exceptionSensorData) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(sensorTypeIdent);
		buffer.append("::");
		buffer.append(throwableIdentityHashCode);

		if (!sensorDataObjects.containsKey(buffer.toString()) && exceptionSensorData.getExceptionEvent().equals(ExceptionEventEnum.CREATED)) {
			sensorDataObjects.put(buffer.toString(), exceptionSensorData);
			notifyListListeners();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ExceptionSensorData getExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(sensorTypeIdent);
		buffer.append("::");
		buffer.append(throwableIdentityHashCode);

		return (ExceptionSensorData) sensorDataObjects.get(buffer.toString());
	}

}
