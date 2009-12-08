package info.novatec.novaspy.agent.core.impl;

import info.novatec.novaspy.agent.config.IConfigurationStorage;
import info.novatec.novaspy.agent.config.impl.AbstractSensorTypeConfig;
import info.novatec.novaspy.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.novaspy.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.config.impl.RepositoryConfig;
import info.novatec.novaspy.agent.connection.IConnection;
import info.novatec.novaspy.agent.connection.RegistrationException;
import info.novatec.novaspy.agent.connection.ServerUnavailableException;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IdNotAvailableException;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.picocontainer.Startable;

/**
 * The default implementation of the ID Manager.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public class IdManager implements IIdManager, Startable {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(IdManager.class.getName());

	/**
	 * The configuration storage used to access some information which needs to
	 * be registered at the server.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	private final IConnection connection;

	/**
	 * The id of this platform.
	 */
	private long platformId = -1;

	/**
	 * The mapping between the local and remote method ids.
	 */
	private Map methodIdMap = new HashMap();

	/**
	 * The mapping between the local and remote sensor type ids.
	 */
	private Map sensorTypeIdMap = new HashMap();

	/**
	 * The {@link Thread} used to register the outstanding methods, sensor types
	 * etc.
	 */
	private volatile RegistrationThread registrationThread;

	/**
	 * The methods to register at the server.
	 */
	private LinkedList methodsToRegister = new LinkedList();

	/**
	 * The sensor types to register at the server.
	 */
	private LinkedList sensorTypesToRegister = new LinkedList();

	/**
	 * The mapping between the sensor types and methods to register at the
	 * server.
	 */
	private LinkedList sensorTypeToMethodRegister = new LinkedList();

	/**
	 * If set to <code>true</code>, the connection to server created an
	 * exception.
	 */
	private volatile boolean serverErrorOccured = false;

	/**
	 * Default constructor. Needs an implementation of the {@link IConnection}
	 * interface to establish the connection to the server.
	 * 
	 * @param configurationStorage
	 *            The configuration storage.
	 * @param connection
	 *            The connection to the server.
	 */
	public IdManager(IConfigurationStorage configurationStorage, IConnection connection) {
		this.configurationStorage = configurationStorage;
		this.connection = connection;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		if (null == registrationThread) {
			registrationThread = new RegistrationThread();
			registrationThread.start();
		}

		// register all method sensor types saved in the configuration storage
		for (Iterator iterator = configurationStorage.getMethodSensorTypes().iterator(); iterator.hasNext();) {
			MethodSensorTypeConfig config = (MethodSensorTypeConfig) iterator.next();
			this.registerMethodSensorType(config);
		}

		// register all platform sensor types saved in the configuration storage
		for (Iterator iterator = configurationStorage.getPlatformSensorTypes().iterator(); iterator.hasNext();) {
			PlatformSensorTypeConfig config = (PlatformSensorTypeConfig) iterator.next();
			this.registerPlatformSensorType(config);
		}
		
		// register exception sensor type saved in the configuration storage
		for (Iterator iterator = configurationStorage.getExceptionSensorTypes().iterator(); iterator.hasNext();) {
			MethodSensorTypeConfig config = (MethodSensorTypeConfig) iterator.next();
			this.registerExceptionSensorType(config);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		// set the registration thread to null to indicate that the while loop
		// will be finished on the next run.
		Thread temp = registrationThread;
		registrationThread = null;
		synchronized (temp) {
			temp.interrupt();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPlatformRegistered() {
		return (-1 != platformId);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPlatformId() throws IdNotAvailableException {
		// if we are not connected to the server and the platform id was not
		// received yet we are throwing an IdNotAvailableException
		if (!connection.isConnected() && !isPlatformRegistered()) {
			if (!serverErrorOccured) {
				try {
					registrationThread.connect();
					registrationThread.registerPlatform();
				} catch (Throwable throwable) {
					serverErrorOccured = true;
					throw new IdNotAvailableException("Connection is not established yet, cannot retrieve platform ID", throwable);
				}
			} else {
				throw new IdNotAvailableException("Cannot retrieve platform ID");
			}
		} else if (!isPlatformRegistered()) {
			if (!serverErrorOccured) {
				// If the platform is not registered and no server error
				// occurred, the registration is started
				try {
					registrationThread.registerPlatform();
				} catch (Throwable throwable) {
					serverErrorOccured = true;
					LOGGER.info("Could not register the platform even though the connection seems to be established, will try later!");
					throw new IdNotAvailableException("Could not register the platform even though the connection seems to be established, will try later!", throwable);
				}
			} else {
				throw new IdNotAvailableException("Cannot retrieve platform ID");
			}
		}

		return platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRegisteredMethodId(long methodId) throws IdNotAvailableException {
		Long methodIdentifier = new Long(methodId);

		// do not enter the block if the method ID map already contains this
		// identifier (which means that it is already registered).
		if (!methodIdMap.containsKey(methodIdentifier)) {
			throw new IdNotAvailableException("Method ID '" + methodId + "' is not mapped");
		} else {
			Long registeredMethodIdentifier = (Long) methodIdMap.get(methodIdentifier);
			return registeredMethodIdentifier.longValue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRegisteredSensorTypeId(long sensorTypeId) throws IdNotAvailableException {
		// same procedure here as in the #getRegisteredMethodId(...) method.
		Long sensorTypeIdentifier = new Long(sensorTypeId);

		if (!sensorTypeIdMap.containsKey(sensorTypeIdentifier)) {
			throw new IdNotAvailableException("Sensor Type ID '" + sensorTypeId + "' is not mapped");
		} else {
			Long registeredSensorTypeIdentifier = (Long) sensorTypeIdMap.get(sensorTypeIdentifier);
			return registeredSensorTypeIdentifier.longValue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethod(RegisteredSensorConfig registeredSensorConfig) {
		long id;
		synchronized (methodsToRegister) {
			id = methodIdMap.size() + methodsToRegister.size();
		}
		registeredSensorConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerMethod(registeredSensorConfig);
			} catch (Throwable throwable) {
				synchronized (methodsToRegister) {
					methodsToRegister.addLast(registeredSensorConfig);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (methodsToRegister) {
				methodsToRegister.addLast(registeredSensorConfig);
			}
		}

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethodSensorType(MethodSensorTypeConfig methodSensorTypeConfig) {
		// same procedure here as in #registerMethod(...)
		long id;
		synchronized (sensorTypesToRegister) {
			id = sensorTypeIdMap.size() + sensorTypesToRegister.size();
		}
		methodSensorTypeConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerSensorType(methodSensorTypeConfig);
			} catch (Throwable throwable) {
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.addLast(methodSensorTypeConfig);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (sensorTypesToRegister) {
				sensorTypesToRegister.addLast(methodSensorTypeConfig);
			}
		}

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensorTypeToMethod(long sensorTypeId, long methodId) {
		// nearly same procedure as in #registerMethod(...) but without
		// returning a value. This mapping only needs to be registered.

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.addSensorTypeToMethod(new Long(sensorTypeId), new Long(methodId));
			} catch (Throwable throwable) {
				synchronized (sensorTypeToMethodRegister) {
					sensorTypeToMethodRegister.addLast(new SensorTypeToMethodMapping(sensorTypeId, methodId));
				}

				// start the thread to retry the registration
				synchronized (registrationThread) {
					registrationThread.notifyAll();
				}
			}
		} else {
			synchronized (sensorTypeToMethodRegister) {
				sensorTypeToMethodRegister.addLast(new SensorTypeToMethodMapping(sensorTypeId, methodId));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatformSensorType(PlatformSensorTypeConfig platformSensorTypeConfig) {
		// same procedure here as in #registerMethod(...)
		long id;
		synchronized (sensorTypesToRegister) {
			id = sensorTypeIdMap.size() + sensorTypesToRegister.size();
		}
		platformSensorTypeConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerSensorType(platformSensorTypeConfig);
			} catch (Throwable throwable) {
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.addLast(platformSensorTypeConfig);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (sensorTypesToRegister) {
				sensorTypesToRegister.addLast(platformSensorTypeConfig);
			}
		}

		return id;
	}
	
	public long registerExceptionSensorType(MethodSensorTypeConfig exceptionSensorTypeConfig) {
		// same procedure here as in #registerMethod(...)
		long id;
		synchronized (sensorTypesToRegister) {
			id = sensorTypeIdMap.size() + sensorTypesToRegister.size();
		}
		exceptionSensorTypeConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerSensorType(exceptionSensorTypeConfig);
			} catch (Throwable throwable) {
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.addLast(exceptionSensorTypeConfig);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (sensorTypesToRegister) {
				sensorTypesToRegister.addLast(exceptionSensorTypeConfig);
			}
		}

		return id;
	}

	/**
	 * Private inner class used to store the mapping between the sensor type IDs
	 * and the method IDs. Only used if they are not yet registered.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static class SensorTypeToMethodMapping {

		/**
		 * The sensor type identifier.
		 */
		private long sensorTypeId;

		/**
		 * The method identifier.
		 */
		private long methodId;

		public SensorTypeToMethodMapping(long sensorTypeId, long methodId) {
			this.sensorTypeId = sensorTypeId;
			this.methodId = methodId;
		}

		public long getSensorTypeId() {
			return sensorTypeId;
		}

		public long getMethodId() {
			return methodId;
		}

	}

	/**
	 * The {@link Thread} used to register the outstanding methods, sensor types
	 * etc.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private class RegistrationThread extends Thread {

		/**
		 * The default wait time between the registrations.
		 */
		private static final long REGISTRATION_WAIT_TIME = 10000L;

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			Thread thisThread = Thread.currentThread();
			// break out of the while loop if the registrationThread is set
			// to null in the stop method of the surrounding class.
			while (registrationThread == thisThread) {
				try {
					synchronized (this) {
						if (serverErrorOccured) {
							// wait for the given time till we try the
							// registering again.
							wait(REGISTRATION_WAIT_TIME);
						} else if (methodsToRegister.isEmpty() && sensorTypesToRegister.isEmpty() && sensorTypeToMethodRegister.isEmpty()) {
							// Wait for a Object#notify()
							wait();
						}
					}
				} catch (InterruptedException e) {
					// nothing to do
				}

				doRegistration();
			}
		}

		/**
		 * Execute the registration if needed.
		 */
		private void doRegistration() {
			try {
				// not connected? -> connect
				if (!connection.isConnected()) {
					connect();
				}

				// register the agent
				if (!isPlatformRegistered()) {
					registerPlatform();
				}

				registerMethods();
				registerSensorTypes();
				registerSensorTypeToMethodMapping();

				// clear the flag
				serverErrorOccured = false;
			} catch (ServerUnavailableException serverUnavailableException) {
				serverErrorOccured = true;
				LOGGER.severe("Server unavailable while trying to register something at the server.");
				LOGGER.throwing(RegistrationThread.class.getName(), "run()", serverUnavailableException);
			} catch (RegistrationException registrationException) {
				serverErrorOccured = true;
				LOGGER.severe("Registration exception occured while trying to register something at the server.");
				LOGGER.throwing(RegistrationThread.class.getName(), "run()", registrationException);
			} catch (ConnectException connectException) {
				serverErrorOccured = true;
				LOGGER.severe("Connection to the server failed.");
				LOGGER.throwing(RegistrationThread.class.getName(), "run()", connectException);
			}
		}

		/**
		 * Establish the connection to the server.
		 * 
		 * @exception ConnectException
		 *                Throws a ConnectException if there was a problem
		 *                connecting to the repository.
		 */
		private void connect() throws ConnectException {
			RepositoryConfig repositoryConfig = configurationStorage.getRepositoryConfig();
			connection.connect(repositoryConfig.getHost(), repositoryConfig.getPort());
		}

		/**
		 * Registers the platform at the CMR.
		 * 
		 * @throws ServerUnavailableException
		 *             If the sending wasn't successful in any way, a
		 *             {@link ServerUnavailableException} exception is thrown.
		 * @throws RegistrationException
		 *             This exception is thrown when a problem with the
		 *             registration process appears.
		 */
		private void registerPlatform() throws ServerUnavailableException, RegistrationException {
			platformId = connection.registerPlatform(configurationStorage.getAgentName());

			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Received platform ID: " + platformId);
			}
		}

		/**
		 * Registers all sensor type to method mappings on the server.
		 * 
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the
		 *             mapping on the server.
		 */
		private void registerSensorTypeToMethodMapping() throws ServerUnavailableException, RegistrationException {
			while (!sensorTypeToMethodRegister.isEmpty()) {
				SensorTypeToMethodMapping mapping;
				mapping = (SensorTypeToMethodMapping) sensorTypeToMethodRegister.getFirst();

				Long sensorTypeId = new Long(mapping.getSensorTypeId());
				Long methodId = new Long(mapping.getMethodId());

				this.addSensorTypeToMethod(sensorTypeId, methodId);
				synchronized (sensorTypeToMethodRegister) {
					sensorTypeToMethodRegister.removeFirst();
				}
			}
		}

		/**
		 * Registers the mapping between the sensor type and a method.
		 * 
		 * @param sensorTypeId
		 *            The sensor type id.
		 * @param methodId
		 *            The method id.
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the
		 *             mapping on the server.
		 */
		private void addSensorTypeToMethod(Long sensorTypeId, Long methodId) throws ServerUnavailableException, RegistrationException {
			if (!sensorTypeIdMap.containsKey(sensorTypeId)) {
				throw new RegistrationException("Sensor type ID could not be found in the map!");
			}

			if (!methodIdMap.containsKey(methodId)) {
				throw new RegistrationException("Method ID could not be found in the map!");
			}

			Long serverSensorTypeId = (Long) sensorTypeIdMap.get(sensorTypeId);
			Long serverMethodId = (Long) methodIdMap.get(methodId);

			connection.addSensorTypeToMethod(serverSensorTypeId.longValue(), serverMethodId.longValue());

			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Mapping registered (method -> sensor type) :: local:" + methodId + "->" + sensorTypeId + " global:" + serverMethodId + "->" + serverSensorTypeId);
			}
		}

		/**
		 * Registers all sensor types on the server.
		 * 
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occured.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the
		 *             sensor types on the server.
		 */
		private void registerSensorTypes() throws ServerUnavailableException, RegistrationException {
			while (!sensorTypesToRegister.isEmpty()) {
				AbstractSensorTypeConfig astc;
				astc = (AbstractSensorTypeConfig) sensorTypesToRegister.getFirst();

				this.registerSensorType(astc);
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.removeFirst();
				}
			}
		}

		/**
		 * Registers a sensor type configuration at the server. Accepts
		 * {@link MethodSensorTypeConfig} and {@link PlatformSensorTypeConfig}
		 * objects.
		 * 
		 * @param astc
		 *            The sensor type configuration.
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the
		 *             sensor types on the server.
		 */
		private void registerSensorType(AbstractSensorTypeConfig astc) throws ServerUnavailableException, RegistrationException {
			long registeredId;
			if (astc instanceof MethodSensorTypeConfig) {
				registeredId = connection.registerMethodSensorType(platformId, (MethodSensorTypeConfig) astc);
			} else if (astc instanceof PlatformSensorTypeConfig) {
				registeredId = connection.registerPlatformSensorType(platformId, (PlatformSensorTypeConfig) astc);
			} else {
				throw new RegistrationException("Could not register sensor type, because unhandled type: " + astc.getClass().getName());
			}

			synchronized (sensorTypesToRegister) {
				Long localId = new Long(sensorTypeIdMap.size());
				sensorTypeIdMap.put(localId, new Long(registeredId));

				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info("Sensor type " + astc.toString() + " registered. ID (local/global): " + localId + "/" + registeredId);
				}
			}
		}

		/**
		 * Registers all methods on the server.
		 * 
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the
		 *             sensor types on the server.
		 */
		private void registerMethods() throws ServerUnavailableException, RegistrationException {
			while (!methodsToRegister.isEmpty()) {
				RegisteredSensorConfig rsc;
				rsc = (RegisteredSensorConfig) methodsToRegister.getFirst();
				this.registerMethod(rsc);
				synchronized (methodsToRegister) {
					methodsToRegister.removeFirst();
				}
			}
		}

		/**
		 * Registers a method on the server and maps the local and global id.
		 * 
		 * @param rsc
		 *            The {@link RegisteredSensorConfig} to be registered at the
		 *            server.
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the
		 *             sensor types on the server.
		 */
		private void registerMethod(RegisteredSensorConfig rsc) throws ServerUnavailableException, RegistrationException {
			long registeredId = connection.registerMethod(platformId, rsc);
			synchronized (methodsToRegister) {
				Long localId = new Long(methodIdMap.size());
				methodIdMap.put(localId, new Long(registeredId));

				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info("Method " + rsc.toString() + " registered. ID (local/global): " + localId + "/" + registeredId);
				}
			}
		}

	}

}
