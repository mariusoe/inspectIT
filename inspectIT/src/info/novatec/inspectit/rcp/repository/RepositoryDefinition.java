package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.rcp.repository.service.CombinedMetricsDataAccessService;
import info.novatec.inspectit.rcp.repository.service.ExceptionDataAccessService;
import info.novatec.inspectit.rcp.repository.service.GlobalDataAccessService;
import info.novatec.inspectit.rcp.repository.service.InvocationDataAccessService;
import info.novatec.inspectit.rcp.repository.service.LicenseService;
import info.novatec.inspectit.rcp.repository.service.ServerStatusService;
import info.novatec.inspectit.rcp.repository.service.SqlDataAccessService;

/**
 * Every repository definition initializes the services exposed by the CMR.
 * 
 * @author Patrice Bouillet
 * @author Dirk Maucher
 * @author Eduard Tudenhoefner
 * 
 */
public class RepositoryDefinition {

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
	 * The server status service exposed by the CMR and initialized by Spring.
	 */
	private final ServerStatusService serverStatusService;

	/**
	 * The license service.
	 */
	private final ILicenseService licenseService;

	/**
	 * The only constructor of this class. The ip and port is mandatory to
	 * create the connection.
	 * 
	 * @param ip
	 *            The ip of the CMR.
	 * @param port
	 *            The port used by the CMR.
	 */
	public RepositoryDefinition(String ip, int port) {
		this.ip = ip;
		this.port = port;

		globalDataAccessService = new GlobalDataAccessService(ip, port);
		sqlDataAccessService = new SqlDataAccessService(ip, port);
		serverStatusService = new ServerStatusService(ip, port);
		invocationDataAccessService = new InvocationDataAccessService(ip, port);
		licenseService = new LicenseService(ip, port);
		exceptionDataAccessService = new ExceptionDataAccessService(ip, port);
		combinedMetricsDataAccessService = new CombinedMetricsDataAccessService(ip, port);
	}

	/**
	 * Returns the global data access service for this repository definition.
	 * 
	 * @return The global data access service.
	 */
	public GlobalDataAccessService getGlobalDataAccessService() {
		return globalDataAccessService;
	}

	/**
	 * Returns the exception data access service for this repository definition.
	 * 
	 * @return The exception data access service.
	 */
	public IExceptionDataAccessService getExceptionDataAccessService() {
		return exceptionDataAccessService;
	}

	/**
	 * Returns the sql data access service for this repository definition.
	 * 
	 * @return The sql data access service.
	 */
	public ISqlDataAccessService getSqlDataAccessService() {
		return sqlDataAccessService;
	}

	/**
	 * Returns the invocation data access service for this repository
	 * definition.
	 * 
	 * @return The invocation data access servie.
	 */
	public IInvocationDataAccessService getInvocationDataAccessService() {
		return invocationDataAccessService;
	}

	/**
	 * Returns the combined metrics data access service for this repository
	 * definition.
	 * 
	 * @return The combined metrics data access service.
	 */
	public ICombinedMetricsDataAccessService getCombinedMetricsDataAccessService() {
		return combinedMetricsDataAccessService;
	}

	/**
	 * Returns the server status service for this repository definition.
	 * 
	 * @return The server status service.
	 */
	public ServerStatusService getServerStatusService() {
		return serverStatusService;
	}

	/**
	 * Returns the license service for this repository definition.
	 * 
	 * @return The license service.
	 */
	public ILicenseService getLicenseService() {
		return licenseService;
	}

	/**
	 * Returns the IP of the definition.
	 * 
	 * @return The IP.
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Returns the port of the definition.
	 * 
	 * @return The port.
	 */
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