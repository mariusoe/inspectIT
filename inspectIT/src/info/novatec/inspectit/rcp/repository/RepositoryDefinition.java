package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.cmr.service.IConfigurationInterfaceDataAccessService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.rcp.repository.service.CachedGlobalDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cmr.ServerStatusService;

/**
 * The interface to the repository definition. A repository can be anywhere and
 * anything, the implementation will provide the details on how to access the
 * information.
 * 
 * @author Patrice Bouillet
 */
public interface RepositoryDefinition {

	/**
	 * Returns the IP of the definition.
	 * 
	 * @return The IP.
	 */
	String getIp();

	/**
	 * Returns the port of the definition.
	 * 
	 * @return The port.
	 */
	int getPort();

	/**
	 * Returns the license service for this repository definition.
	 * 
	 * @return The license service.
	 */
	ILicenseService getLicenseService();

	/**
	 * Returns the server status service for this repository definition.
	 * 
	 * @return The server status service.
	 */
	ServerStatusService getServerStatusService();

	/**
	 * Returns the combined metrics data access service for this repository
	 * definition.
	 * 
	 * @return The combined metrics data access service.
	 */
	ICombinedMetricsDataAccessService getCombinedMetricsDataAccessService();

	/**
	 * Returns the invocation data access service for this repository
	 * definition.
	 * 
	 * @return The invocation data access servie.
	 */
	IInvocationDataAccessService getInvocationDataAccessService();

	/**
	 * Returns the sql data access service for this repository definition.
	 * 
	 * @return The sql data access service.
	 */
	ISqlDataAccessService getSqlDataAccessService();

	/**
	 * Returns the exception data access service for this repository definition.
	 * 
	 * @return The exception data access service.
	 */
	IExceptionDataAccessService getExceptionDataAccessService();

	/**
	 * Returns the global data access service for this repository definition.
	 * 
	 * @return The global data access service.
	 */
	CachedGlobalDataAccessService getGlobalDataAccessService();
	
	/**
	 * Returns the configuration interface data access service for this repository definition.
	 * 
	 * @return The configuration data access service.
	 */
	IConfigurationInterfaceDataAccessService getConfigurationInterfaceDataAccessService();

}