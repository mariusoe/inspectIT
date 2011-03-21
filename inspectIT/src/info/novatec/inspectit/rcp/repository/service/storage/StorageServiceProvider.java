package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.storage.IStorageIdProvider;

import java.util.List;

/**
 * Provider of all storage related services. This classes correctly initialize the service with help
 * of Spring.
 *
 * @author Ivan Senic
 *
 */
public abstract class StorageServiceProvider {

	/**
	 * @return Spring created {@link StorageTimerDataAccessService}.
	 */
	protected abstract StorageTimerDataAccessService createStorageTimerDataAccessService();

	/**
	 * Properly initialized {@link StorageTimerDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            CMR where storage is located.
	 * @param storageIdProvider
	 *            {@link StorageIdProvider}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageTimerDataAccessService}.
	 */
	public StorageTimerDataAccessService createStorageTimerDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageIdProvider storageIdProvider,
			IStorageTreeComponent<TimerData> storageTreeComponent) {
		StorageTimerDataAccessService storageTimerDataService = createStorageTimerDataAccessService();
		storageTimerDataService.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageTimerDataService.setStorageIdProvider(storageIdProvider);
		storageTimerDataService.setIndexingTree(storageTreeComponent);
		return storageTimerDataService;
	}

	/**
	 * @return Spring created {@link StorageHttpTimerDataAccessService}.
	 */
	protected abstract StorageHttpTimerDataAccessService createStorageHttpTimerDataAccessService();

	/**
	 * Properly initialized {@link StorageHttpTimerDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            CMR where storage is located.
	 * @param storageIdProvider
	 *            {@link StorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageHttpTimerDataAccessService}.
	 */
	public StorageHttpTimerDataAccessService createStorageHttpTimerDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageIdProvider storageIdProvider,
			IStorageTreeComponent<HttpTimerData> storageTreeComponent) {
		StorageHttpTimerDataAccessService storageHttpTimerDataService = createStorageHttpTimerDataAccessService();
		storageHttpTimerDataService.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageHttpTimerDataService.setStorageIdProvider(storageIdProvider);
		storageHttpTimerDataService.setIndexingTree(storageTreeComponent);
		return storageHttpTimerDataService;
	}

	/**
	 * @return Spring created {@link StorageSqlDataAccessService}.
	 */
	protected abstract StorageSqlDataAccessService createStorageSqlDataAccessService();

	/**
	 * Properly initialized {@link StorageSqlDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            CMR where storage is located.
	 * @param storageIdProvider
	 *            {@link StorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageSqlDataAccessService}.
	 */
	public StorageSqlDataAccessService createStorageSqlDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageIdProvider storageIdProvider,
			IStorageTreeComponent<SqlStatementData> storageTreeComponent) {
		StorageSqlDataAccessService storageSqlDataAccessService = createStorageSqlDataAccessService();
		storageSqlDataAccessService.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageSqlDataAccessService.setStorageIdProvider(storageIdProvider);
		storageSqlDataAccessService.setIndexingTree(storageTreeComponent);
		return storageSqlDataAccessService;
	}

	/**
	 * @return Spring created {@link StorageExceptionDataAccessService}.
	 */
	protected abstract StorageExceptionDataAccessService createStorageExceptionDataAccessService();

	/**
	 * Properly initialized {@link StorageExceptionDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            CMR where storage is located.
	 * @param storageIdProvider
	 *            {@link StorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageExceptionDataAccessService}.
	 */
	public StorageExceptionDataAccessService createStorageExceptionDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageIdProvider storageIdProvider,
			IStorageTreeComponent<ExceptionSensorData> storageTreeComponent) {
		StorageExceptionDataAccessService storageExceptionDataAccessService = createStorageExceptionDataAccessService();
		storageExceptionDataAccessService.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageExceptionDataAccessService.setStorageIdProvider(storageIdProvider);
		storageExceptionDataAccessService.setIndexingTree(storageTreeComponent);
		return storageExceptionDataAccessService;
	}

	/**
	 * @return Spring created {@link StorageInvocationDataAccessService}.
	 */
	protected abstract StorageInvocationDataAccessService createStorageInvocationDataAccessService();

	/**
	 * Properly initialized {@link StorageInvocationDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            CMR where storage is located.
	 * @param storageIdProvider
	 *            {@link StorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageInvocationDataAccessService}.
	 */
	public StorageInvocationDataAccessService createStorageInvocationDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageIdProvider storageIdProvider,
			IStorageTreeComponent<InvocationSequenceData> storageTreeComponent) {
		StorageInvocationDataAccessService storageExceptionDataAccessService = createStorageInvocationDataAccessService();
		storageExceptionDataAccessService.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageExceptionDataAccessService.setStorageIdProvider(storageIdProvider);
		storageExceptionDataAccessService.setIndexingTree(storageTreeComponent);
		return storageExceptionDataAccessService;
	}

	/**
	 * @return Spring created {@link StorageGlobalDataAccessService}.
	 */
	protected abstract StorageGlobalDataAccessService createStorageGlobalDataAccessService();

	/**
	 * Properly initialized {@link StorageGlobalDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            CMR where storage is located.
	 * @param storageIdProvider
	 *            {@link StorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @param platformIdents
	 *            Agents related to storage.
	 * @return Properly initialized {@link StorageGlobalDataAccessService}.
	 */
	public StorageGlobalDataAccessService createStorageGlobalDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageIdProvider storageIdProvider,
			IStorageTreeComponent<DefaultData> storageTreeComponent, List<PlatformIdent> platformIdents) {
		StorageGlobalDataAccessService storageGlobalCachedDataAccessService = createStorageGlobalDataAccessService();
		storageGlobalCachedDataAccessService.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageGlobalCachedDataAccessService.setStorageIdProvider(storageIdProvider);
		storageGlobalCachedDataAccessService.setIndexingTree(storageTreeComponent);
		storageGlobalCachedDataAccessService.setAgents(platformIdents);
		return storageGlobalCachedDataAccessService;
	}

}
