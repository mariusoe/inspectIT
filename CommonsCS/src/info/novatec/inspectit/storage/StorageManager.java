package info.novatec.inspectit.storage;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageDescriptor;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.lang.mutable.MutableObject;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.esotericsoftware.kryo.io.Input;
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
	 * Default upload folder.
	 */
	@Value(value = "${storage.storageDefaultFolder}/uploads")
	private String storageUploadsFolder;

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
	 * Returns the default storage directory as the absolute path.
	 * 
	 * @return Returns the default storage directory as the absolute path.
	 */
	protected abstract Path getDefaultStorageDirPath();

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
		return getStoragePath(storageData).resolve(String.valueOf(descriptor.getChannelId()) + StorageFileExtensions.DATA_FILE_EXT);
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
		return getStoragePath(storageData).resolve(channelId + StorageFileExtensions.DATA_FILE_EXT);
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
		sb.append(StorageFileExtensions.DATA_FILE_EXT);
		return sb.toString();
	}

	/**
	 * Writes the storage data file to disk (in the default storage directory). If the file already
	 * exists, it will be deleted.
	 * 
	 * @param storageData
	 *            Storage data.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeStorageDataToDisk(StorageData storageData) throws IOException, SerializationException {
		this.writeStorageDataToDisk(storageData, getStoragePath(storageData), StorageFileExtensions.STORAGE_FILE_EXT);
	}

	/**
	 * Writes the storage data file to disk (in the default storage directory). If the file already
	 * exists, it will be deleted.
	 * 
	 * @param localStorageData
	 *            Local Storage data.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeLocalStorageDataToDisk(LocalStorageData localStorageData) throws IOException, SerializationException {
		this.writeStorageDataToDisk(localStorageData, getStoragePath(localStorageData), StorageFileExtensions.LOCAL_STORAGE_FILE_EXT);
	}

	/**
	 * Writes the storage data file to disk (to the given directory). If the file already exists, it
	 * will be deleted.
	 * 
	 * @param storageData
	 *            Storage data.
	 * @param dir
	 *            Directory where file will be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeStorageDataToDisk(StorageData storageData, Path dir) throws IOException, SerializationException {
		this.writeStorageDataToDisk(storageData, dir, StorageFileExtensions.STORAGE_FILE_EXT);
	}

	/**
	 * Writes the storage data file to disk (to the given directory). If the file already exists, it
	 * will be deleted.
	 * 
	 * @param localStorageData
	 *            Local Storage data.
	 * @param dir
	 *            Directory where file will be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	protected void writeLocalStorageDataToDisk(LocalStorageData localStorageData, Path dir) throws IOException, SerializationException {
		this.writeStorageDataToDisk(localStorageData, dir, StorageFileExtensions.LOCAL_STORAGE_FILE_EXT);
	}

	/**
	 * Writes the storage data file to disk (to the given directory). If the file already exists, it
	 * will be deleted.
	 * 
	 * @param storageData
	 *            Object that can provide ID of storage.
	 * @param extenstion
	 *            File extension to search for.
	 * @param dir
	 *            Directory where file will be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	private void writeStorageDataToDisk(IStorageData storageData, Path dir, String extenstion) throws IOException, SerializationException {
		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}

		Path storageDataFile = dir.resolve(storageData.getId() + extenstion);
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
		Path defaultDirectory = getDefaultStorageDirPath();

		Path parent = defaultDirectory;
		while (Files.notExists(parent)) {
			parent = parent.getParent();
		}
		FileStore fileStore = Files.getFileStore(parent);
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
	 * Compresses the content of the storage data folder to the file. File name is provided via
	 * given path. If the file already exists, it will be deleted first.
	 * 
	 * @param storageData
	 *            {@link StorageData} to zip.
	 * @param zipPath
	 *            Path to the zip file.
	 * @param zipRoot
	 *            Path that points to the root of the zip file.
	 * @throws IOException
	 *             If {@link IOException} occurs during compressing.
	 */
	protected void zipStorageData(IStorageData storageData, final Path zipPath, final Path zipRoot) throws IOException {
		final Path storageDir = getStoragePath(storageData);
		if (Files.notExists(storageDir)) {
			throw new IOException("Storage can not be packed. Storage directory " + storageDir.toString() + " does not exist.");
		} else {
			Files.walkFileTree(storageDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (zipPath.equals(file)) {
						return FileVisitResult.CONTINUE;
					}
					Path destination = zipRoot.resolve(storageDir.relativize(file).toString());
					Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Path toCreate = zipRoot.resolve(storageDir.relativize(dir).toString());
					if (Files.notExists(toCreate)) {
						Files.createDirectories(toCreate);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	/**
	 * Returns the {@link StorageData} object that exists in the compressed storage file.
	 * 
	 * @param zipFilePath
	 *            Compressed storage file.
	 * @return StorageData object or <code>null</code> if the given path is not of correct type.
	 */
	protected IStorageData getStorageDataFromZip(final Path zipFilePath) {
		if (Files.notExists(zipFilePath)) {
			return null;
		}

		final ISerializer serializer = serializationManagerProvider.createSerializer();
		try {
			FileSystem zipFileSystem = createZipFileSystem(zipFilePath, false);
			final Path zipRoot = zipFileSystem.getPath("/");
			final MutableObject mutableStorageData = new MutableObject();
			Files.walkFileTree(zipRoot, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.equals(zipFilePath)) {
						return FileVisitResult.CONTINUE;
					}
					if (file.toString().endsWith(StorageFileExtensions.LOCAL_STORAGE_FILE_EXT)) {
						InputStream inputStream = null;
						Input input = null;
						try {
							inputStream = Files.newInputStream(file, StandardOpenOption.READ);
							input = new Input(inputStream);
							Object deserialized = serializer.deserialize(input);
							if (deserialized instanceof IStorageData) {
								mutableStorageData.setValue(deserialized);
							}
						} catch (SerializationException e) {
							return FileVisitResult.CONTINUE;
						} finally {
							if (null != input) {
								input.close();
							}
						}
					}
					return FileVisitResult.CONTINUE;
				}

			});
			zipFileSystem.close();
			return (IStorageData) mutableStorageData.getValue();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Unzips the content of the zip file provided to the default storage folder. The method will
	 * first unzip the complete content of the zip file to the temporary folder and then rename the
	 * temporary folder to match the storage ID.
	 * 
	 * @param zipFilePath
	 *            Path to the zip file.
	 * @param destinationPath
	 *            The path where it should be unpacked.
	 * @throws StorageException
	 *             If zipFilePath does not exist or destination path does exist.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * 
	 */
	protected void unzipStorageData(final Path zipFilePath, final Path destinationPath) throws StorageException, IOException {
		if (Files.notExists(zipFilePath)) {
			throw new StorageException("Can not unpack the storage zip file. File " + zipFilePath + " does not exist.");
		}

		Files.createDirectories(destinationPath);

		final FileSystem zipFileSystem = createZipFileSystem(zipFilePath, false);
		final Path zipRoot = zipFileSystem.getPath("/");
		Files.walkFileTree(zipRoot, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.equals(zipFilePath)) {
					return FileVisitResult.CONTINUE;
				}

				Path destination = destinationPath.resolve(zipRoot.relativize(file).toString());
				if (!isGzipCompressedData(file)) {
					Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				} else {
					GZIPInputStream gzis = new GZIPInputStream(Files.newInputStream(file, StandardOpenOption.READ));
					try {
						Files.copy(gzis, destination, StandardCopyOption.REPLACE_EXISTING);
					} finally {
						if (null != gzis) {
							gzis.close();
						}
					}
					return FileVisitResult.CONTINUE;
				}
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path toCreate = destinationPath.resolve(zipRoot.relativize(dir).toString());
				if (Files.notExists(toCreate)) {
					Files.createDirectories(toCreate);
				}
				return FileVisitResult.CONTINUE;
			}

		});
		zipFileSystem.close();
	}

	/**
	 * Returns true if the data stored in the file is in a GZIP format. The input stream will be
	 * opened on a file and two first bytes will be read.
	 * 
	 * @param file
	 *            File to check.
	 * @return True if the data is in GZIP format, false otherwise.
	 * @throws IOException
	 *             If file does not exists or can not be opened for read.
	 */
	private boolean isGzipCompressedData(Path file) throws IOException {
		InputStream is = null;
		try {
			is = Files.newInputStream(file, StandardOpenOption.READ);
			byte[] firsTwoBytes = new byte[2];
			int read = 0;
			// safety from reading one byte only
			while (read < 2) {
				read += is.read(firsTwoBytes, read, 2 - read);
			}
			int head = ((int) firsTwoBytes[0] & 0xff) | ((firsTwoBytes[1] << 8) & 0xff00);
			return (GZIPInputStream.GZIP_MAGIC == head);
		} finally {
			if (null != is) {
				is.close();
			}
		}
	}

	/**
	 * Creates the zip file system.
	 * 
	 * @param path
	 *            Path that will hold the path to the new zip file.
	 * @param create
	 *            If zip file system should be created.
	 * @return {@link FileSystem}.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	protected static FileSystem createZipFileSystem(Path path, boolean create) throws IOException {
		final URI uri = URI.create("jar:file:" + path.toUri().getPath());
		final Map<String, String> env = new HashMap<String, String>();
		if (create) {
			env.put("create", "true");
		}
		return FileSystems.newFileSystem(uri, env);
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
	 * Gets {@link #storageUploadsFolder}.
	 * 
	 * @return {@link #storageUploadsFolder}
	 */
	public String getStorageUploadsFolder() {
		return storageUploadsFolder;
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
