package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.service.IBufferService;
import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.cmr.service.IConfigurationInterfaceDataAccessService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.cmr.service.IServerStatusService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.cmr.service.IStorageService;
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;
import info.novatec.inspectit.rcp.repository.service.cmr.CmrServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * The CMR repository definition initializes the services exposed by the CMR.
 * 
 * @author Patrice Bouillet
 * @author Dirk Maucher
 * @author Eduard Tudenhoefner
 * @author Matthias Huber
 * 
 */
public class CmrRepositoryDefinition implements RepositoryDefinition, ICmrRepositoryProvider {

	/**
	 * Default CMR name.
	 */
	public static final String DEFAULT_NAME = "Local CMR";

	/**
	 * Default CMR ip address.
	 */
	public static final String DEFAULT_IP = "localhost";

	/**
	 * Default CMR port.
	 */
	public static final int DEFAULT_PORT = 8182;

	/**
	 * Default description.
	 */
	public static final String DEFAULT_DESCRIPTION = "This Central Management Repository (CMR) is automatically added by default when you first start the inspectIT.";

	/**
	 * Enumeration for the online status of {@link CmrRepositoryDefinition}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum OnlineStatus {
		
		/**
		 * Unknown state before the first check.
		 */
		UNKNOWN,

		/**
		 * CMR is off-line.
		 */
		OFFLINE,

		/**
		 * Status is being checked.
		 */
		CHECKING,

		/**
		 * CMR is online.
		 */
		ONLINE;

