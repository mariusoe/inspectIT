package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.RegistrationException;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.cmr.service.IAgentStorageService;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.cmr.service.LicenseException;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.communication.DefaultData;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implements the {@link IConnection} interface using RMI.
 * 
 * @author Patrice Bouillet
 * 
 */
public class RMIConnection implements IConnection {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(RMIConnection.class.getName());

	/**
	 * The name of the repository service.
	 */
	private String agentStorageName = IAgentStorageService.class.getCanonicalName();

	/**
	 * The name of the registration service.
	 */
	private String registrationName = IRegistrationService.class.getCanonicalName();

	/**
	 * The RMI registry.
	 */
	private Registry registry;

	/**
	 * The agent storage rmi object which will be used to send the measurements to.
	 */
	private IAgentStorageService agentStorageService;

	/**
	 * The registration rmi object which will be used for the registration of the sensors.
	 */
	private IRegistrationService registrationService;

	/**
	 * Attribute to check if we are connected.
	 */
	private boolean connected = false;

	/**
	 * Defines if there was a connection exception before. Used for throttling the info log
	 * messages.
	 */
	private boolean connectionException = false;

	/**
	 * The list of all network interfaces.
	 */
	private List<String> networkInterfaces;

	/**
	 * {@inheritDoc}
	 */
	public void connect(String host, int port) throws ConnectException {
		if (null == registry) {
			try {
				if (!connectionException) {
					LOGGER.info("RMI: Connecting to " + host + ":" + port);
				}
				registry = LocateRegistry.getRegistry(host, port);
				agentStorageService = (IAgentStorageService) registry.lookup(agentStorageName);
				registrationService = (IRegistrationService) registry.lookup(registrationName);
				LOGGER.info("RMI: Connection established!");
				connected = true;
				connectionException = false;
			} catch (RemoteException remoteException) {
				if (!connectionException) {
					LOGGER.info("RMI: Connection to the server failed.");
				}
				connectionException = true;
				disconnect();
				LOGGER.throwing(RMIConnection.class.getName(), "connect()", remoteException);
				ConnectException e = new ConnectException(remoteException.getMessage());
				e.initCause(remoteException);
				throw e; // NOPMD root cause exception is set
			} catch (NotBoundException notBoundException) {
				if (!connectionException) {
					LOGGER.info("RMI: Needed services are not bound on the server.");
				}
				connectionException = true;
				disconnect();
				LOGGER.throwing(RMIConnection.class.getName(), "connect()", notBoundException);
				ConnectException e = new ConnectException(notBoundException.getMessage());
				e.initCause(notBoundException);
				throw e; // NOPMD root cause exception is set
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void disconnect() {
		registry = null; // NOPMD
		agentStorageService = null; // NOPMD
		registrationService = null; // NOPMD
		connected = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatform(String agentName, String version) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		try {
			if (null == networkInterfaces) {
				networkInterfaces = getNetworkInterfaces();
			}
			return registrationService.registerPlatformIdent(networkInterfaces, agentName, version);
		} catch (RemoteException remoteException) {
			LOGGER.throwing(RMIConnection.class.getName(), "registerPlatform(String)", remoteException);
			if (remoteException.getCause() instanceof LicenseException) {
				LOGGER.severe("License could not be obtained, inspectIT agent will not start! Cause: " + remoteException.getCause().getMessage());
			}
			throw new RegistrationException("Could not register the platform", remoteException);
		} catch (SocketException socketException) {
			LOGGER.severe("Could not obtain network interfaces from this machine!");
			LOGGER.throwing(RMIConnection.class.getName(), "Constructor", socketException);
			throw new RegistrationException("Could not register the platform", socketException);
		} catch (ServiceException serviceException) {
			LOGGER.throwing(ServiceException.class.getName(), "registerPlatform(String)", serviceException);
			throw new RegistrationException("Could not register the platform", serviceException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterPlatform(String agentName) throws RegistrationException {
		if (!connected) {
			return;
		}

		try {
			if (null == networkInterfaces) {
				networkInterfaces = getNetworkInterfaces();
			}

			registrationService.unregisterPlatformIdent(networkInterfaces, agentName);
		} catch (SocketException socketException) {
			LOGGER.severe("Could not obtain network interfaces from this machine!");
			LOGGER.throwing(RMIConnection.class.getName(), "unregisterPlatform(List,String)", socketException);
			throw new RegistrationException("Could not un-register the platform", socketException);
		} catch (ServiceException serviceException) {
			LOGGER.throwing(RMIConnection.class.getName(), "unregisterPlatform(List,String)", serviceException);
			throw new RegistrationException("Could not un-register the platform", serviceException);
		} catch (RemoteException remoteException) {
			LOGGER.throwing(RMIConnection.class.getName(), "unregisterPlatform(List,String)", remoteException);
			throw new RegistrationException("Could not un-register the platform", remoteException);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void sendDataObjects(List<? extends DefaultData> measurements) throws ServerUnavailableException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		if (null != measurements && !measurements.isEmpty()) {
			try {
				AbstractRemoteMethodCall remote = new AddDataObjects(agentStorageService, measurements);
				remote.makeCall();
			} catch (ServerUnavailableException serverUnavailableException) {
				LOGGER.throwing(RMIConnection.class.getName(), "sendDataObjects(List)", serverUnavailableException);
				throw serverUnavailableException;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethod(long platformId, RegisteredSensorConfig sensorConfig) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		RegisterMethodIdent register = new RegisterMethodIdent(registrationService, sensorConfig, platformId);
		try {
			Long id = (Long) register.makeCall();
			return id.longValue();
		} catch (ServerUnavailableException serverUnavailableException) {
			LOGGER.throwing(RMIConnection.class.getName(), "registerMethod(RegisteredSensorConfig)", serverUnavailableException);
			throw new RegistrationException("Could not register the method", serverUnavailableException);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethodSensorType(long platformId, MethodSensorTypeConfig methodSensorTypeConfig) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		RegisterMethodSensorType register = new RegisterMethodSensorType(registrationService, methodSensorTypeConfig, platformId);
		try {
			Long id = (Long) register.makeCall();
			return id.longValue();
		} catch (ServerUnavailableException serverUnavailableException) {
			LOGGER.throwing(RMIConnection.class.getName(), "registerMethod(RegisteredSensorConfig)", serverUnavailableException);
			throw new RegistrationException("Could not register the method sensor type", serverUnavailableException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatformSensorType(long platformId, PlatformSensorTypeConfig platformSensorTypeConfig) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		RegisterPlatformSensorType register = new RegisterPlatformSensorType(registrationService, platformSensorTypeConfig, platformId);
		try {
			Long id = (Long) register.makeCall();
			return id.longValue();
		} catch (ServerUnavailableException serverUnavailableException) {
			LOGGER.throwing(RMIConnection.class.getName(), "registerPlatformSensorType(PlatformSensorTypeConfig)", serverUnavailableException);
			throw new RegistrationException("Could not register the platform sensor type", serverUnavailableException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensorTypeToMethod(long sensorTypeId, long methodId) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		AddSensorTypeToMethod addTypeToSensor = new AddSensorTypeToMethod(registrationService, sensorTypeId, methodId);
		try {
			addTypeToSensor.makeCall();
		} catch (ServerUnavailableException serverUnavailableException) {
			LOGGER.throwing(RMIConnection.class.getName(), "addSensorTypeToMethod(long, long)", serverUnavailableException);
			throw new RegistrationException("Could not add the sensor type to a method", serverUnavailableException);
		}
	}

	/**
	 * Loads all the network interfaces and transforms the enumeration to the list of strings
	 * containing all addresses.
	 * 
	 * @return List of all network interfaces.
	 * @throws SocketException
	 *             If {@link SocketException} occurs.
	 */
	private List<String> getNetworkInterfaces() throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		List<String> networkInterfaces = new ArrayList<String>();

		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = (InetAddress) addresses.nextElement();
				networkInterfaces.add(address.getHostAddress());
			}
		}

		return networkInterfaces;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConnected() {
		return connected;
	}
}
