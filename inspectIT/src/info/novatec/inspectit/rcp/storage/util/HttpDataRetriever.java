package info.novatec.inspectit.rcp.storage.util;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.CombinedStorageBranch;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.storage.IStorageIdProvider;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.util.RangeDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Class responsible for retrieving the data via HTTP, and de-serializing the data into objects.
 *
 * @author Ivan Senic
 *
 */
public class HttpDataRetriever {

	/**
	 * {@link StorageManager}.
	 */
	private StorageManager storageManager;

	/**
	 * De-serializer.
	 */
	private ISerializer serializer;

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
	 * @param storageIdProvider
	 *            {@link StorageData} that points to the wanted storage.
	 * @param descriptors
	 *            Descriptors.
	 * @return List of objects in the supplied generic type. Note that if the data described in the
	 *         descriptor is not of a supplied generic type, there will be a casting exception
	 *         thrown.
	 */
	@SuppressWarnings("unchecked")
	public <E extends DefaultData> List<E> getDataViaHttp(CmrRepositoryDefinition cmrRepositoryDefinition, IStorageIdProvider storageIdProvider, List<IStorageDescriptor> descriptors) {
		Map<String, List<IStorageDescriptor>> separateFilesGroup = createFilesGroup(storageIdProvider, descriptors);
		List<E> recievedData = new ArrayList<E>();
		String serverUri = getServerUri(cmrRepositoryDefinition);

		HttpClient httpClient = new DefaultHttpClient();
		for (Map.Entry<String, List<IStorageDescriptor>> entry : separateFilesGroup.entrySet()) {
			HttpGet httpGet = new HttpGet(serverUri + entry.getKey());
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
			ByteBuffer buffer;
			try {
				buffer = httpClient.execute(httpGet, new HttpRequestHandler());
				while (buffer.hasRemaining()) {
					try {
						Object object = serializer.deserialize(buffer);
						E element = (E) object;
						recievedData.add(element);
					} catch (SerializationException e) {
						InspectIT.getDefault().createErrorDialog("Error de-serializing data from the CMR.", e, -1);
						// TODO Do we stop the process here!?
					} catch (Exception e) {
						InspectIT.getDefault().createErrorDialog("Error de-serializing data from the CMR.", e, -1);
						// TODO Do we stop the process here!?
					}
				}
			} catch (Exception e) {
				InspectIT.getDefault().createErrorDialog("Error retrieving data from the CMR via HTTP.", e, -1);
				// TODO Do we stop the process here!?
			}
		}

		return recievedData;
	}

	/**
	 * Returns the {@link PlatformIdent} associated with given storage.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            Storage to get {@link PlatformIdent}s for.
	 * @return Returns the {@link PlatformIdent} associated with given storage.
	 */
	public List<PlatformIdent> getPlatformIdentsViaHttp(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData) {
		final List<String> platformIdentsFiles;
		try {
			platformIdentsFiles = cmrRepositoryDefinition.getStorageService().getAgentFilesLocations(storageData);
		} catch (StorageException e) {
			InspectIT.getDefault().createErrorDialog("Error retrieving the agent information from the storage.", e, -1);
			return Collections.emptyList();
		}

		return this.getObjects(cmrRepositoryDefinition, platformIdentsFiles);
	}

