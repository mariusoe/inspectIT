package info.novatec.inspectit.rcp.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.ArrayBasedStorageLeaf;
import info.novatec.inspectit.indexing.storage.impl.CombinedStorageBranch;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.util.HttpDataRetriever;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.storage.IStorageIdProvider;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.StorageFileExtensions;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.mutable.MutableObject;

/**
 * {@link StorageManager} for GUI.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class InspectITStorageManager extends StorageManager implements CmrRepositoryChangeListener {

	/**
	 * Map of mounted and online not available storages.
	 */
	private List<LocalStorageData> mountedNotAvailableStorages = Collections.synchronizedList(new ArrayList<LocalStorageData>());

	/**
	 * Map of mounted and online not available storages.
	 */
	private Map<LocalStorageData, CmrRepositoryDefinition> mountedAvailableStorages = new ConcurrentHashMap<LocalStorageData, CmrRepositoryDefinition>();

	/**
	 * Cashed statuses of CMR repository definitions.
	 */
	private ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus> cachedRepositoriesStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/**
	 * {@link HttpDataRetriever}.
	 */
	private HttpDataRetriever httpDataRetriever;

	/**
	 * Returns Spring instantiated {@link StorageRepositoryDefinition}.
	 * 
	 * @return Spring instantiated {@link StorageRepositoryDefinition}.
	 */
	protected abstract StorageRepositoryDefinition createStorageRepositoryDefinition();

	/**
	 * Mounts a new storage locally.
	 * 
	 * @param storageData
	 *            Storage to mount.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param fullyDownload
	 *            Should storage be immediately fully down-loaded. Intended for future use.
	 * @throws Exception
	 *             If mount fails.
	 */
	public void mountStorage(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition, boolean fullyDownload) throws Exception {
		LocalStorageData localStorageData = new LocalStorageData(storageData);

		Path directory = getStoragePath(localStorageData);
		if (!Files.exists(directory)) {
			try {
				Files.createDirectories(directory);
			} catch (IOException e) {
				throw new StorageException("Could not create local storage directory.", e);
			}
		}

		if (!httpDataRetriever.downloadAndSavePlatformIdents(cmrRepositoryDefinition, storageData, directory)) {
			deleteLocalStorageData(localStorageData);
			throw new StorageException("Could not download and save agent information for local storage.");
		}

		if (!httpDataRetriever.downloadAndSaveIndexingTrees(cmrRepositoryDefinition, storageData, directory)) {
			deleteLocalStorageData(localStorageData);
			throw new StorageException("Could not download and save idnexing trees for local storage.");
		}

		if (fullyDownload) {
			localStorageData.setFullyDownloaded(true);
			// TODO full download!!!
		}

		try {
			writeLocalStorageDataToDisk(localStorageData);
		} catch (Exception e) {
			throw new StorageException("Could save local storage information to disk.", e);
		}

		try {
			String systemUserName = getSystemUsername();
			if (null != systemUserName) {
				StringStorageLabel mountedByLabel = new StringStorageLabel(systemUserName, new ExploredByLabelType());
				cmrRepositoryDefinition.getStorageService().addLabelToStorage(storageData, mountedByLabel, true);
			}
		} catch (Exception e) {
			// ignore
		}

		mountedAvailableStorages.put(localStorageData, cmrRepositoryDefinition);
	}

	/**
	 * Deletes all local data saved for given {@link LocalStorageData}, unmount storage.
	 * 
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @throws IOException
	 *             If deleting of the local data fails.
	 */
	private void deleteLocalStorageData(LocalStorageData localStorageData) throws IOException {
		this.deleteCompleteStorageDataFromDisk(localStorageData);
		mountedAvailableStorages.remove(localStorageData);
		mountedNotAvailableStorages.remove(localStorageData);
	}

	/**
	 * Informs the {@link InspectITStorageManager} that a {@link StorageData} has been remotly
	 * deleted.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 * @throws Exception
	 *             If deleting of the local data fails.
	 */
	public void storageRemotlyDeleted(StorageData storageData) throws Exception {
		LocalStorageData localStorageData = null;
		for (Map.Entry<LocalStorageData, CmrRepositoryDefinition> entry : mountedAvailableStorages.entrySet()) {
			if (ObjectUtils.equals(entry.getKey().getId(), storageData.getId())) {
				localStorageData = entry.getKey();
				break;
			}
		}
		if (null != localStorageData && !localStorageData.isFullyDownloaded()) {
			deleteLocalStorageData(localStorageData);
		} else {
			for (LocalStorageData notAvailable : mountedNotAvailableStorages) {
				if (ObjectUtils.equals(notAvailable.getId(), storageData.getId())) {
					localStorageData = notAvailable;
					break;
				}
			}
			if (null != localStorageData && !localStorageData.isFullyDownloaded()) {
				deleteLocalStorageData(localStorageData);
			}
		}
	}

	/**
	 * Returns mounted and available storages, thus the ones that can be read from.
	 * 
	 * @return List of {@link LocalStorageData}.
	 */
	public Collection<LocalStorageData> getMountedAvailableStorages() {
		return Collections.unmodifiableSet(mountedAvailableStorages.keySet());
	}

	/**
	 * Returns mounted but not available storages, thus the ones that can not be read from because
	 * there is no CMR that can handle them.
	 * 
	 * @return List of {@link LocalStorageData}.
	 */
	public Collection<LocalStorageData> getMountedUnavailableStorages() {
		return Collections.unmodifiableList(mountedNotAvailableStorages);
	}

	/**
	 * Returns storage {@link Path}.
	 * 
	 * @param storageIdProvider
	 *            Storage.
	 * @return Returns storage {@link Path}.
	 * @see Paths#get(String, String...)
	 */
	public Path getStoragePath(IStorageIdProvider storageIdProvider) {
		return Paths.get(getStorageDefaultFolder(), storageIdProvider.getStorageFolder());
	}

	/**
	 * Loads initial local mounted storage information.
	 */
	public void startUp() {
		List<LocalStorageData> mountedStorages;
		try {
			mountedStorages = getMountedStoragesFromDisk();
		} catch (Exception e) {
			mountedStorages = Collections.emptyList();
		}
		Map<StorageData, CmrRepositoryDefinition> onlineStorages = getOnlineStorages();

		for (LocalStorageData localStorageData : mountedStorages) {
			if (localStorageData.isFullyDownloaded()) {
				mountedAvailableStorages.put(localStorageData, null);
			} else {
				boolean availableOnline = false;
				for (Map.Entry<StorageData, CmrRepositoryDefinition> entry : onlineStorages.entrySet()) {
					if (ObjectUtils.equals(entry.getKey().getId(), localStorageData.getId())) {
						availableOnline = true;
						mountedAvailableStorages.put(localStorageData, entry.getValue());
						break;
					}
				}
				if (!availableOnline) {
					mountedNotAvailableStorages.add(localStorageData);
				}
			}
		}

		InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryChangeListener(this);
	}

	/**
	 * Instantiates the new storage repository definition based on the {@link LocalStorageData}
	 * provided. Note that the local storage data has to be in the collection of
	 * {@link #getMountedAvailableStorages()}. Otherwise the creation will fail with an Exception
	 * being thrown..
	 * <p>
	 * A special care needs to be taken with the method, because the the returned object could be
	 * quite big, since it will hold the complete indexing tree. Thus, it is important that the
	 * caller of this method take responsibility to make earlier created definitions ready for
	 * garbage collection as soon as they are not needed anymore.
	 * 
	 * @param localStorageData
	 *            {@link LocalStorageData} to create the definition for.
	 * @return {@link StorageRepositoryDefinition}.
	 * @throws Exception
	 *             If the wanted {@link LocalStorageData} is not available. If any exception occurs
	 *             during definition creation.
	 */
	public StorageRepositoryDefinition getStorageRepositoryDefinition(LocalStorageData localStorageData) throws Exception {
		// check if it is available
		if (!mountedAvailableStorages.keySet().contains(localStorageData)) {
			throw new StorageException("The storage is not fully downloaded, and it's repository could not be found. The Storage repository definition could not be created.");
		}

		// find CMR repository def
		CmrRepositoryDefinition cmrRepositoryDefinition = mountedAvailableStorages.get(localStorageData);

		// get agents
		List<PlatformIdent> platformIdents = getPlatformIdentsLocally(localStorageData);
		if (null == platformIdents) {
			platformIdents = Collections.emptyList();
		}

		// get indexing tree
		IStorageTreeComponent<? extends DefaultData> indexingTree = getIndexingTree(localStorageData);
		if (null == indexingTree) {
			indexingTree = new ArrayBasedStorageLeaf<DefaultData>();
		}

		// create new storage repository definition
		StorageRepositoryDefinition storageRepositoryDefinition = createStorageRepositoryDefinition();
		storageRepositoryDefinition.setAgents(platformIdents);
		storageRepositoryDefinition.setIndexingTree(indexingTree);
		storageRepositoryDefinition.setCmrRepositoryDefinition(cmrRepositoryDefinition);
		storageRepositoryDefinition.setLocalStorageData(localStorageData);
		storageRepositoryDefinition.initServices();
		return storageRepositoryDefinition;
	}

	/**
	 * Checks if the storage is locally mounted.
	 * 
	 * @param storageData
	 *            Storage data to check.
	 * @return True if storage is mounted, false otherwise.
	 */
	public boolean isStorageMounted(StorageData storageData) {
		return getLocalDataForStorage(storageData) != null;
	}

	/**
	 * Returns the local data for storage if the storage is mounted.
	 * 
	 * @param storageData
	 *            Storage data to check.
	 * @return {@link LocalStorageData}.
	 */
	public LocalStorageData getLocalDataForStorage(StorageData storageData) {
		for (LocalStorageData mountedStorage : mountedNotAvailableStorages) {
			if (ObjectUtils.equals(storageData.getId(), mountedStorage.getId())) {
				return mountedStorage;
			}
		}
		for (LocalStorageData mountedStorage : mountedAvailableStorages.keySet()) {
			if (ObjectUtils.equals(storageData.getId(), mountedStorage.getId())) {
				return mountedStorage;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.addMountedStorages(cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.removeMountedStorages(cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition cmrRepositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus != OnlineStatus.CHECKING) {
			OnlineStatus cachedStatus = cachedRepositoriesStatus.get(cmrRepositoryDefinition);
			if (!ObjectUtils.equals(cachedStatus, newStatus)) {
				if (newStatus == OnlineStatus.ONLINE) {
					this.addMountedStorages(cmrRepositoryDefinition);
				} else if (newStatus == OnlineStatus.OFFLINE) {
					this.removeMountedStorages(cmrRepositoryDefinition);
				}
			}
			cachedRepositoriesStatus.put(cmrRepositoryDefinition, newStatus);
		}
	}

	/**
	 * Adds mounted storages that are bounded to the given {@link CmrRepositoryDefinition} to
	 * "available" map.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	private void addMountedStorages(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
			List<StorageData> closedStorages = cmrRepositoryDefinition.getStorageService().getReadableStorages();
			List<LocalStorageData> newAvailableStoarges = new ArrayList<LocalStorageData>();
			for (LocalStorageData localStorageData : mountedNotAvailableStorages) {
				for (StorageData storageData : closedStorages) {
					if (ObjectUtils.equals(localStorageData.getId(), storageData.getId())) {
						newAvailableStoarges.add(localStorageData);
						mountedAvailableStorages.put(localStorageData, cmrRepositoryDefinition);
						break;
					}
				}
			}

			if (!newAvailableStoarges.isEmpty()) {
				mountedNotAvailableStorages.removeAll(newAvailableStoarges);
			}
		}
	}

	/**
	 * Removes mounted storages that are bounded to the given {@link CmrRepositoryDefinition} to
	 * "available" map.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	private void removeMountedStorages(CmrRepositoryDefinition cmrRepositoryDefinition) {
		List<LocalStorageData> removeList = new ArrayList<LocalStorageData>();
		for (Map.Entry<LocalStorageData, CmrRepositoryDefinition> entry : mountedAvailableStorages.entrySet()) {
			if (!entry.getKey().isFullyDownloaded()) {
				if (ObjectUtils.equals(entry.getValue(), cmrRepositoryDefinition)) {
					removeList.add(entry.getKey());
				}
			}
		}
		if (!removeList.isEmpty()) {
			mountedAvailableStorages.keySet().removeAll(removeList);
			mountedNotAvailableStorages.addAll(removeList);
		}
	}

	/**
	 * Loads {@link PlatformIdent}s from a disk for a storage.
	 * 
	 * @param storageIdProvider
	 *            {@link IStorageIdProvider}
	 * @return List of {@link PlatformIdent}s involved in the storage data or null if no file
	 *         exists.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 */
	private List<PlatformIdent> getPlatformIdentsLocally(final IStorageIdProvider storageIdProvider) throws IOException, SerializationException {
		Path storagePath = getStoragePath(storageIdProvider);
		List<PlatformIdent> returnList = this.getObjectsByFileTreeWalk(storagePath, StorageFileExtensions.AGENT_FILE_EXT);
		if (!returnList.isEmpty()) {
			return returnList;
		} else {
			return null;
		}
	}

	/**
	 * Loads indexing tree from a disk for a storage.
	 * 
	 * @param storageIdProvider
	 *            {@link IStorageIdProvider}
	 * @return Indexing tree or null if it can not be found.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 */
	private IStorageTreeComponent<DefaultData> getIndexingTree(final IStorageIdProvider storageIdProvider) throws IOException, SerializationException {
		Path storagePath = getStoragePath(storageIdProvider);
		List<IStorageTreeComponent<DefaultData>> indexingTrees = this.getObjectsByFileTreeWalk(storagePath, StorageFileExtensions.INDEX_FILE_EXT);
		if (!indexingTrees.isEmpty()) {
			if (indexingTrees.size() == 1) {
				return indexingTrees.get(0);
			} else {
				CombinedStorageBranch<DefaultData> combinedStorageBranch = new CombinedStorageBranch<DefaultData>(indexingTrees);
				return combinedStorageBranch;
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns all storages that have been mounted locally.
	 * 
	 * @return Returns all storages that have been mounted locally as a list of
	 *         {@link LocalStorageData}.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 */
	private List<LocalStorageData> getMountedStoragesFromDisk() throws IOException, SerializationException {
		Path defaultDirectory = Paths.get(getStorageDefaultFolder());
		return this.getObjectsByFileTreeWalk(defaultDirectory, StorageFileExtensions.LOCAL_STORAGE_FILE_EXT);
	}

	/**
	 * Reads the objects from files that are in a given path or sub-paths. Note that generic can be
	 * used to specify the wanted class. How ever, if the object loaded from a file is not of a
	 * wanted class, {@link ClassCastException} will be thrown as usual.
	 * 
	 * @param <E>
	 *            Wanted type. Use object if it is uncertain what types object will be.
	 * @param path
	 *            {@link Path} to look in.
	 * @param fileSufix
	 *            Ending of the files (extension).
	 * @return List of deserialized objects.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If data can not be deserialized.
	 * 
	 */
	private <E> List<E> getObjectsByFileTreeWalk(Path path, final String fileSufix) throws IOException, SerializationException {
		if (!Files.isDirectory(path)) {
			return Collections.emptyList();
		}

		final MutableObject mutableException = new MutableObject();
		final List<E> returnList = new ArrayList<E>();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			@SuppressWarnings("unchecked")
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().endsWith(fileSufix)) {
					try {
						byte[] bytes = Files.readAllBytes(file);
						ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
						Object deserialized = getSerializer().deserialize(byteBuffer);
						returnList.add((E) deserialized);
					} catch (SerializationException e) {
						mutableException.setValue(e);
						return FileVisitResult.TERMINATE;
					}

				}
				return FileVisitResult.CONTINUE;
			}
		});

		SerializationException serializationException = (SerializationException) mutableException.getValue();
		if (null != serializationException) {
			throw serializationException;
		} else {
			return returnList;
		}
	}

	/**
	 * Returns map of online available storages with their {@link CmrRepositoryDefinition} as a
	 * value.
	 * 
	 * @return Map of online available storages with their {@link CmrRepositoryDefinition} as a
	 *         value.
	 */
	private Map<StorageData, CmrRepositoryDefinition> getOnlineStorages() {
		Map<StorageData, CmrRepositoryDefinition> storageMap = new HashMap<StorageData, CmrRepositoryDefinition>();
		List<CmrRepositoryDefinition> allRepositories = InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : allRepositories) {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				List<StorageData> closedStorages = cmrRepositoryDefinition.getStorageService().getReadableStorages();
				if (null != closedStorages) {
					for (StorageData storageData : closedStorages) {
						storageMap.put(storageData, cmrRepositoryDefinition);
					}
				}
			}
		}
		return storageMap;
	}

	/**
	 * 
	 * @return Returns the system username.
	 */
	private String getSystemUsername() {
		return System.getProperty("user.name");
	}

	/**
	 * @param httpDataRetriever
	 *            the httpDataRetriever to set
	 */
	public void setHttpDataRetriever(HttpDataRetriever httpDataRetriever) {
		this.httpDataRetriever = httpDataRetriever;
	}

}
