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
	 * Returns the {@link Path} for given {@link IStorageIdProvider}.
	 *
	 * @param storageIdProvider
	 *            {@link IStorageIdProvider} object.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public abstract Path getStoragePath(IStorageIdProvider storageIdProvider);

	/**
	 * Returns the {@link Path} of the channel for given {@link StorageData} and
	 * {@link StorageDescriptor}.
	 *
	 * @param storageData
	 *            {@link StorageData} object.
	 * @param descriptor
	 *            {@link StorageDescriptor} object.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public Path getChannelPath(StorageData storageData, IStorageDescriptor descriptor) {
		return getStoragePath(storageData).resolve(String.valueOf(descriptor.getChannelId()) + StorageFileExtensions.DATA_FILE_EXTENSION);
	}

	/**
	 * Returns the {@link Path} of the channel for given {@link StorageData} and ID of the channel.
	 *
	 * @param storageData
	 *            {@link StorageData} object.
	 * @param channelId
	 *            Id of channel.
	 * @return {@link Path} that can be used in IO operations.
	 */
	public Path getChannelPath(StorageData storageData, int channelId) {
		return getStoragePath(storageData).resolve(channelId + StorageFileExtensions.DATA_FILE_EXTENSION);
	}

	/**
	 * Returns the URL location of the file on the server where the descriptor is pointing to,
	 * without ip and port information.
	 * <p>
	 * Example locations is: /storageId/descriptorId.itdata
	 *
	 * @param storageIdProvider
	 *            {@link StorageData}
	 * @param descriptor
	 *            {@link StorageDescriptor}
	 * @return URL location without ip and port.
	 */
	public String getHttpFileLocation(IStorageIdProvider storageIdProvider, IStorageDescriptor descriptor) {
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(storageIdProvider.getId());
		sb.append("/");
		sb.append(String.valueOf(descriptor.getChannelId()));
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
		this.writeIStorageIdProviderToDisk(storageData, StorageFileExtensions.STORAGE_FILE_EXT);
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
		this.writeIStorageIdProviderToDisk(localStorageData, StorageFileExtensions.LOCAL_STORAGE_FILE_EXT);
	}

	/**
	 * Writes the storage data file to disk. If the file already exists, it will be deleted.
	 *
	 * @param storageIdProvider
	 *            Object that can provide ID of storage.
	 * @param extenstion
	 *            File extension to search for.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	private void writeIStorageIdProviderToDisk(IStorageIdProvider storageIdProvider, String extenstion) throws IOException, SerializationException {
		Path storageDir = getStoragePath(storageIdProvider);
		if (!Files.exists(storageDir)) {
			Files.createDirectories(storageDir);
		}

		Path storagePath = getStoragePath(storageIdProvider);
		Path storageDataFile = storagePath.resolve(storageIdProvider.getId() + extenstion);
		if (Files.exists(storageDataFile)) {
			Files.delete(storageDataFile);
		}

		// 100KB should always enough
		ByteBuffer buffer = ByteBuffer.allocateDirect(100 * 1024);
		serializer.serialize(storageIdProvider, buffer);
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);

		Files.write(storageDataFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}

	/**
	 * Deletes all files associated with given {@link StorageData}, thus completely removes storage
	 * from disk.
	 *
	 * @param storageIdProvider
	 *            Storage to delete data for.
	 * @throws IOException
	 *             If {@link IOException} happens.
	 */
	protected void deleteCompleteStorageDataFromDisk(IStorageIdProvider storageIdProvider) throws IOException {
		Path storageDir = getStoragePath(storageIdProvider);

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
	 * @param serializer the serializer to set
	 */
	public void setSerializer(ISerializer serializer) {
		this.serializer = serializer;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 *
	 * @param storageDefaultFolder the storageDefaultFolder to set
	 */
	public void setStorageDefaultFolder(String storageDefaultFolder) {
		this.storageDefaultFolder = storageDefaultFolder;
	}

}
