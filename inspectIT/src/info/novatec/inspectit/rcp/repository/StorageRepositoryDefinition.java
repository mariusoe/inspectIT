package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.cmr.service.IServerStatusService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.rcp.repository.service.storage.StorageServiceProvider;
import info.novatec.inspectit.storage.LocalStorageData;

import java.util.List;

/**
 * Storage repository definition. This {@link RepositoryDefinition} has a direct usage of a
 * {@link CmrRepositoryDefinition} where storage is located.
 *
 * @author Ivan Senic
 *
 */
public class StorageRepositoryDefinition implements RepositoryDefinition {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link LocalStorageData}.
	 */
	private LocalStorageData localStorageData;

	/**
	 * {@link IInvocationDataAccessService} service.
	 */
	private IInvocationDataAccessService invocationDataAccessService;

	/**
	 * {@link IGlobalDataAccessService} service.
	 */
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Caching component.
	 */
	private CachedDataService cachedDataService;

	/**
	 * {@link IExceptionDataAccessService}.
	 */
	private IExceptionDataAccessService exceptionDataAccessService;

	/**
	 * {@link ISqlDataAccessService}.
	 */
	private ISqlDataAccessService sqlDataAccessService;

	/**
	 * {@link ITimerDataAccessService}.
	 */
	private ITimerDataAccessService timerDataAccessService;

	/**
	 * {@link IHttpTimerDataAccessService}.
	 */
	private IHttpTimerDataAccessService httpTimerDataAccessService;

	/**
	 * {@link StorageServiceProvider} for instantiating storage services.
	 */
	private StorageServiceProvider storageServiceProvider;

	/**
	 * Indexing tree for storage.
	 */
	private IStorageTreeComponent<? extends DefaultData> indexingTree;

	/**
	 * Involved agents.
	 */
	private List<PlatformIdent> agents;

	/**
	 * {@inheritDoc}
	 */
	public String getIp() {
		return cmrRepositoryDefinition.getIp();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPort() {
		return cmrRepositoryDefinition.getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return localStorageData.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public ILicenseService getLicenseService() {
		return cmrRepositoryDefinition.getLicenseService();
	}

	/**
	 * {@inheritDoc}
	 */
	public IServerStatusService getServerStatusService() {
		return cmrRepositoryDefinition.getServerStatusService();
	}

	/**
	 * {@inheritDoc}
	 */
	public ICombinedMetricsDataAccessService getCombinedMetricsDataAccessService() {
		throw new UnsupportedOperationException("CombinedMetricsDataAccessService not available with Storage repository.");
	}

	/**
	 * {@inheritDoc}
	 */
	public IInvocationDataAccessService getInvocationDataAccessService() {
		return invocationDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISqlDataAccessService getSqlDataAccessService() {
		return sqlDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	public IExceptionDataAccessService getExceptionDataAccessService() {
		return exceptionDataAccessService;
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
	public CachedDataService getCachedDataService() {
		return cachedDataService;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITimerDataAccessService getTimerDataAccessService() {
		return timerDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHttpTimerDataAccessService getHttpTimerDataAccessService() {
		return httpTimerDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void initServices() {
		// init services
		globalDataAccessService = storageServiceProvider.createStorageGlobalDataAccessService(cmrRepositoryDefinition, localStorageData, (IStorageTreeComponent<DefaultData>) indexingTree, agents);
		exceptionDataAccessService = storageServiceProvider.createStorageExceptionDataAccessService(cmrRepositoryDefinition, localStorageData,
				(IStorageTreeComponent<ExceptionSensorData>) indexingTree);
		invocationDataAccessService = storageServiceProvider.createStorageInvocationDataAccessService(cmrRepositoryDefinition, localStorageData,
				(IStorageTreeComponent<InvocationSequenceData>) indexingTree);
		sqlDataAccessService = storageServiceProvider.createStorageSqlDataAccessService(cmrRepositoryDefinition, localStorageData, (IStorageTreeComponent<SqlStatementData>) indexingTree);
		timerDataAccessService = storageServiceProvider.createStorageTimerDataAccessService(cmrRepositoryDefinition, localStorageData, (IStorageTreeComponent<TimerData>) indexingTree);
		httpTimerDataAccessService = storageServiceProvider.createStorageHttpTimerDataAccessService(cmrRepositoryDefinition, localStorageData, (IStorageTreeComponent<HttpTimerData>) indexingTree);
		cachedDataService = new CachedDataService(globalDataAccessService);
	}

	/**
	 * @return the cmrRepositoryDefinition
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * @param cmrRepositoryDefinition
	 *            the cmrRepositoryDefinition to set
	 */
	public void setCmrRepositoryDefinition(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * @return the storageData
	 */
	public LocalStorageData getLocalStorageData() {
		return localStorageData;
	}

	/**
	 * @param localStorageData
	 *            the storageData to set
	 */
	public void setLocalStorageData(LocalStorageData localStorageData) {
		this.localStorageData = localStorageData;
	}

	/**
	 * @param storageServiceProvider
	 *            the storageServiceProvider to set
	 */
	public void setStorageServiceProvider(StorageServiceProvider storageServiceProvider) {
		this.storageServiceProvider = storageServiceProvider;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<? extends DefaultData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param agents
	 *            the agents to set
	 */
	public void setAgents(List<PlatformIdent> agents) {
		this.agents = agents;
	}

}
