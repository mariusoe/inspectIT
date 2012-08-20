package info.novatec.inspectit.storage;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageDescriptor;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
	 * Serializer.
	 */
	@Autowired
	private ISerializer serializer;

	/**
	 * Default storage folder.
	 */
	@Value(value = "${storage.storageDefaultFolder}")
	private String storageDefaultFolder;

	/**
	 * Returns the {@link Path} for given {@link IStorageData}.
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

		// 100KB should always enough
		ByteBuffer buffer = ByteBuffer.allocateDirect(100 * 1024);
		serializer.serialize(storageData, buffer);
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);

		Files.write(storageDataFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
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
	 * @return the serializer
	 */
	protected ISerializer getSerializer() {
		return serializer;
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
	 * @param serializer
	 *            the serializer to set
	 */
	public void setSerializer(ISerializer serializer) {
		this.serializer = serializer;
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

}
