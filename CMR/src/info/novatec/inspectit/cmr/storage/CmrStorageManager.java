package info.novatec.inspectit.cmr.storage;

import info.novatec.inspectit.cmr.dao.StorageDataDao;
import info.novatec.inspectit.cmr.dao.impl.DefaultDataDaoImpl;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.spring.logger.Logger;
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
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.util.CopyMoveFileVisitor;
import info.novatec.inspectit.storage.util.DeleteFileVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.lang.mutable.MutableObject;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.io.Input;

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
	 * The log of this class.
	 */
	@Logger
	Log log;

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
			if (Objects.equals(local, recorderStorageData)) {
				throw new StorageException("Storage " + local + " can not be finalized because it is currenlty used for recording purposes.");
			}
			StorageWriter writer = openedStoragesMap.get(local);
			if (writer != null) {
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
				StorageWriter storageWriter = openedStoragesMap.remove(local);
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
				StorageWriter storageWriter = storageRecorder.getStorageWriter();
				storageRecorder.stopRecording();
				recorderStorageData.markOpened();
				openedStoragesMap.put(recorderStorageData, storageWriter);
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
				log.warn("Exception occured trying to automatically stop the recording due to the hard disk space limitation warning.", e);
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
	 * @param synchronously
	 *            If write will be done synchronously or not.
	 * @throws StorageException
	 *             If storage is used as a recording storage.
	 */
	public void writeToStorage(StorageData storageData, Collection<? extends DefaultData> dataToWrite, Collection<AbstractDataProcessor> dataProcessors, boolean synchronously) throws StorageException {
		StorageData local = getLocalStorageDataObject(storageData);
		StorageWriter writer = openedStoragesMap.get(local);
		if (writer != null) {
			if (synchronously) {
				writer.processSynchronously(dataToWrite, dataProcessors);
			} else {
				writer.process(dataToWrite, dataProcessors);
			}
		} else if (Objects.equals(local, recorderStorageData)) {
			throw new StorageException("Can not write to storage that is currently used as a recording storage.");
		} else if (local.getState() == StorageState.CLOSED) {
			throw new StorageException("Can not write to closed storage");
		} else {
			log.error("Writer for the not closed storage " + local + " not available.");
			throw new StorageException("Writer for the not closed storage " + local + " not available.");
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
			this.writeToStorage(local, toWriteList, dataProcessors, true);
		}
		updateExistingStorageSize(local);
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
		this.writeToStorage(local, toWriteList, dataProcessors, true);
		updateExistingStorageSize(local);
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
				log.warn("Recording storage could not be finalized during the CMR shut-down.", e);

			}
		}
		for (StorageData openedStorage : openedStoragesMap.keySet()) {
			try {
				this.closeStorage(openedStorage);
			} catch (Exception e) {
				log.warn("Storage " + openedStorage + " could not be finalized during the CMR shut-down.", e);
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
	 * {@inheritDoc}
	 */
	public Path getStoragePath(IStorageData storageData) {
		return getDefaultStorageDirPath().resolve(storageData.getStorageFolder());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Path getDefaultStorageDirPath() {
		return Paths.get(getStorageDefaultFolder()).toAbsolutePath();
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
		return getFilesHttpLocation(storageData, StorageFileExtensions.DATA_FILE_EXT);
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
			updateExistingStorageSize(storageData);
		}
	}

	/**
	 * Updates size of the given storage and saves information to this.
	 * 
	 * @param storageData
	 *            Storage data.
	 * @throws IOException
	 *             If {@link IOException} happened during operation.
	 * @throws SerializationException
	 *             If serialization failed.
	 */
	private void updateExistingStorageSize(StorageData storageData) throws IOException, SerializationException {
		if (null != storageData) {
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
	 * Checks for the uploaded files in the storage uploads folder and tries to extract data to the
	 * default storage folder.
	 * 
	 * @param packedStorageData
	 *            Storage data that is packed in the file that needs to be unpacked.
	 * 
	 * @throws IOException
	 *             IF {@link IOException} occurs during the file tree walk.
	 * @throws StorageException
	 *             If there is not enough space for the unpacking the storage.
	 */
	public void unpackUploadedStorage(final IStorageData packedStorageData) throws IOException, StorageException {
		long storageBytesLeft = getBytesHardDriveOccupancyLeft();
		if (packedStorageData.getDiskSize() > storageBytesLeft) {
			throw new StorageException("Uploaded storage " + packedStorageData + " can not be unpacked because there is not enough disk space left for storage data on the CMR.");
		}

		Path uploadPath = Paths.get(this.getStorageUploadsFolder());
		if (Files.notExists(uploadPath)) {
			throw new IOException("Can not perform storage unpacking. The main upload path " + uploadPath.toString() + " does not exist.");
		} else {
			Files.walkFileTree(uploadPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						// skip all other files
						if (!file.toString().endsWith(StorageFileExtensions.ZIP_STORAGE_EXT)) {
							return FileVisitResult.CONTINUE;
						}

						IStorageData storageData = getStorageDataFromZip(file);
						if (!Objects.equals(packedStorageData, storageData)) {
							// go to next file if the file that we found does not hold the correct
							// storage to unpack
							return FileVisitResult.CONTINUE;
						}

						if (null != storageData) {
							StorageData importedStorageData = new StorageData(storageData);
							if (existingStoragesSet.add(importedStorageData)) {
								unzipStorageData(file, getStoragePath(importedStorageData));
								Path localInformation = getStoragePath(importedStorageData).resolve(importedStorageData.getId() + StorageFileExtensions.LOCAL_STORAGE_FILE_EXT);
								Files.deleteIfExists(localInformation);
								writeStorageDataToDisk(importedStorageData);
							} else {
								log.info("Uploaded storage file " + file.toString() + " contains the storage that is already available on the CMR. File will be deleted.");
							}
						}
						Files.deleteIfExists(file);
					} catch (Exception e) {
						log.warn("Uploaded storage file " + file.toString() + " is not of correct type and can not be extracted. File will be deleted.", e);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	/**
	 * Creates a storage form the uploaded local storage directory.
	 * 
	 * @param localStorageData
	 *            Local storage information.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws StorageException
	 *             If there is not enough space for the unpacking the storage.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	public void createStorageFromUploadedDir(final IStorageData localStorageData) throws IOException, StorageException, SerializationException {
		long storageBytesLeft = getBytesHardDriveOccupancyLeft();
		if (localStorageData.getDiskSize() > storageBytesLeft) {
			throw new StorageException("Uploaded storage " + localStorageData + " can not be unpacked because there is not enough disk space left for storage data on the CMR.");
		}

		Path uploadPath = Paths.get(this.getStorageUploadsFolder());
		if (Files.notExists(uploadPath)) {
			throw new IOException("Can not perform storage unpacking. The main upload path " + uploadPath.toString() + " does not exist.");
		} else {
			final MutableObject storageUploadPath = new MutableObject();
			final MutableObject uploadedStorageData = new MutableObject();
			final ISerializer serializer = getSerializationManagerProvider().createSerializer();
			Files.walkFileTree(uploadPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					// skip all other files, search for the local data
					if (!file.toString().endsWith(localStorageData.getId() + StorageFileExtensions.LOCAL_STORAGE_FILE_EXT)) {
						return FileVisitResult.CONTINUE;
					}

					// when found confirm it is the one we wanted to upload
					InputStream inputStream = null;
					Input input = null;
					try {
						inputStream = Files.newInputStream(file, StandardOpenOption.READ);
						input = new Input(inputStream);
						Object deserialized = serializer.deserialize(input);
						if (Objects.equals(deserialized, localStorageData)) {
							uploadedStorageData.setValue(new StorageData(localStorageData));
							storageUploadPath.setValue(file.toAbsolutePath().getParent());
							return FileVisitResult.TERMINATE;
						}
					} catch (SerializationException e) {
						log.warn("Error de-serializing local storage file.", e);
					} finally {
						if (null != input) {
							input.close();
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});

			// do the rest out of the file walk
			Path parentDir = (Path) storageUploadPath.getValue();
			StorageData storageData = (StorageData) uploadedStorageData.getValue();
			if (null != storageData && null != parentDir) {
				Path storageDir = getStoragePath(storageData);
				if (existingStoragesSet.add(storageData)) {
					if (Files.notExists(storageDir)) {
						Files.walkFileTree(parentDir, new CopyMoveFileVisitor(parentDir, storageDir, true));
						Path localInformation = getStoragePath(storageData).resolve(storageData.getId() + StorageFileExtensions.LOCAL_STORAGE_FILE_EXT);
						Files.deleteIfExists(localInformation);
						writeStorageDataToDisk(storageData);
					} else {
						throw new IOException("Directory to place uploaded storage already exists.");
					}
				} else {
					log.info("Uploaded storage on path " + parentDir.toString() + " contains the storage that is already available on the CMR. Dir will be deleted.");
					Files.walkFileTree(parentDir, new DeleteFileVisitor());
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

		final ISerializer serializer = getSerializationManagerProvider().createSerializer();
		try {
			Files.walkFileTree(defaultDirectory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(StorageFileExtensions.STORAGE_FILE_EXT)) {

						InputStream inputStream = null;
						Input input = null;
						try {
							inputStream = Files.newInputStream(file, StandardOpenOption.READ);
							input = new Input(inputStream);
							Object deserialized = serializer.deserialize(input);
							if (deserialized instanceof StorageData) {
								StorageData storageData = (StorageData) deserialized;
								if (storageData.getState() == StorageState.CLOSED) {
									// do not add any corrupted storages
									existingStoragesSet.add(storageData);
								}
							}
						} catch (IOException e) {
							log.error("Error reading existing storage data file. File path: " + file.toString() + ".", e);
						} catch (SerializationException e) {
							log.error("Error deserializing existing storage binary data in file:" + file.toString() + ".", e);
						} finally {
							if (null != input) {
								input.close();
							}
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring default storage directory. Directory path: " + defaultDirectory.toString() + ".", e);
		}
	}

	/**
	 * Clears the upload folder.
	 */
	private void clearUploadFolder() {
		final Path uploadPath = Paths.get(this.getStorageUploadsFolder());
		if (Files.notExists(uploadPath)) {
			return;
		}

		try {
			Files.walkFileTree(uploadPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (dir.equals(uploadPath)) {
						return FileVisitResult.CONTINUE;
					}

					if (null == exc) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						throw exc;
					}
				}

			});
		} catch (IOException e) {
			log.warn("Could not delete the storage upload folder on the start-up.", e);
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
		clearUploadFolder();
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
