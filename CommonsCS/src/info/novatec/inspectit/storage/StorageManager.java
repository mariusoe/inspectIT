package info.novatec.inspectit.storage;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageDescriptor;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.esotericsoftware.kryo.io.Output;

/**
 * Abstract class that defines basic storage functionality and properties.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class StorageManager {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * The fixed rate of the refresh rate for gathering the statistics.
	 */
	@Value("${storage.updateRefreshRate}")
	protected static final int UPDATE_RATE = 30000;

	/**
	 * {@link SerializationManagerProvider}.
	 */
	@Autowired
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * Default storage folder.
	 */
	@Value(value = "${storage.storageDefaultFolder}")
	private String storageDefaultFolder;

	/**
	 * Amount of bytes CMR can use on the Hard drive to write storage data.
	 */
	@Value("${storage.maxHardDriveOccupancy}")
	private int maxHardDriveOccupancy;

	/**
	 * Amount of bytes when warning the user about the critical hard drive space left should start.
	 * This applies to both hard drive space or max hard drive occupancy.
	 */
	@Value("${storage.warnHardDriveByteLeft}")
	private long warnBytesLeft = 1073741824;

	/**
	 * Amount of bytes when writing any more data is suspended because of the hard drive space left.
	 * This applies to both hard drive space or max hard drive occupancy.
	 */
	@Value("${storage.stopWriteHardDriveBytesLeft}")
	private long stopWriteBytesLeft = 104857600;

	/**
	 * Amount of space left for write in bytes. This value is either {@link #maxHardDriveOccupancy}
	 * or actual space left on the hard drive if no {@link #maxHardDriveOccupancy} is specified or
	 * space left is smaller than {@link #maxHardDriveOccupancy}.
	 */
	private long bytesHardDriveOccupancyLeft;

	/**
	 * Amount of total space on the hard drive in bytes.
	 */
	private long hardDriveSize;

	/**
	 * Returns the {@link Path} for given {@link IStorageIdProvider}.
	 * 
	 * @param storageData
	 *            {@link IStorageData} object.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public abstract Path getStoragePath(IStorageData storageData);

	/**
	 * Returns the {@link Path} of the channel for given {@link StorageData} and
	 * {@link StorageDescriptor}.
	 * 
	 * @param storageData
	 *            {@link IStorageData} object.
	 * @param descriptor
	 *            {@link StorageDescriptor} object.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public Path getChannelPath(IStorageData storageData, IStorageDescriptor descriptor) {
		return getStoragePath(storageData).resolve(String.valueOf(descriptor.getChannelId()) + StorageFileExtensions.DATA_FILE_EXTENSION);
	}

	/**
	 * Returns the {@link Path} of the channel for given {@link StorageData} and ID of the channel.
	 * 
	 * @param storageData
	 *            {@link IStorageData} object.
	 * @param channelId
	 *            Id of channel.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public Path getChannelPath(IStorageData storageData, int channelId) {
		return getStoragePath(storageData).resolve(channelId + StorageFileExtensions.DATA_FILE_EXTENSION);
	}

	/**
	 * Returns the URL location of the file on the server where the descriptor is pointing to,
	 * without ip and port information.
	 * <p>
	 * Example locations is: /storageId/descriptorId.itdata
	 * 
	 * @param storageData
	 *            {@link StorageData}
	 * @param descriptor
	 *            {@link StorageDescriptor}
	 * @return URL location without ip and port.
	 */
	public String getHttpFileLocation(IStorageData storageData, IStorageDescriptor descriptor) {
		return this.getHttpFileLocation(storageData, Integer.valueOf(descriptor.getChannelId()));
	}

	/**
	 * Returns the URL location of the file on the server where the channel ID is pointing to,
	 * without ip and port information.
	 * <p>
	 * Example locations is: /storageId/descriptorId.itdata
	 * 
	 * @param storageData
	 *            {@link StorageData}
	 * @param channelId
	 *            Channel ID.
	 * @return URL location without ip and port.
	 */
	public String getHttpFileLocation(IStorageData storageData, Integer channelId) {
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(storageData.getId());
		sb.append("/");
		sb.append(channelId.intValue());
		sb.append(StorageFileExtensions.DATA_FILE_EXTENSION);
		return sb.toString();
	}

	/**
	 * Writes the storage data file to disk. If the file already exists, it will be deleted.
	 * 
	 * @param storageData
	 *            Storage data.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeStorageDataToDisk(StorageData storageData) throws IOException, SerializationException {
		this.writeStorageDataToDisk(storageData, StorageFileExtensions.STORAGE_FILE_EXT);
	}

	/**
	 * Writes the storage data file to disk. If the file already exists, it will be deleted.
	 * 
	 * @param localStorageData
	 *            Local Storage data.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeLocalStorageDataToDisk(LocalStorageData localStorageData) throws IOException, SerializationException {
		this.writeStorageDataToDisk(localStorageData, StorageFileExtensions.LOCAL_STORAGE_FILE_EXT);
	}

	/**
	 * Writes the storage data file to disk. If the file already exists, it will be deleted.
	 * 
	 * @param storageData
	 *            Object that can provide ID of storage.
	 * @param extenstion
	 *            File extension to search for.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	private void writeStorageDataToDisk(IStorageData storageData, String extenstion) throws IOException, SerializationException {
		Path storageDir = getStoragePath(storageData);
		if (!Files.exists(storageDir)) {
			Files.createDirectories(storageDir);
		}

		Path storagePath = getStoragePath(storageData);
		Path storageDataFile = storagePath.resolve(storageData.getId() + extenstion);
		if (Files.exists(storageDataFile)) {
			Files.delete(storageDataFile);
		}

		ISerializer serializer = serializationManagerProvider.createSerializer();
		OutputStream outputStream = null;
		Output output = null;
		try {
			outputStream = Files.newOutputStream(storageDataFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			output = new Output(outputStream);
			serializer.serialize(storageData, output);
		} finally {
			if (null != output) {
				output.close();
			}
		}
	}

	/**
	 * Deletes all files associated with given {@link StorageData}, thus completely removes storage
	 * from disk.
	 * 
	 * @param storageData
	 *            Storage to delete data for.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 */
	protected void deleteCompleteStorageDataFromDisk(IStorageData storageData) throws IOException {
		Path storageDir = getStoragePath(storageData);

		if (log.isDebugEnabled()) {
			log.info("Deleting the storage data from disk. Path: " + storageDir);
		}

		if (Files.exists(storageDir)) {
			Files.walkFileTree(storageDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (null == exc) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						throw exc;
					}
				}
			});
		}
	}

	/**
	 * @return Returns if the write of data can be performed in terms of hard disk space left.
	 */
	public boolean canWriteMore() {
		return bytesHardDriveOccupancyLeft > stopWriteBytesLeft;
	}

	/**
	 * @return Returns if the warn about the low space left is active.
	 */
	public boolean isSpaceWarnActive() {
		return bytesHardDriveOccupancyLeft < warnBytesLeft;
	}

	/**
	 * Updates the space left on the hard drive.
	 * 
	 * @throws IOException
	 *             IF {@link IOException} occurs.
	 */
	@Scheduled(fixedRate = UPDATE_RATE * 2)
	protected void updatedStorageSpaceLeft() throws IOException {
		Path defaultDirectory = Paths.get(getStorageDefaultFolder());

		FileStore fileStore = Files.getFileStore(Paths.get(FileSystems.getDefault().getSeparator()));
		hardDriveSize = fileStore.getTotalSpace();
		long bytesAvailable = fileStore.getUsableSpace();

		if (Files.exists(defaultDirectory) && maxHardDriveOccupancy > 0 && bytesAvailable > maxHardDriveOccupancy) {
			final MutableLong totalSizeInBytes = new MutableLong();
			Files.walkFileTree(defaultDirectory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					totalSizeInBytes.add(Files.size(file));
					return FileVisitResult.CONTINUE;
				}
			});

			long totalSize = totalSizeInBytes.longValue();
			bytesHardDriveOccupancyLeft = maxHardDriveOccupancy - totalSize;
			if (bytesHardDriveOccupancyLeft < 0) {
				bytesHardDriveOccupancyLeft = 0;
			}
		} else {
			bytesHardDriveOccupancyLeft = bytesAvailable;
		}
	}

	/**
	 * Gets {@link #serializationManagerProvider}.
	 * 
	 * @return {@link #serializationManagerProvider}
	 */
	public SerializationManagerProvider getSerializationManagerProvider() {
		return serializationManagerProvider;
	}

	/**
	 * Sets {@link #serializationManagerProvider}.
	 * 
	 * @param serializationManagerProvider
	 *            New value for {@link #serializationManagerProvider}
	 */
	public void setSerializationManagerProvider(SerializationManagerProvider serializationManagerProvider) {
		this.serializationManagerProvider = serializationManagerProvider;
	}

	/**
	 * @return the storageDefaultFolder
	 */
	public String getStorageDefaultFolder() {
		return storageDefaultFolder;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 * 
	 * @param storageDefaultFolder
	 *            the storageDefaultFolder to set
	 */
	public void setStorageDefaultFolder(String storageDefaultFolder) {
		this.storageDefaultFolder = storageDefaultFolder;
	}

	/**
	 * Gets {@link #bytesHardDriveOccupancyLeft}.
	 * 
	 * @return {@link #bytesHardDriveOccupancyLeft}
	 */
	public long getBytesHardDriveOccupancyLeft() {
		return bytesHardDriveOccupancyLeft;
	}

	/**
	 * Gets {@link #maxHardDriveOccupancy}.
	 * 
	 * @return {@link #maxHardDriveOccupancy}
	 */
	public long getMaxBytesHardDriveOccupancy() {
		if (maxHardDriveOccupancy > -1) {
			return maxHardDriveOccupancy;
		} else {
			return hardDriveSize;
		}
	}

}
