package info.novatec.inspectit.rcp.repository.service.cmr;

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
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * Provider of the {@link ICmrService}s via Spring.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class CmrServiceProvider {

	/**
	 * Returns properly initialized {@link BufferService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link BufferService}.
	 */
	public IBufferService getBufferService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IBufferService bufferService = getBufferService();
		((ICmrService) bufferService).initService(cmrRepositoryDefinition);
		return bufferService;
	}

	/**
	 * Returns Spring created {@link BufferService}.
	 * 
	 * @return Returns Spring created {@link BufferService}.
	 */
	protected abstract IBufferService getBufferService();

	/**
	 * Returns properly initialized {@link CombinedMetricsDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link CombinedMetricsDataAccessService}.
	 */
	public ICombinedMetricsDataAccessService getCombinedMetricsDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		ICombinedMetricsDataAccessService combinedMetricsDataAccessService = getCombinedMetricsDataAccessService();
		((ICmrService) combinedMetricsDataAccessService).initService(cmrRepositoryDefinition);
		return combinedMetricsDataAccessService;
	}

	/**
	 * Returns Spring created {@link CombinedMetricsDataAccessService}.
	 * 
	 * @return Returns Spring created {@link CombinedMetricsDataAccessService}.
	 */
	protected abstract ICombinedMetricsDataAccessService getCombinedMetricsDataAccessService();

	/**
	 * Returns properly initialized {@link ExceptionDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link ExceptionDataAccessService}.
	 */
	public IExceptionDataAccessService getExceptionDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IExceptionDataAccessService exceptionDataAccessService = getExceptionDataAccessService();
		((ICmrService) exceptionDataAccessService).initService(cmrRepositoryDefinition);
		return exceptionDataAccessService;
	}

	/**
	 * Returns Spring created {@link ExceptionDataAccessService}.
	 * 
	 * @return Returns Spring created {@link ExceptionDataAccessService}.
	 */
	protected abstract IExceptionDataAccessService getExceptionDataAccessService();

	/**
	 * Returns properly initialized {@link GlobalDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link GlobalDataAccessService}.
	 */
	public IGlobalDataAccessService getGlobalDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IGlobalDataAccessService globalDataAccessService = getGlobalDataAccessService();
		((ICmrService) globalDataAccessService).initService(cmrRepositoryDefinition);
		return globalDataAccessService;
	}

	/**
	 * Returns Spring created {@link GlobalDataAccessService}.
	 * 
	 * @return Returns Spring created {@link GlobalDataAccessService}.
	 */
	protected abstract IGlobalDataAccessService getGlobalDataAccessService();

	/**
	 * Returns properly initialized {@link InvocationDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link InvocationDataAccessService}.
	 */
	public IInvocationDataAccessService getInvocationDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IInvocationDataAccessService invocationDataAccessService = getInvocationDataAccessService();
		((ICmrService) invocationDataAccessService).initService(cmrRepositoryDefinition);
		return invocationDataAccessService;
	}

	/**
	 * Returns Spring created {@link InvocationDataAccessService}.
	 * 
	 * @return Returns Spring created {@link InvocationDataAccessService}.
	 */
	protected abstract IInvocationDataAccessService getInvocationDataAccessService();

	/**
	 * Returns properly initialized {@link LicenseService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link LicenseService}.
	 */
	public ILicenseService getLicenseService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		ILicenseService licenceService = getLicenseService();
		((ICmrService) licenceService).initService(cmrRepositoryDefinition);
		return licenceService;
	}

	/**
	 * Returns Spring created {@link LicenseService}.
	 * 
	 * @return Returns Spring created {@link LicenseService}.
	 */
	protected abstract ILicenseService getLicenseService();

	/**
	 * Returns properly initialized {@link ServerStatusService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link ServerStatusService}.
	 */
	public IServerStatusService getServerStatusService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IServerStatusService serverStatusService = getServerStatusService();
		((ICmrService) serverStatusService).initService(cmrRepositoryDefinition);
		return serverStatusService;
	}

	/**
	 * Returns Spring created {@link ServerStatusService}.
	 * 
	 * @return Returns Spring created {@link ServerStatusService}.
	 */
	protected abstract IServerStatusService getServerStatusService();

	/**
	 * Returns properly initialized {@link SqlDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link SqlDataAccessService}.
	 */
	public ISqlDataAccessService getSqlDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		ISqlDataAccessService sqlDataAccessService = getSqlDataAccessService();
		((ICmrService) sqlDataAccessService).initService(cmrRepositoryDefinition);
		return sqlDataAccessService;
	}

	/**
	 * Returns Spring created {@link SqlDataAccessService}.
	 * 
	 * @return Returns Spring created {@link SqlDataAccessService}.
	 */
	protected abstract ISqlDataAccessService getSqlDataAccessService();

	/**
	 * Returns properly initialized {@link TimerDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link TimerDataAccessService}.
	 */
	public ITimerDataAccessService getTimerDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		ITimerDataAccessService timerDataAccessService = getTimerDataAccessService();
		((ICmrService) timerDataAccessService).initService(cmrRepositoryDefinition);
		return timerDataAccessService;
	}

	/**
	 * Returns Spring created {@link TimerDataAccessService}.
	 * 
	 * @return Returns Spring created {@link TimerDataAccessService}.
	 */
	protected abstract ITimerDataAccessService getTimerDataAccessService();

	/**
	 * Returns properly initialized {@link TimerDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link TimerDataAccessService}.
	 */
	public IHttpTimerDataAccessService getHttpTimerDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IHttpTimerDataAccessService httpTimerDataAccessService = getHttpTimerDataAccessService();
		((ICmrService) httpTimerDataAccessService).initService(cmrRepositoryDefinition);
		return httpTimerDataAccessService;
	}

	/**
	 * Returns Spring created {@link TimerDataAccessService}.
	 * 
	 * @return Returns Spring created {@link TimerDataAccessService}.
	 */
	protected abstract IHttpTimerDataAccessService getHttpTimerDataAccessService();

	/**
	 * Returns properly initialized {@link ConfigurationInterfaceDataAccessService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link ConfigurationInterfaceDataAccessService}.
	 */
	public IConfigurationInterfaceDataAccessService getConfigurationInterfaceDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IConfigurationInterfaceDataAccessService configurationInterfaceDataAccessService = getConfigurationInterfaceDataAccessService();
		((ICmrService) configurationInterfaceDataAccessService).initService(cmrRepositoryDefinition);
		return configurationInterfaceDataAccessService;
	}

	/**
	 * Returns Spring created {@link ConfigurationInterfaceDataAccessService}.
	 * 
	 * @return Returns Spring created {@link ConfigurationInterfaceDataAccessService}.
	 */
	protected abstract IConfigurationInterfaceDataAccessService getConfigurationInterfaceDataAccessService();

}
