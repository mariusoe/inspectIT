package info.novatec.inspectit.rcp.storage.util;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageDescriptor;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.nio.stream.StreamProvider;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;
import info.novatec.inspectit.storage.serializer.util.KryoUtil;
import info.novatec.inspectit.storage.util.RangeDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;

import com.esotericsoftware.kryo.io.Input;

/**
 * Class responsible for retrieving the data via HTTP, and de-serializing the data into objects.
 * 
 * @author Ivan Senic
 * 
 */
public class DataRetriever {

	/**
	 * Amount of serializers to be available to this class.
	 */
	private int serializerCount = 3;

	/**
	 * {@link StorageManager}.
	 */
	private StorageManager storageManager;

	/**
	 * Serialization manager provider.
	 */
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * Queue for {@link ISerializer} that are available.
	 */
	private BlockingQueue<ISerializer> serializerQueue = new LinkedBlockingQueue<ISerializer>();

	/**
	 * Stream provider needed for local data reading.
	 */
	private StreamProvider streamProvider;

	/**
	 * Initializes the retriever.
	 * 
	 * @throws Exception
	 *             If exception occurs.
	 */
	protected void init() throws Exception {
		for (int i = 0; i < serializerCount; i++) {
			serializerQueue.add(serializationManagerProvider.createSerializer());
		}
	}

