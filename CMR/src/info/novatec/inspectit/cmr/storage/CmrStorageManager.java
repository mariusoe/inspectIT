package info.novatec.inspectit.cmr.storage;

import info.novatec.inspectit.cmr.dao.StorageDataDao;
import info.novatec.inspectit.cmr.dao.impl.DefaultDataDaoImpl;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.StorageFileExtensions;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.StorageRecorder;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.WritingStatus;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Storage manager for the CMR. Manages creation, opening and closing of storages, as well as
 * recording.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class CmrStorageManager extends StorageManager {

	/**
	 * {@link DefaultDataDaoImpl}.
	 */
	@Autowired
	private StorageDataDao storageDataDao;

	/**
	 * {@link StorageData} for currently active recorder.
	 */
	private volatile StorageData recorderStorageData = null;

	/**
	 * {@link StorageWriter} provider.
	 */
	@Autowired
	private CmrStorageWriterProvider storageWriterProvider;

	/**
	 * Opened storages and their writers.
	 */
	private Map<StorageData, StorageWriter> openedStoragesMap = new ConcurrentHashMap<StorageData, StorageWriter>(8, 0.75f, 1);

	/**
	 * Existing storages.
	 */
	private Set<StorageData> existingStoragesSet;

	/**
	 * {@link StorageRecorder} to deal with recording.
	 */
	@Autowired
	private StorageRecorder storageRecorder;

	/**
	 * Logger for buffer.
	 */
	private static final Logger LOGGER = Logger.getLogger(CmrStorageManager.class);

	/**
	 * Creates new storage.
	 * 
	 * @param storageData
	 *            Storage.
	 * @throws IOException
	 *             if {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	public void createStorage(StorageData storageData) throws IOException, SerializationException {
		storageData.setId(getRandomUUIDString());
		writeStorageDataToDisk(storageData);
		existingStoragesSet.add(storageData);
	}

	/**
	 * Opens existing storage if it is not already opened.
	 * 
	 * @param storageData
	 *            Storage to open.
	 * @return {@link StorageWriter} created for this storage. Of <code>null</code> if no new writer
	 *         is created.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If exception occurs during update of storage data.
	 */
	public StorageWriter openStorage(StorageData storageData) throws IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		synchronized (local) {
			if (isStorageExisting(storageData) && !isStorageOpen(storageData)) {
				local.markOpened();
				StorageWriter writer = storageWriterProvider.getCmrStorageWriter();
				openedStoragesMap.put(local, writer);
				writer.prepareForWrite(local);
				writeStorageDataToDisk(local);
				return writer;
			}
		}
		return null;
	}

	/**
	 * Closes the storage if it is open.
	 * 
	 * @param storageData
	 *            Storage.
	 * @throws StorageException
	 *             When storage that should be closed is used for recording.
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void closeStorage(StorageData storageData) throws StorageException, IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		synchronized (local) {
			StorageWriter writer = openedStoragesMap.get(local);
			if (writer != null) {
				if (writer == storageRecorder.getStorageWriter()) {
					throw new StorageException("Storage " + local + " can not be finalized because it is currenlty used for recording purposes.");
				}
				writer.closeStorageWriter();
			}
			openedStoragesMap.remove(local);
			local.setDiskSize(getDiskSizeForStorage(local));
			local.markClosed();
			writeStorageDataToDisk(local);
		}
	}

	/**
	 * Deletes a storage information and files from disk.
	 * 
	 * @param storageData
	 *            {@link StorageData} to delete.
	 * @throws StorageException
	 *             If storage is not closed.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public void deleteStorage(StorageData storageData) throws StorageException, IOException {
		StorageData local = getLocalStorageDataObject(storageData);
		synchronized (local) {
			if (local.isStorageOpened()) {
				throw new StorageException("Writable storages can not be deleted. Please finalize the storage first.");
			}
			deleteCompleteStorageDataFromDisk(local);
			existingStoragesSet.remove(local);
		}
	}

	/**
	 * @return Returns if the recording is currently active.
	 */
	public boolean isRecordingOn() {
		return storageRecorder.isRecordingOn();
	}

	/**
	 * If the recording is active, returns the storage that is used for storing recording data.
	 * 
	 * @return Storage that is used for recording, or null if recording is not active.
	 */
	public StorageData getRecordingStorage() {
		return recorderStorageData;
	}

	/**
	 * Returns the properties used for the current recording on the CMR.
	 * 
	 * @return {@link RecordingProperties} that are used for recording, or null if recording is not
	 *         active.
	 */
	public RecordingProperties getRecordingProperties() {
		return storageRecorder.getRecordingProperties();
	}

	/**
	 * Returns the {@link WritingStatus} for the storage that is currently used as a recording one
	 * or <code>null</code> if the recording is not active.
	 * 
	 * @return {@link WritingStatus} if recording is active. <code>Null</code> otherwise.
	 */
	public WritingStatus getRecordingStatus() {
		StorageWriter recordingStorageWriter = storageRecorder.getStorageWriter();
		if (null != recordingStorageWriter) {
			return recordingStorageWriter.getWritingStatus();
		} else {
			return null;
		}
	}

	/**
	 * Starts recording on the provided storage if recording is not active. If storage is not
	 * created it will be. If it is not open, it will be.
	 * 
	 * @param storageData
	 *            Storage.
	 * @param recordingProperties
	 *            Recording properties. Must not be null.
	 * @throws IOException
	 *             If {@link IOException} occurs while creating and opening the storage.
	 * @throws SerializationException
	 *             If serialization fails when creating the storage.
	 */
	public void startRecording(StorageData storageData, RecordingProperties recordingProperties) throws IOException, SerializationException {
		if (!isStorageExisting(storageData)) {
			this.createStorage(storageData);
		}
		StorageData local = getLocalStorageDataObject(storageData);
		if (!isStorageOpen(local)) {
			this.openStorage(local);
		}
		synchronized (this) {
			if (!isRecordingOn()) {
				StorageWriter storageWriter = openedStoragesMap.get(local);
				storageRecorder.startRecording(storageWriter, recordingProperties);
				recorderStorageData = local;
				recorderStorageData.markRecording();
				writeStorageDataToDisk(recorderStorageData);
			}
		}
	}

	/**
	 * Stops recording.
	 * 
	 * @throws SerializationException
	 *             If serialization fails during write {@link StorageData} to disk.
	 * @throws IOException
	 *             If IOException occurs during write {@link StorageData} to disk.
	 */
	public void stopRecording() throws IOException, SerializationException {
		synchronized (this) {
			if (isRecordingOn()) {
				storageRecorder.stopRecording();
				recorderStorageData.markOpened();
				writeStorageDataToDisk(recorderStorageData);
				recorderStorageData = null;
			}
		}
	}

	/**
	 * Writes one data to the recording storage.
	 * 
	 * @param dataToRecord
	 *            Data to write.
	 */
	public void record(DefaultData dataToRecord) {
		if (isRecordingOn() && canWriteMore()) {
			storageRecorder.record(dataToRecord);
		} else if (isRecordingOn() && !canWriteMore()) {
			try {
				stopRecording();
			} catch (Exception e) {
				LOGGER.warn("Exception occured trying to automatically stop the recording due to the hard disk space limitation warning.", e);
			}
		}
	}

	/**
	 * Writes collection of {@link DefaultData} objects to the storage.
	 * 
	 * @param storageData
	 *            Storage to write.
	 * @param dataToWrite
	 *            Data to write.
	 * @param dataProcessors
	 *            Processors that will be used for data writing. Can be null. In this case, the
	 *            direct write is done.
	 * @throws StorageException
	 *             If storage is used as a recording storage.
	 */
	public void writeToStorage(StorageData storageData, Collection<? extends DefaultData> dataToWrite, Collection<AbstractDataProcessor> dataProcessors) throws StorageException {
		StorageData local = getLocalStorageDataObject(storageData);
		StorageWriter writer = openedStoragesMap.get(local);
		if (writer != null) {
			writer.process(dataToWrite, dataProcessors);
		} else if (local.equals(recorderStorageData)) {
			throw new StorageException("Can not write to storage that is currenlty used as a recording storage.");
		} else if (local.getState() == StorageState.CLOSED) {
			throw new StorageException("Can not write to closed storage");
		}
	}

	/**
	 * Copies the content of the current CMR buffer to the Storage.
	 * 
	 * @param storageData
	 *            Storage to copy data to.
	 * @param platformIdents
	 *            List of agent IDs.
	 * @param dataProcessors
	 *            Processors that will be used for data writing.
	 * @throws StorageException
	 *             If storage is used as a recording storage.
	 * @throws SerializationException
	 *             If storage needs to be created, and serialization fails.
	 * @throws IOException
	 *             If IO exception occurs.
	 */
	public void copyBufferToStorage(StorageData storageData, List<Long> platformIdents, Collection<AbstractDataProcessor> dataProcessors) throws StorageException, IOException, SerializationException {
		if (!isStorageExisting(storageData)) {
			this.createStorage(storageData);
		}
		StorageData local = getLocalStorageDataObject(storageData);
		if (!isStorageOpen(local)) {
			this.openStorage(local);
		}

		for (Long platformId : platformIdents) {
			List<DefaultData> toWriteList = storageDataDao.getAllDefaultDataForAgent(platformId.longValue());
			this.writeToStorage(local, toWriteList, dataProcessors);
		}
	}

	/**
	 * Copies set of template data to storage. The storage does not have to be opened before action
	 * can be executed (storage will be created/opened first in this case)
	 * 
	 * @param storageData
	 *            {@link StorageData} to copy to.
	 * 
	 * @param copyDataList
	 *            List of data. Note that this list will be used as a template list. The data will
	 *            be first loaded from the buffer.
	 * @param dataProcessors
	 *            Processors to process the data. Can be null, then the data is only copied with no
	 *            processing.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails when storage needs to be created/opened.
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 */
	public void copyDataToStorage(StorageData storageData, List<DefaultData> copyDataList, Collection<AbstractDataProcessor> dataProcessors) throws IOException, SerializationException,
			StorageException {
		if (!isStorageExisting(storageData)) {
			this.createStorage(storageData);
		}
		StorageData local = getLocalStorageDataObject(storageData);
		if (!isStorageOpen(local)) {
			this.openStorage(local);
		}

		List<DefaultData> toWriteList = storageDataDao.getDataFromCopyTemplateList(copyDataList);
		this.writeToStorage(local, toWriteList, dataProcessors);
	}

	/**
	 * Closes all opened storages. This method should only be called when the CMR shutdown hook is
	 * activated to ensure that no data is lost.
	 * 
	 * @throws SerializationException
	 * @throws IOException
	 */
	protected void closeAllStorages() {
		if (isRecordingOn()) {
			try {
				stopRecording();
			} catch (Exception e) {
				LOGGER.warn("Recording storage could not be finalized during the CMR shut-down.", e);

			}
		}
		for (StorageData openedStorage : openedStoragesMap.keySet()) {
			try {
				this.closeStorage(openedStorage);
			} catch (Exception e) {
				LOGGER.warn("Storage " + openedStorage + " could not be finalized during the CMR shut-down.", e);
			}
		}
	}

	/**
	 * Returns the storage data based on the ID. This method can be helpful when the updated version
	 * of {@link StorageData} needs to be retrieved.
	 * 
	 * @param id
	 *            ID of storage.
	 * @return {@link StorageData}
	 */
	public StorageData getStorageData(String id) {
		for (StorageData storageData : existingStoragesSet) {
			if (storageData.getId().equals(id)) {
				return storageData;
			}
		}
		return null;
	}

	/**
	 * Returns list of existing storages.
	 * 
	 * @return Returns list of existing storages.
	 */
	public List<StorageData> getExistingStorages() {
		List<StorageData> list = new ArrayList<StorageData>();
		list.addAll(existingStoragesSet);
		return list;
	}

	/**
	 * Returns list of opened storages.
	 * 
	 * @return Returns list of opened storages.
	 */
	public List<StorageData> getOpenedStorages() {
		List<StorageData> list = new ArrayList<StorageData>();
		list.addAll(openedStoragesMap.keySet());
		return list;
	}

	/**
	 * Returns list of readable storages.
	 * 
	 * @return Returns list of readable storages.
	 */
	public List<StorageData> getReadableStorages() {
		List<StorageData> list = new ArrayList<StorageData>();
		for (StorageData storageData : existingStoragesSet) {
			if (storageData.isStorageClosed()) {
				list.add(storageData);
			}
		}
		return list;
	}

	/**
	 * Returns if the storage is opened, and thus if the write to the storage can be executed.
	 * 
	 * @param storageData
	 *            Storage to check.
	 * @return True if storage is opened, otherwise false.
	 */
	public boolean isStorageOpen(StorageData storageData) {
		for (StorageData existing : openedStoragesMap.keySet()) {
			if (existing.getId().equals(storageData.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if the storage is existing.
	 * 
	 * @param storageData
	 *            Storage to check.
	 * @return True if storage exists, otherwise false.
	 */
	public boolean isStorageExisting(StorageData storageData) {
		for (StorageData existing : existingStoragesSet) {
			if (existing.getId().equals(storageData.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the amount of writing tasks storage still has to process. Note that this is an
	 * approximate number.
	 * 
	 * @param storageData
	 *            Storage data to get information for.
	 * @return Returns number of queued tasks. Note that if the storage is not in writable mode
	 *         <code>0</code> will be returned.
	 */
	public long getStorageQueuedWriteTaskCount(StorageData storageData) {
		StorageData local = getLocalStorageDataObject(storageData);
		if (!isStorageOpen(local)) {
			return 0;
		}

		StorageWriter storageWriter = openedStoragesMap.get(local);
		if (null == storageWriter) {
			return 0;
		} else {
			return storageWriter.getQueuedTaskCount();
		}
	}

	/**
	 * Returns storage {@link Path}.
	 * 
	 * @param storageData
	 *            Storage.
	 * @return Returns storage {@link Path}.
	 * @see Paths#get(String, String...)
	 */
	public Path getStoragePath(IStorageData storageData) {
		return Paths.get(getStorageDefaultFolder(), storageData.getStorageFolder());
	}

	/**
	 * Returns the list of the string that represent the path to the index files for one storage.
	 * The paths are in form "/directory/file.extension". These paths can be used in combination to
	 * CMR's IP and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the IP localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 * 
	 * @param storageData
	 *            Storage to get index files for.
	 * @return Returns the list of the string that represent the path to the index files.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public List<String> getIndexFilesLocations(StorageData storageData) throws IOException {
		return getFilesHttpLocation(storageData, StorageFileExtensions.INDEX_FILE_EXT);
	}

	/**
	 * Returns the list of the string that represent the path to the data files for one storage. The
	 * paths are in form "/directory/file.extension". These paths can be used in combination to
	 * CMR's IP and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the IP localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 * 
	 * @param storageData
	 *            Storage to get index files for.
	 * @return Returns the list of the string that represent the path to the data files.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public List<String> getDataFilesLocations(StorageData storageData) throws IOException {
		return getFilesHttpLocation(storageData, StorageFileExtensions.DATA_FILE_EXTENSION);
	}

	/**
	 * Returns the list of the string that represent the path to the agent files for one storage.
	 * The paths are in form "/directory/file.extension". These paths can be used in combination to
	 * CMR's IP and port to get the files via HTTP.
	 * <p>
	 * For example, if the CMR has the IP localhost and port 8080, the address for the file would
	 * be: http://localhost:8080/directory/file.extension
	 * 
	 * @param storageData
	 *            Storage to get index files for.
	 * @return Returns the list of the string that represent the path to the agent files.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	public List<String> getAgentFilesLocations(StorageData storageData) throws IOException {
		return getFilesHttpLocation(storageData, StorageFileExtensions.AGENT_FILE_EXT);
	}

	/**
	 * Returns list of files paths with given extension for a storage in HTTP form.
	 * 
	 * @param storageData
	 *            Storage.
	 * @param extension
	 *            Files extension.
	 * @return Returns list of files with given extension for a storage in HTTP form.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	private List<String> getFilesHttpLocation(StorageData storageData, final String extension) throws IOException {
		Path storagePath = getStoragePath(storageData);
		if (storagePath == null || !Files.isDirectory(storagePath)) {
			return Collections.emptyList();
		}

		final List<Path> filesPaths = new ArrayList<Path>();
		Files.walkFileTree(storagePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().endsWith(extension)) {
					filesPaths.add(file);
				}
				return super.visitFile(file, attrs);
			}
		});

		List<String> result = new ArrayList<String>();
		for (Path path : filesPaths) {
			result.add("/" + storageData.getStorageFolder() + "/" + path.getFileName());
		}

		return result;
	}

	/**
	 * Add a label to the storage and saves new state of the storage to the disk.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 * @param storageLabel
	 *            Label to add.
	 * @param doOverwrite
	 *            Overwrite if label type already exists and is only one per storage allowed.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 * @throws SerializationException
	 *             If {@link SerializationException} happens.
	 */
	public void addLabelToStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel, boolean doOverwrite) throws IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		if (null != local) {
			local.addLabel(storageLabel, doOverwrite);
			writeStorageDataToDisk(local);
		}
	}

	/**
	 * Removes label from storage and saves new state of the storage data to the disk.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 * @param storageLabel
	 *            Label to remove.
	 * @return True if the label was removed, false otherwise.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 * @throws SerializationException
	 *             If {@link SerializationException} happens.
	 */
	public boolean removeLabelFromStorage(StorageData storageData, AbstractStorageLabel<?> storageLabel) throws IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		if (null != local) {
			boolean removed = local.removeLabel(storageLabel);
			writeStorageDataToDisk(local);
			return removed;
		}
		return false;
	}

	/**
	 * Updates the storage data for already existing storage.
	 * 
	 * @param storageData
	 *            Storage data containing update values.
	 * @throws StorageException
	 *             If storage does not exists.
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If IO operation fails.
	 */
	public void updateStorageData(StorageData storageData) throws StorageException, IOException, SerializationException {
		StorageData local = getLocalStorageDataObject(storageData);
		if (null == local) {
			throw new StorageException("Storage to update does not exists.");
		} else {
			synchronized (local) {
				local.setName(storageData.getName());
				local.setDescription(storageData.getDescription());
				writeStorageDataToDisk(local);
			}
		}
	}

	/**
	 * Returns the status of the active storage writers. This can be used for logging purposes.
	 * 
	 * @return Returns the status of the active storage writers.
	 */
	public Map<StorageData, String> getWritersStatus() {
		Map<StorageData, String> map = new HashMap<StorageData, String>();
		for (Map.Entry<StorageData, StorageWriter> entry : openedStoragesMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue().getExecutorServiceStatus());
		}
		return map;
	}

	/**
	 * Updates the size of each existing storage, if it changed.
	 * <p>
	 * This method is called from a Spring configured job.
	 * 
	 * @throws IOException
	 *             If {@link IOException} happened during operation.
	 * @throws SerializationException
	 *             If serialization failed.
	 */
	@Scheduled(fixedRate = UPDATE_RATE)
	protected void updateExistingStoragesSize() throws IOException, SerializationException {
		for (StorageData storageData : existingStoragesSet) {
			synchronized (storageData) {
				long newSize = getDiskSizeForStorage(storageData);
				if (newSize != storageData.getDiskSize()) {
					storageData.setDiskSize(newSize);
					writeStorageDataToDisk(storageData);
				}
			}
		}
	}

	/**
	 * Returns the size of the storage on disk.
	 * 
	 * @param storageData
	 *            Storage.
	 * @return Size of storage on disk, or 0 if {@link IOException} occurs during calculations.
	 */
	private long getDiskSizeForStorage(StorageData storageData) {
		Path storageDir = getStoragePath(storageData);
		try {
			final MutableLong size = new MutableLong(0);
			Files.walkFileTree(storageDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					size.add(attrs.size());
					return super.visitFile(file, attrs);
				}
			});
			return size.longValue();
		} catch (IOException e) {
			return 0;
		}
	}

	/**
	 * Returns the local cached object that represent the {@link StorageData}.
	 * 
	 * @param storageData
	 *            Template.
	 * @return Local object.
	 */
	private StorageData getLocalStorageDataObject(StorageData storageData) {
		for (StorageData existing : existingStoragesSet) {
			if (existing.getId().equals(storageData.getId())) {
				return existing;
			}
		}
		throw new RuntimeException("Local storage object can not be found with given storage data: " + storageData);
	}

	/**
	 * Loads all existing storages by walking through the default storage directory.
	 */
	private void loadAllExistingStorages() {
		existingStoragesSet = Collections.newSetFromMap(new ConcurrentHashMap<StorageData, Boolean>());

		Path defaultDirectory = Paths.get(getStorageDefaultFolder());
		if (!Files.isDirectory(defaultDirectory)) {
			return;
		}

		try {
			Files.walkFileTree(defaultDirectory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(StorageFileExtensions.STORAGE_FILE_EXT)) {
						try {
							byte[] bytes = Files.readAllBytes(file);
							ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
							Object deserialized = getSerializer().deserialize(byteBuffer);
							if (deserialized instanceof StorageData) {
								StorageData storageData = (StorageData) deserialized;
								if (storageData.getState() == StorageState.CLOSED) {
									// do not add any corrupted storages
									existingStoragesSet.add(storageData);
								}
							}
						} catch (IOException e) {
							LOGGER.error("Error reading existing storage data file. File path: " + file.toString() + ".", e);
						} catch (SerializationException e) {
							LOGGER.error("Error deserializing existing storage binary data in file:" + file.toString() + ".", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			LOGGER.error("Error exploring default storage directory. Directory path: " + defaultDirectory.toString() + ".", e);
		}
	}

	/**
	 * Adds a shutdown hook to the current {@link Runtime}, so that all storages can be closed,
	 * before the CMR is shutdown. All pending writes should be executed on shutdown.
	 */
	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				CmrStorageManager.this.closeAllStorages();
			}
		});
	}

	/**
	 * Returns the unique String that will be used as a StorageData ID. This ID needs to be unique
	 * not only for the current CMR, but we need to ensure that is unique for all CMRs, because the
	 * correlation between storage and CMR will be done by this ID.
	 * 
	 * @return Returns unique string based on the {@link UUID}.
	 */
	private String getRandomUUIDString() {
		return UUID.randomUUID().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		loadAllExistingStorages();
		updatedStorageSpaceLeft();
		addShutdownHook();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("existingStoragesSet", existingStoragesSet);
		toStringBuilder.append("openedStoragesMap", openedStoragesMap);
		toStringBuilder.append("storageRecorder", storageRecorder);
		return toStringBuilder.toString();
	}

}
