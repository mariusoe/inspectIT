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
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
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
	 * Already used data objects which can be used directly on the CMR to persist.
	 */
	private Map<String, DefaultData> sensorDataObjects = new ConcurrentHashMap<String, DefaultData>();

	/**
	 * Contains object storage instances which will be initialized when sending.
	 */
	private Map<String, IObjectStorage> objectStorages = new ConcurrentHashMap<String, IObjectStorage>();

	/**
	 * Used as second hash table for the measurements when processed before sending.
	 */
	private Map<String, DefaultData> measurementsProcessing = new ConcurrentHashMap<String, DefaultData>();

	/**
	 * Used as second hash table for the object storages when processed before sending.
	 */
	private Map<String, IObjectStorage> objectStoragesProcessing = new ConcurrentHashMap<String, IObjectStorage>();

	/**
	 * Temporary Map to switch the references of the active hash table with the processed one.
	 */
	private Map<String, ?> temp;

	/**
	 * The registered list listeners.
	 */
	private List<ListListener<?>> listListeners = new ArrayList<ListListener<?>>();

	/**
	 * The available and registered sending strategies.
	 */
	private List<ISendingStrategy> sendingStrategies = new ArrayList<ISendingStrategy>();

	/**
	 * The selected buffer strategy to store the list of value objects.
	 */
	private IBufferStrategy<DefaultData> bufferStrategy;

	/**
	 * The default refresh time.
	 */
	private static final long DEFAULT_REFRESH_TIME = 1000L;

	/**
	 * The refresh time for the platformSensorRefresher thread in ms.
	 */
	private long platformSensorRefreshTime = DEFAULT_REFRESH_TIME;

	/**
	 * The platformSensorRefresher is a thread which updates the platform informations after the
	 * specified platformSensorRefreshTime.
	 */
	private volatile PlatformSensorRefresher platformSensorRefresher;

	/**
	 * The preparing thread used to execute the preparation of the measurement in a separate
	 * process.
	 */
	private volatile PreparingThread preparingThread;

	/**
	 * The sending thread used to execute the sending of the measurement in a separate process.
	 */
	private volatile SendingThread sendingThread;

	/**
	 * Defines if there was an exception before while trying to send the data. Used to throttle the
	 * printing of log statements.
	 */
	private boolean sendingException = false;

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
	public CoreService(IConfigurationStorage configurationStorage, IConnection connection, IBufferStrategy<DefaultData> bufferStrategy, List<ISendingStrategy> sendingStrategies) {
		if (null == configurationStorage) {
			throw new IllegalArgumentException("Configuration Storage cannot be null!");
		}

		if (null == connection) {
			throw new IllegalArgumentException("Connection cannot be null!");
		}

		if (null == bufferStrategy) {
			throw new IllegalArgumentException("Buffer strategy cannot be null!");
		}

		if (null == sendingStrategies || sendingStrategies.isEmpty()) {
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
		for (ISendingStrategy strategy : sendingStrategies) {
			strategy.start(this);
		}

		preparingThread = new PreparingThread();
		preparingThread.start();

		sendingThread = new SendingThread();
		sendingThread.start();

		platformSensorRefresher = new PlatformSensorRefresher();
		platformSensorRefresher.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		for (ISendingStrategy strategy : sendingStrategies) {
			strategy.stop();
		}

		synchronized (preparingThread) {
			preparingThread.interrupt();
		}

		synchronized (sendingThread) {
			sendingThread.interrupt();
		}

		Thread temp = platformSensorRefresher;
		platformSensorRefresher = null; // NOPMD
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
		synchronized (preparingThread) {
			preparingThread.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix, MethodSensorData methodSensorData) {
		StringBuffer buffer = new StringBuffer();
		if (null != prefix) {
			buffer.append(prefix);
			buffer.append('.');
		}
		buffer.append(methodIdent);
		buffer.append('.');
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
			buffer.append('.');
		}
		buffer.append(methodIdent);
		buffer.append('.');
		buffer.append(sensorTypeIdent);
		return (MethodSensorData) sensorDataObjects.get(buffer.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorData(long sensorTypeIdent, SystemSensorData systemSensorData) {
		sensorDataObjects.put(Long.toString(sensorTypeIdent), systemSensorData);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemSensorData getPlatformSensorData(long sensorTypeIdent) {
		return (SystemSensorData) sensorDataObjects.get(Long.toString(sensorTypeIdent));
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode, ExceptionSensorData exceptionSensorData) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(sensorTypeIdent);
		buffer.append("::");
		buffer.append(throwableIdentityHashCode);
		String key = buffer.toString();

		// we always only save the first data object, because this object contains the nested
		// objects to create the whole exception tree
		if (exceptionSensorData.getExceptionEvent().equals(ExceptionEvent.CREATED)) {
			// if a data object with the same hash code was already created, then it has to be
			// removed, because it was created from a constructor delegation. For us only the
			// last-most data object is relevant
			if (sensorDataObjects.containsKey(key)) {
				sensorDataObjects.remove(key);
			}

			sensorDataObjects.put(key, exceptionSensorData);
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

	/**
	 * {@inheritDoc}
	 */
	public void addObjectStorage(long sensorTypeIdent, long methodIdent, String prefix, IObjectStorage objectStorage) {
		StringBuffer buffer = new StringBuffer();
		if (null != prefix) {
			buffer.append(prefix);
			buffer.append('.');
		}
		buffer.append(methodIdent);
		buffer.append('.');
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
			buffer.append('.');
		}
		buffer.append(methodIdent);
		buffer.append('.');
		buffer.append(sensorTypeIdent);
		return (IObjectStorage) objectStorages.get(buffer.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListListener(ListListener<?> listener) {
		if (!listListeners.contains(listener)) {
			listListeners.add(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListListener(ListListener<?> listener) {
		listListeners.remove(listener);
	}

	/**
	 * Notify all registered listeners that a change occurred in the lists.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void notifyListListeners() {
		if (!listListeners.isEmpty()) {
			List temp = new ArrayList(sensorDataObjects.values());
			temp.addAll(objectStorages.values());
			for (ListListener<?> listListener : listListeners) {
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
			while (platformSensorRefresher == thisThread) { // NOPMD
				try {
					synchronized (this) {
						wait(platformSensorRefreshTime);
					}
				} catch (InterruptedException e) {
					LOGGER.severe("Platform sensor refresher was interrupted!");
				}

				// iterate the platformSensors and update the information
				for (PlatformSensorTypeConfig platformSensorTypeConfig : configurationStorage.getPlatformSensorTypes()) {
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
	 * This implementation of a {@link Thread} is used to prepare the data and value objects that
	 * have to be sent to the CMR. Prepared data is put into {@link IBufferStrategy}.
	 * <p>
	 * Note that only one thread of this type can be started. Otherwise serious synchronization
	 * problems can appear.
	 * 
	 * @author Patrice Bouillet
	 * @author Ivan Senic
	 * 
	 */
	private class PreparingThread extends Thread {
		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public void run() {
			for (;;) {
				// wait for activation
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						LOGGER.severe("Preparing thread interrupted!");
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
					measurementsProcessing = (Map<String, DefaultData>) temp;

					temp = objectStorages;
					objectStorages = objectStoragesProcessing;
					objectStoragesProcessing = (Map<String, IObjectStorage>) temp;

					// copy the measurements values to a new list
					List<DefaultData> tempList = new ArrayList<DefaultData>(measurementsProcessing.values());
					measurementsProcessing.clear();

					// iterate the object storages and get the value
					// objects which will be stored in the same list.
					for (Iterator<IObjectStorage> i = objectStoragesProcessing.values().iterator(); i.hasNext();) {
						IObjectStorage objectStorage = i.next();
						tempList.add(objectStorage.finalizeDataObject());
					}
					objectStoragesProcessing.clear();

					// Now give the strategy the list
					bufferStrategy.addMeasurements(tempList);

					// Notify sending thread
					synchronized (sendingThread) {
						sendingThread.notifyAll();
					}
				}
			}
		}
	}

	/**
	 * This implementation of a {@link Thread} is taking the data from the {@link IBufferStrategy}
	 * and sending it to the CMR.
	 * <p>
	 * Note that only one thread of this type can be started. Otherwise serious synchronization
	 * problems can appear.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class SendingThread extends Thread {
		/**
		 * {@inheritDoc}
		 */
		public void run() {
			for (;;) {
				// wait for activation if there is nothing to send
				if (!bufferStrategy.hasNext()) {
					synchronized (this) {
						if (!bufferStrategy.hasNext()) { // NOCHK: Will be fixed with another ticket
							try {
								wait();
							} catch (InterruptedException e) {
								LOGGER.severe("Sending thread interrupted!");
							}
						}
					}
				}

				try {
					while (bufferStrategy.hasNext()) {
						List<DefaultData> dataToSend = bufferStrategy.next();
						connection.sendDataObjects(dataToSend);
						sendingException = false;
					}
				} catch (Throwable e) { // NOPMD
					if (!sendingException) {
						sendingException = true;
						LOGGER.log(Level.SEVERE, "Connection problem appeared, stopping sending actual data!", e);
					}
				}
			}
		}
	}

}