	/**
	 * Retrieves the wanted data described in the {@link StorageDescriptor} from the desired
	 * {@link CmrRepositoryDefinition}. This method will try to invoke as less as possible HTTP
	 * requests for all descriptors.
	 * <p>
	 * The method will execute the HTTP requests sequentially.
	 * <p>
	 * It is not guaranteed that amount of returned objects in the list is same as the amount of
	 * provided descriptors. If some of the descriptors are pointing to the wrong files or files
	 * positions, it can happen that this influences the rest of the descriptor that point to the
	 * same file. Thus, a special care needs to be taken that the data in descriptors is correct.
	 * 
	 * @param <E>
	 *            Type of the objects are wanted.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData} that points to the wanted storage.
	 * @param descriptors
	 *            Descriptors.
	 * @return List of objects in the supplied generic type. Note that if the data described in the
	 *         descriptor is not of a supplied generic type, there will be a casting exception
	 *         thrown.
	 * @throws Exception
	 *             If {@link Exception} occurs.
	 */
	@SuppressWarnings("unchecked")
	public <E extends DefaultData> List<E> getDataViaHttp(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageData storageData, List<IStorageDescriptor> descriptors) throws Exception {
		Map<Integer, List<IStorageDescriptor>> separateFilesGroup = createFilesGroup(descriptors);
		List<E> receivedData = new ArrayList<E>();
		String serverUri = getServerUri(cmrRepositoryDefinition);

		HttpClient httpClient = new DefaultHttpClient();
		for (Map.Entry<Integer, List<IStorageDescriptor>> entry : separateFilesGroup.entrySet()) {
			HttpGet httpGet = new HttpGet(serverUri + storageManager.getHttpFileLocation(storageData, entry.getKey()));
			StringBuilder rangeHeader = new StringBuilder("bytes=");

			RangeDescriptor rangeDescriptor = null;
			for (IStorageDescriptor descriptor : entry.getValue()) {
				if (null == rangeDescriptor) {
					rangeDescriptor = new RangeDescriptor(descriptor);
				} else {
					if (rangeDescriptor.getEnd() + 1 == descriptor.getPosition()) {
						rangeDescriptor.setEnd(descriptor.getPosition() + descriptor.getSize() - 1);
					} else {
						rangeHeader.append(rangeDescriptor.toString() + ",");
						rangeDescriptor = new RangeDescriptor(descriptor);
					}
				}
			}
			rangeHeader.append(rangeDescriptor);

			httpGet.addHeader("Range", rangeHeader.toString());
			ISerializer serializer = serializerQueue.take();
			InputStream inputStream = null;
			Input input = null;
			try {
				HttpResponse response = httpClient.execute(httpGet);
				HttpEntity entity = response.getEntity();
				if (MultipartEntityUtil.isMultipart(entity)) {
					inputStream = entity.getContent();
					// all non-deprecated constructors have default modifier
					MultipartStream multipartStream = new MultipartStream(inputStream, MultipartEntityUtil.getBoundary(entity).getBytes());
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					boolean nextPart = multipartStream.skipPreamble();
					while (nextPart) {
						multipartStream.readHeaders();
						multipartStream.readBodyData(byteArrayOutputStream);
						input = new Input(byteArrayOutputStream.toByteArray());
						while (KryoUtil.hasMoreBytes(input)) {
							Object object = serializer.deserialize(input);
							E element = (E) object;
							receivedData.add(element);
						}
						nextPart = multipartStream.readBoundary();
					}
				} else {
					// when kryo changes the visibility of optional() method, we can really stream
					input = new Input(EntityUtils.toByteArray(entity));
					while (KryoUtil.hasMoreBytes(input)) {
						Object object = serializer.deserialize(input);
						E element = (E) object;
						receivedData.add(element);
					}
				}
			} finally {
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != input) {
					input.close();
				}
				serializerQueue.add(serializer);
			}
		}
		return receivedData;
	}

	/**
	 * Retrieves the wanted data described in the {@link StorageDescriptor} from the desired
	 * offline-available storage.
	 * <p>
	 * It is not guaranteed that amount of returned objects in the list is same as the amount of
	 * provided descriptors. If some of the descriptors are pointing to the wrong files or files
	 * positions, it can happen that this influences the rest of the descriptor that point to the
	 * same file. Thus, a special care needs to be taken that the data in descriptors is correct.
	 * 
	 * @param <E>
	 *            Type of the objects are wanted.
	 * @param localStorageData
	 *            {@link LocalStorageData} that points to the wanted storage.
	 * @param descriptors
	 *            Descriptors.
	 * @return List of objects in the supplied generic type. Note that if the data described in the
	 *         descriptor is not of a supplied generic type, there will be a casting exception
	 *         thrown.
	 * @throws Exception
	 *             If {@link Exception} occurs.
	 */
	@SuppressWarnings("unchecked")
	public <E extends DefaultData> List<E> getDataLocally(LocalStorageData localStorageData, List<IStorageDescriptor> descriptors) throws Exception {
		Map<Integer, List<IStorageDescriptor>> separateFilesGroup = createFilesGroup(descriptors);
		List<IStorageDescriptor> optimizedDescriptors = new ArrayList<IStorageDescriptor>();
		for (Map.Entry<Integer, List<IStorageDescriptor>> entry : separateFilesGroup.entrySet()) {
			StorageDescriptor storageDescriptor = null;
			for (IStorageDescriptor descriptor : entry.getValue()) {
				if (null == storageDescriptor) {
					storageDescriptor = new StorageDescriptor(entry.getKey());
					storageDescriptor.setPositionAndSize(descriptor.getPosition(), descriptor.getSize());
				} else {
					if (!storageDescriptor.join(descriptor)) {
						optimizedDescriptors.add(storageDescriptor);
						storageDescriptor = new StorageDescriptor(entry.getKey());
						storageDescriptor.setPositionAndSize(descriptor.getPosition(), descriptor.getSize());
					}
				}
			}
			optimizedDescriptors.add(storageDescriptor);
		}

		List<E> receivedData = new ArrayList<E>(descriptors.size());

		ISerializer serializer = serializerQueue.take();
		InputStream inputStream = null;
		Input input = null;
		try {
			inputStream = streamProvider.getExtendedByteBufferInputStream(localStorageData, optimizedDescriptors);
			input = new Input(inputStream);
			while (KryoUtil.hasMoreBytes(input)) {
				Object object = serializer.deserialize(input);
				E element = (E) object;
				receivedData.add(element);
			}
		} finally {
			if (null != input) {
				input.close();
			}
			serializerQueue.add(serializer);
		}

		return receivedData;
	}

	/**
	 * Down-loads and saves locally all platform idents associated with given {@link StorageData}.
	 * Files will be saved in passed directory.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData}.
	 * @param directory
	 *            Directory to save objects. compressBefore Should data files be compressed on the
	 *            fly before sent.
	 * @param compressBefore
	 *            Should data files be compressed on the fly before sent.
	 * @param decompressContent
	 *            If the useGzipCompression is <code>true</code>, this parameter will define if the
	 *            received content will be de-compressed. If false is passed content will be saved
	 *            to file in the same format as received, but the path of the file will be altered
	 *            with additional '.gzip' extension at the end.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws StorageException
	 *             If status of HTTP response is not successful (codes 2xx).
	 */
	public void downloadAndSavePlatformIdents(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, Path directory, boolean compressBefore, boolean decompressContent)
			throws IOException, StorageException {
		if (!Files.isDirectory(directory)) {
			throw new StorageException("Directory path supplied as the agent data saving destinationis is not valid. Given path is: " + directory.toString());
		}

		List<String> platformIdentsFiles = cmrRepositoryDefinition.getStorageService().getAgentFilesLocations(storageData);
		if (null != platformIdentsFiles) {
			this.downloadAndSaveObjects(cmrRepositoryDefinition, platformIdentsFiles, directory, compressBefore, decompressContent);
		} else {
			throw new StorageException("No agent data files could be found on the repository '" + cmrRepositoryDefinition.getName() + "' for the storage " + storageData.toString());
		}
	}

	/**
	 * Down-loads and saves locally all platform idents associated with given {@link StorageData}.
	 * Files will be saved in passed directory.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData}.
	 * @param directory
	 *            Directory to save objects.
	 * @param compressBefore
	 *            Should data files be compressed on the fly before sent.
	 * @param decompressContent
	 *            If the useGzipCompression is <code>true</code>, this parameter will define if the
	 *            received content will be de-compressed. If false is passed content will be saved
	 *            to file in the same format as received, but the path of the file will be altered
	 *            with additional '.gzip' extension at the end.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws StorageException
	 *             If status of HTTP response is not successful (codes 2xx).
	 */
	public void downloadAndSaveDataFiles(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, Path directory, boolean compressBefore, boolean decompressContent)
			throws IOException, StorageException {
		if (!Files.isDirectory(directory)) {
			throw new StorageException("Directory path supplied as the data saving destination is not valid. Given path is: " + directory.toString());
		}

		List<String> dataFiles = cmrRepositoryDefinition.getStorageService().getDataFilesLocations(storageData);
		if (null != dataFiles) {
			this.downloadAndSaveObjects(cmrRepositoryDefinition, dataFiles, directory, compressBefore, decompressContent);
		}
	}

	/**
	 * Down-loads and saves locally all indexing files associated with given {@link StorageData}.
	 * Files will be saved in passed directory.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData}.
	 * @param directory
	 *            Directory to save objects. compressBefore Should data files be compressed on the
	 *            fly before sent.
	 * @param compressBefore
	 *            Should data files be compressed on the fly before sent.
	 * @param decompressContent
	 *            If the useGzipCompression is <code>true</code>, this parameter will define if the
	 *            received content will be de-compressed. If false is passed content will be saved
	 *            to file in the same format as received, but the path of the file will be altered
	 *            with additional '.gzip' extension at the end.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws StorageException
	 *             If status of HTTP response is not successful (codes 2xx).
	 */
	public void downloadAndSaveIndexingTrees(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, Path directory, boolean compressBefore, boolean decompressContent)
			throws IOException, StorageException {
		if (!Files.isDirectory(directory)) {
			throw new StorageException("Directory path supplied as the agent data saving destination is not valid. Given path is: " + directory.toString());
		}

		List<String> indexingTreeFiles = cmrRepositoryDefinition.getStorageService().getIndexFilesLocations(storageData);
		if (null != indexingTreeFiles) {
			this.downloadAndSaveObjects(cmrRepositoryDefinition, indexingTreeFiles, directory, compressBefore, decompressContent);
		} else {
			throw new StorageException("No index data files could be found on the repository '" + cmrRepositoryDefinition.getName() + "' for the storage " + storageData.toString());
		}
	}

	/**
	 * Returns the URI of the server in format 'http://ip:port'.
	 * 
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return URI as string.
	 */
	private String getServerUri(CmrRepositoryDefinition repositoryDefinition) {
		return "http://" + repositoryDefinition.getIp() + ":" + repositoryDefinition.getPort();
	}

	/**
	 * Creates the pairs that have a channel ID as a key, and list of descriptors as value. All the
	 * descriptors in the list are associated with the channel, thus all the data described in the
	 * descriptors can be retrieved with a single HTTP/local request.
	 * 
	 * @param descriptors
	 *            Un-grouped descriptors.
	 * 
	 * @return Map of channel IDs with its descriptors.
	 */
	private Map<Integer, List<IStorageDescriptor>> createFilesGroup(List<IStorageDescriptor> descriptors) {
		Map<Integer, List<IStorageDescriptor>> filesMap = new HashMap<Integer, List<IStorageDescriptor>>();
		for (IStorageDescriptor storageDescriptor : descriptors) {
			Integer channelId = Integer.valueOf(storageDescriptor.getChannelId());
			List<IStorageDescriptor> oneFileList = filesMap.get(channelId);
			if (null == oneFileList) {
				oneFileList = new ArrayList<IStorageDescriptor>();
				filesMap.put(channelId, oneFileList);
			}
			oneFileList.add(storageDescriptor);
		}

		// sort lists
		for (Map.Entry<Integer, List<IStorageDescriptor>> entry : filesMap.entrySet()) {
			List<IStorageDescriptor> list = entry.getValue();
			Collections.sort(list, new Comparator<IStorageDescriptor>() {

				@Override
				public int compare(IStorageDescriptor o1, IStorageDescriptor o2) {
					return Long.compare(o1.getPosition(), o2.getPosition());
				}
			});
		}

		return filesMap;
	}

	/**
	 * Down-loads and saves the file from a {@link CmrRepositoryDefinition}. Files will be saved in
	 * the directory that is denoted as the given Path object. Original file names will be used.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository.
	 * @param fileNames
	 *            Name of the files.
	 * @param path
	 *            Directory where files will be saved.
	 * @param useGzipCompression
	 *            If the GZip compression should be used when files are downloaded.
	 * @param decompressContent
	 *            If the useGzipCompression is <code>true</code>, this parameter will define if the
	 *            received content will be de-compressed. If false is passed content will be saved
	 *            to file in the same format as received, but the path of the file will be altered
	 *            with additional '.gzip' extension at the end.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws StorageException
	 *             If status of HTTP response is not successful (codes 2xx).
	 */
	private void downloadAndSaveObjects(CmrRepositoryDefinition cmrRepositoryDefinition, List<String> fileNames, Path path, boolean useGzipCompression, boolean decompressContent) throws IOException,
			StorageException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		if (useGzipCompression && decompressContent) {
			httpClient.addResponseInterceptor(new GzipHttpResponseInterceptor());
		}
		for (String fileName : fileNames) {
			HttpGet httpGet = new HttpGet(getServerUri(cmrRepositoryDefinition) + fileName);
			if (useGzipCompression) {
				httpGet.addHeader("accept-encoding", "gzip");
			}
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			if (HttpStatus.valueOf(statusLine.getStatusCode()).series().equals(Series.SUCCESSFUL)) {
				HttpEntity entity = response.getEntity();
				InputStream is = null;
				try {
					is = entity.getContent();
					String[] splittedFileName = fileName.split("/");
					String originalFileName = splittedFileName[splittedFileName.length - 1];
					Path writePath = path.resolve(originalFileName);
					Files.copy(is, writePath, StandardCopyOption.REPLACE_EXISTING);
				} finally {
					if (null != is) {
						is.close();
					}
				}
			}

		}
	}

	/**
	 * Sets {@link #storageManager}.
	 * 
	 * @param storageManager
	 *            New value for {@link #storageManager}
	 */
	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	/**
	 * 
	 * @param entity
	 *            {@link HttpEntity}
	 * @return True if the GZip encoding is active.
	 */
	private static boolean isGZipContentEncoding(HttpEntity entity) {
		Header ceHeader = entity.getContentEncoding();
		if (ceHeader != null) {
			HeaderElement[] codecs = ceHeader.getElements();
			for (int i = 0; i < codecs.length; i++) {
				if (codecs[i].getName().equalsIgnoreCase("gzip")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets {@link #serializerCount}.
	 * 
	 * @param serializerCount
	 *            New value for {@link #serializerCount}
	 */
	public void setSerializerCount(int serializerCount) {
		this.serializerCount = serializerCount;
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
	 * Sets {@link #streamProvider}.
	 * 
	 * @param streamProvider
	 *            New value for {@link #streamProvider}
	 */
	public void setStreamProvider(StreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}

	/**
	 * A wrapper for the {@link HttpEntity} that will surround the entity's input stream with the
	 * {@link GZIPInputStream}.
	 * 
	 */
	private static class GzipDecompressingEntity extends HttpEntityWrapper {

		/**
		 * Default constructor.
		 * 
		 * @param entity
		 *            Entity that has the response in the GZip format.
		 */
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		/**
		 * {@inheritDoc}
		 */
		public InputStream getContent() throws IOException {
			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long getContentLength() {
			// length of uncompressed content is not known
			return -1;
		}

	}

	/**
	 * Response interceptor that alters the response entity if the encoding is gzip.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class GzipHttpResponseInterceptor implements HttpResponseInterceptor {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
			if (isGZipContentEncoding(response.getEntity())) {
				response.setEntity(new GzipDecompressingEntity(response.getEntity()));
			}
		}

	}

}