	/**
	 * Returns the indexing tree for the storage.
	 *
	 * @param <E>
	 *            Generic.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param storageData
	 *            {@link StorageData}
	 * @return Returns the indexing tree for the storage.
	 */
	public <E extends DefaultData> IStorageTreeComponent<E> getIndexingTreeViaHttp(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData) {
		final List<String> indexingTreeFiles;
		try {
			indexingTreeFiles = cmrRepositoryDefinition.getStorageService().getIndexFilesLocations(storageData);
		} catch (StorageException e) {
			InspectIT.getDefault().createErrorDialog("Error retrieving the indexing tree information from the storage.", e, -1);
			return null;
		}

		List<IStorageTreeComponent<E>> indexingTrees = this.getObjects(cmrRepositoryDefinition, indexingTreeFiles);
		if (indexingTrees.size() == 1) {
			return indexingTrees.get(0);
		} else if (indexingTrees.size() > 1) {
			CombinedStorageBranch<E> combinedStorageBranch = new CombinedStorageBranch<E>(indexingTrees);
			return combinedStorageBranch;
		}

		return null;
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
	 * @return True if operation is successful.
	 */
	public boolean downloadAndSavePlatformIdents(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, Path directory) {
		if (!Files.isDirectory(directory) && !Files.exists(directory)) {
			return false;
		}

		final List<String> platformIdentsFiles;
		try {
			platformIdentsFiles = cmrRepositoryDefinition.getStorageService().getAgentFilesLocations(storageData);
		} catch (StorageException e) {
			InspectIT.getDefault().createErrorDialog("Error retrieving the agent information from the storage.", e, -1);
			return false;
		}
		if (null != platformIdentsFiles) {
			return this.downloadAndSaveObjects(cmrRepositoryDefinition, platformIdentsFiles, directory);
		} else {
			return false;
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
	 *            Directory to save objects.
	 * @return True if operation is successful.
	 */
	public boolean downloadAndSaveIndexingTrees(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, Path directory) {
		if (!Files.isDirectory(directory) && !Files.exists(directory)) {
			return false;
		}

		final List<String> indexingTreeFiles;
		try {
			indexingTreeFiles = cmrRepositoryDefinition.getStorageService().getIndexFilesLocations(storageData);
		} catch (StorageException e) {
			InspectIT.getDefault().createErrorDialog("Error retrieving the indexing tree information from the storage.", e, -1);
			return false;
		}

		if (null != indexingTreeFiles) {
			return this.downloadAndSaveObjects(cmrRepositoryDefinition, indexingTreeFiles, directory);
		} else {
			return false;
		}
	}

	/**
	 * Returns the URI of the server in format 'http://ip:port'.
	 *
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}.
	 * @return URI as string.
	 */
	private String getServerUri(CmrRepositoryDefinition repositoryDefinition) {
		return "http://" + repositoryDefinition.getIp() + ":" + repositoryDefinition.getPort();
	}

	/**
	 * Creates the pairs that have a file name as a key, and list of descriptors as value. All the
	 * descriptors in the list are associated with the file, thus all the data described in the
	 * descriptors can be retrieved with a single HTTP request.
	 *
	 * @param storageIdProvider
	 *            {@link StorageData} for correct file names.
	 * @param descriptors
	 *            Un-grouped descriptors.
	 * @return Map of files names with its descriptors.
	 */
	private Map<String, List<IStorageDescriptor>> createFilesGroup(IStorageIdProvider storageIdProvider, List<IStorageDescriptor> descriptors) {
		Map<String, List<IStorageDescriptor>> filesMap = new HashMap<String, List<IStorageDescriptor>>();
		for (IStorageDescriptor storageDescriptor : descriptors) {
			String fileName = storageManager.getHttpFileLocation(storageIdProvider, storageDescriptor);
			List<IStorageDescriptor> oneFileList = filesMap.get(fileName);
			if (null == oneFileList) {
				oneFileList = new ArrayList<IStorageDescriptor>();
				filesMap.put(fileName, oneFileList);
			}
			oneFileList.add(storageDescriptor);
		}

		// sort lists
		for (Map.Entry<String, List<IStorageDescriptor>> entry : filesMap.entrySet()) {
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
	 * Gets the objects that are contained on the files on the given {@link CmrRepositoryDefinition}
	 * . Note that to use this method, each object in the given file has to be stored alone in file.
	 *
	 * @param <E>
	 *            Type of returned objects. Note that {@link ClassCastException} can occur if the
	 *            files are holding objects that are not of a wanted type.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param fileNames
	 *            List of file locations.
	 * @return List of wanted objects.
	 */
	@SuppressWarnings("unchecked")
	private <E> List<E> getObjects(CmrRepositoryDefinition cmrRepositoryDefinition, List<String> fileNames) {
		List<E> returnList = new ArrayList<E>();
		HttpClient httpClient = new DefaultHttpClient();
		for (String fileName : fileNames) {
			HttpGet httpGet = new HttpGet(getServerUri(cmrRepositoryDefinition) + fileName);
			ByteBuffer byteBuffer;
			try {
				byteBuffer = httpClient.execute(httpGet, new HttpRequestHandler());
				Object deserialized = serializer.deserialize(byteBuffer);
				returnList.add((E) deserialized);
			} catch (Exception e) {
				InspectIT.getDefault().createErrorDialog("Error retrieving the agent information from the storage.", e, -1);
			}
		}
		return returnList;
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
	 * @return True if down-load off all files succeeded. It is responsibility of a caller to delete
	 *         any files that might be created if the down-load fails.
	 */
	private boolean downloadAndSaveObjects(CmrRepositoryDefinition cmrRepositoryDefinition, List<String> fileNames, Path path) {
		HttpClient httpClient = new DefaultHttpClient();
		boolean succeeded = true;
		for (String fileName : fileNames) {
			HttpGet httpGet = new HttpGet(getServerUri(cmrRepositoryDefinition) + fileName);
			ByteBuffer byteBuffer;
			try {
				byteBuffer = httpClient.execute(httpGet, new HttpRequestHandler());
				String[] splittedFileName = fileName.split("/");
				Path writePath = path.resolve(splittedFileName[splittedFileName.length - 1]);
				byte[] data = new byte[byteBuffer.remaining()];
				byteBuffer.get(data, byteBuffer.position(), byteBuffer.limit());
				Files.write(writePath, byteBuffer.array(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (Exception e) {
				succeeded = false;
				InspectIT.getDefault().createErrorDialog("Error retrieving the agent information from the storage.", e, -1);
			}
		}
		return succeeded;
	}

	/**
	 * @param storageManager
	 *            the storageManager to set
	 */
	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	/**
	 * @param serializer
	 *            the serializer to set
	 */
	public void setSerializer(ISerializer serializer) {
		this.serializer = serializer;
	}

	/**
	 * Simple handler for the HTTP requests.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class HttpRequestHandler implements ResponseHandler<ByteBuffer> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ByteBuffer handleResponse(HttpResponse response) throws IOException {
			HttpEntity entity = response.getEntity();
			ByteBuffer byteBuffer = HttpEntityContentParser.getByteContent(entity);
			return byteBuffer;
		}

	}

}