		/**
		 * Defines if the status can be changed.
		 * 
		 * @param newStatus
		 *            New status
		 * @return True if the status change is allowed.
		 */
		public boolean canChangeTo(OnlineStatus newStatus) {
			if (this.equals(newStatus)) {
				return false;
			}
			if (newStatus.equals(UNKNOWN)) {
				return false;
			}
			switch (this) {
			case OFFLINE:
				if (newStatus.equals(ONLINE)) {
					return false;
				}
			case ONLINE:
				if (newStatus.equals(OFFLINE)) {
					return false;
				}
			default:
				return true;
			}
		}
	}

	/**
	 * The ip of the CMR.
	 */
	private final String ip;

	/**
	 * The port used by the CMR.
	 */
	private final int port;

	/**
	 * State of the CMR.
	 */
	private OnlineStatus onlineStatus;

	/**
	 * CMR name assigned by user.
	 */
	private String name;

	/**
	 * Optional description for the CMR.
	 */
	private String description;

	/**
	 * The cached data service.
	 */
	private final CachedDataService cachedDataService;

	/**
	 * The sql data access service.
	 */
	private final ISqlDataAccessService sqlDataAccessService;

	/**
	 * The invocation data access service.
	 */
	private final IInvocationDataAccessService invocationDataAccessService;

	/**
	 * The exception data access service.
	 */
	private final IExceptionDataAccessService exceptionDataAccessService;

	/**
	 * The combined metrics data access service.
	 */
	private final ICombinedMetricsDataAccessService combinedMetricsDataAccessService;

	/**
	 * The configuration interface data access service.
	 */
	private final IConfigurationInterfaceDataAccessService configurationInterfaceDataAccessService;

	/**
	 * The server status service exposed by the CMR and initialized by Spring.
	 */
	private final IServerStatusService serverStatusService;

	/**
	 * The license service.
	 */
	private final ILicenseService licenseService;

	/**
	 * The buffer data access service.
	 */
	private IBufferService bufferService;

	/**
	 * The timer data access service.
	 */
	private ITimerDataAccessService timerDataAccessService;

	/**
	 * The http timer data access service.
	 */
	private IHttpTimerDataAccessService httpTimerDataAccessService;

	/**
	 * The {@link IGlobalDataAccessService}.
	 */
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * The storage service.
	 */
	private IStorageService storageService;

	/**
	 * CMR repository change listeners.
	 */
	private List<CmrRepositoryChangeListener> cmrRepositoryChangeListeners = new ArrayList<CmrRepositoryChangeListener>(1);

	/**
	 * Calls default constructor with name 'Undefined'.
	 * 
	 * @param ip
	 *            The ip of the CMR.
	 * @param port
	 *            The port used by the CMR.
	 */
	public CmrRepositoryDefinition(String ip, int port) {
		this(ip, port, "Undefined");
	}

	/**
	 * The default constructor of this class. The ip and port is mandatory to create the connection.
	 * 
	 * @param ip
	 *            The ip of the CMR.
	 * @param port
	 *            The port used by the CMR.
	 * @param name
	 *            The name of the CMR assigned by user.
	 */
	public CmrRepositoryDefinition(String ip, int port, String name) {
		this.ip = ip;
		this.port = port;
		this.onlineStatus = OnlineStatus.UNKNOWN;
		this.name = name;

		CmrServiceProvider cmrServiceProvider = (CmrServiceProvider) InspectIT.getDefault().getApplicationContext().getBean("cmrServiceProvider");

		sqlDataAccessService = cmrServiceProvider.getSqlDataAccessService(this);
		serverStatusService = cmrServiceProvider.getServerStatusService(this);
		invocationDataAccessService = cmrServiceProvider.getInvocationDataAccessService(this);
		licenseService = cmrServiceProvider.getLicenseService(this);
		exceptionDataAccessService = cmrServiceProvider.getExceptionDataAccessService(this);
		combinedMetricsDataAccessService = cmrServiceProvider.getCombinedMetricsDataAccessService(this);
		configurationInterfaceDataAccessService = cmrServiceProvider.getConfigurationInterfaceDataAccessService(this);
		httpTimerDataAccessService = cmrServiceProvider.getHttpTimerDataAccessService(this);
		bufferService = cmrServiceProvider.getBufferService(this);
		timerDataAccessService = cmrServiceProvider.getTimerDataAccessService(this);
		globalDataAccessService = cmrServiceProvider.getGlobalDataAccessService(this);
		storageService = cmrServiceProvider.getStorageService(this);

		cachedDataService = new CachedDataService(globalDataAccessService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CachedDataService getCachedDataService() {
		return cachedDataService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IExceptionDataAccessService getExceptionDataAccessService() {
		return exceptionDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISqlDataAccessService getSqlDataAccessService() {
		return sqlDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInvocationDataAccessService getInvocationDataAccessService() {
		return invocationDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ICombinedMetricsDataAccessService getCombinedMetricsDataAccessService() {
		return combinedMetricsDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	public IConfigurationInterfaceDataAccessService getConfigurationInterfaceDataAccessService() {
		return configurationInterfaceDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IServerStatusService getServerStatusService() {
		return serverStatusService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ILicenseService getLicenseService() {
		return licenseService;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBufferService getBufferService() {
		return bufferService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITimerDataAccessService getTimerDataAccessService() {
		return timerDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageService getStorageService() {
		return storageService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHttpTimerDataAccessService getHttpTimerDataAccessService() {
		return httpTimerDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IGlobalDataAccessService getGlobalDataAccessService() {
		return globalDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIp() {
		return ip;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		return port;
	}

	/**
	 * Registers a CMR repository change listener to this CMR if it was not already.
	 * 
	 * @param cmrRepositoryChangeListener
	 *            {@link CmrRepositoryChangeListener}.
	 */
	public void addCmrRepositoryChangeListener(CmrRepositoryChangeListener cmrRepositoryChangeListener) {
		synchronized (cmrRepositoryChangeListeners) {
			if (!cmrRepositoryChangeListeners.contains(cmrRepositoryChangeListener)) {
				cmrRepositoryChangeListeners.add(cmrRepositoryChangeListener);
			}
		}
	}

	/**
	 * Removes a CMR repository change listener to this CMR.
	 * 
	 * @param cmrRepositoryChangeListener
	 *            {@link CmrRepositoryChangeListener}.
	 */
	public void removeCmrRepositoryChangeListener(CmrRepositoryChangeListener cmrRepositoryChangeListener) {
		synchronized (cmrRepositoryChangeListeners) {
			cmrRepositoryChangeListeners.remove(cmrRepositoryChangeListener);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the onlineStatus
	 */
	public OnlineStatus getOnlineStatus() {
		return onlineStatus;
	}

	/**
	 * Updates the status of the CMR if possible.
	 * 
	 * @param newStatus
	 *            New status.
	 * @return True if change was successful, false if the change is not allowed.
	 */
	public boolean changeOnlineStatus(OnlineStatus newStatus) {
		if (onlineStatus.canChangeTo(newStatus)) {
			OnlineStatus oldStatus = onlineStatus;
			onlineStatus = newStatus;
			synchronized (cmrRepositoryChangeListeners) {
				for (CmrRepositoryChangeListener changeListener : cmrRepositoryChangeListeners) {
					changeListener.repositoryOnlineStatusUpdated(this, oldStatus, newStatus);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Refreshes the online status.
	 */
	public void refreshOnlineStatus() {
		this.changeOnlineStatus(OnlineStatus.CHECKING);
		boolean isOnline = isOnline();
		if (isOnline) {
			this.changeOnlineStatus(OnlineStatus.ONLINE);
		} else {
			this.changeOnlineStatus(OnlineStatus.OFFLINE);
		}
	}

	/**
	 * Returns if the server is online by checking the {@link IServerStatusService}.
	 * 
	 * @return Returns if the server is online by checking the {@link IServerStatusService}.
	 */
	private boolean isOnline() {
		try {
			int status = serverStatusService.getServerStatus();
			if (IServerStatusService.SERVER_ONLINE == status) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CmrRepositoryDefinition other = (CmrRepositoryDefinition) obj;
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!ip.equals(other.ip)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Repository definition :: Name=" + name + " IP=" + ip + " Port=" + port;
	}

}
