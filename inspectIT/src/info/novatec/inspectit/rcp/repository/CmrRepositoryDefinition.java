package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.service.IBufferService;
import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.cmr.service.IConfigurationInterfaceDataAccessService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.rcp.repository.service.ConfigurationInterfaceDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cmr.BufferService;
import info.novatec.inspectit.rcp.repository.service.cmr.CombinedMetricsDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cmr.ExceptionDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cmr.GlobalDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cmr.InvocationDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cmr.LicenseService;
import info.novatec.inspectit.rcp.repository.service.cmr.ServerStatusService;
import info.novatec.inspectit.rcp.repository.service.cmr.SqlDataAccessService;

/**
 * The CMR repository definition initializes the services exposed by the CMR.
 * 
 * @author Patrice Bouillet
 * @author Dirk Maucher
 * @author Eduard Tudenhoefner
 * @author Matthias Huber
 * 
 */
public class CmrRepositoryDefinition implements RepositoryDefinition {

	/**
	 * The ip of the CMR.
	 */
	private final String ip;

	/**
	 * The port used by the CMR.
	 */
	private final int port;

	/**
	 * The global data access service.
	 */
	private final GlobalDataAccessService globalDataAccessService;

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
	 * The configuration interface data access service
	 */
	private final IConfigurationInterfaceDataAccessService configurationInterfaceDataAccessService;

	/**
	 * The server status service exposed by the CMR and initialized by Spring.
	 */
	private final ServerStatusService serverStatusService;

	/**
	 * The license service.
	 */
	private final ILicenseService licenseService;

	/**
	 * The buffer data access service.
	 */
	private IBufferService bufferService;
	
	/**
	 * The only constructor of this class. The ip and port is mandatory to
	 * create the connection.
	 * 
	 * @param ip
	 *            The ip of the CMR.
	 * @param port
	 *            The port used by the CMR.
	 */
	public CmrRepositoryDefinition(String ip, int port) {
		this.ip = ip;
		this.port = port;

		globalDataAccessService = new GlobalDataAccessService(ip, port);
		sqlDataAccessService = new SqlDataAccessService(ip, port);
		serverStatusService = new ServerStatusService(ip, port);
		invocationDataAccessService = new InvocationDataAccessService(ip, port);
		licenseService = new LicenseService(ip, port);
		exceptionDataAccessService = new ExceptionDataAccessService(ip, port);
		combinedMetricsDataAccessService = new CombinedMetricsDataAccessService(ip, port);
		configurationInterfaceDataAccessService = new ConfigurationInterfaceDataAccessService(ip, port);
		bufferService = new BufferService(ip, port);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GlobalDataAccessService getGlobalDataAccessService() {
		return globalDataAccessService;
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

	@Override
	public IConfigurationInterfaceDataAccessService getConfigurationInterfaceDataAccessService() {
		return configurationInterfaceDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerStatusService getServerStatusService() {
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
	@Override
	public IBufferService getBufferService() {
		return bufferService;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Repository definition :: IP=" + ip + " Port=" + port;
	}

}
