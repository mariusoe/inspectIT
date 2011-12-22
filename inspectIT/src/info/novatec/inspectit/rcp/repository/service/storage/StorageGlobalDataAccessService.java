package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Storage based global data access server.
 * 
 * @author Patrice Bouillet
 * 
 */
public class StorageGlobalDataAccessService implements IGlobalDataAccessService {

	/**
	 * The definition.
	 */
	private StorageRepositoryDefinition storageRepositoryDefinition;
	

	/**
	 * Only constructor which needs a root path to be passed.
	 * 
	 * @param storageRepositoryDefinition
	 *            the root path.
	 */
	public StorageGlobalDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition) {
		this.storageRepositoryDefinition = storageRepositoryDefinition;
	}

	@Override
	public List<PlatformIdent> getConnectedAgents() {
		// first, we have to analyze this directory with all its sub-directories
		// and see if we can find some saved data.
		File file = new File(storageRepositoryDefinition.getPath());
		List<PlatformIdent> platformIdents = new ArrayList<PlatformIdent>();

		if (file.exists()) {
			if (file.isDirectory()) {
				recursiveTraversal(file, platformIdents);
			} else {
				throw new RuntimeException("Path is not a folder: " + storageRepositoryDefinition.getPath());
			}
		}

		return platformIdents;
	}

	/**
	 * Traverse through the directory hierarchy and find our saved data.
	 * 
	 * @param fileObject
	 *            The file/folder to scan.
	 * @param platformIdents
	 *            The already found platform ident objects.
	 */
	private void recursiveTraversal(File fileObject, List<PlatformIdent> platformIdents) {
		if (fileObject.isDirectory()) {
			File allFiles[] = fileObject.listFiles();
			for (File aFile : allFiles) {
				recursiveTraversal(aFile, platformIdents);
			}
		} else if (fileObject.isFile()) {
			if (StorageNamingConstants.FILE_NAME_PLATFORM.equals(fileObject.getName())) {
				PlatformIdent platformIdent;
				try {
					platformIdent = loadPlatformIdent(fileObject);
					String folder = fileObject.getParentFile().getAbsolutePath();
					String folderName = fileObject.getParentFile().getName();
					if (platformIdent instanceof StoragePlatformIdent) {
						// was stored before, thus we modify the metadata
						StoragePlatformIdent platform = (StoragePlatformIdent) platformIdent;
						platform.setPath(folder);
						platform.setFolderName(folderName);
					} else {
						platformIdent = new StoragePlatformIdent(platformIdent, folder, folderName);
					}
					platformIdents.add(platformIdent);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Load the platform ident object from the passed file.
	 * 
	 * @param fileObject
	 *            The file object.
	 * @return the platform ident object
	 * @throws IOException
	 *             throws IOException in case of something goes wrong while readin
	 */
	private PlatformIdent loadPlatformIdent(File fileObject) throws IOException {
		InputStream fis = null;
		InputStream bis = null;
		InputStream input = null;
		try {
			fis = new FileInputStream(fileObject);
			bis = new BufferedInputStream(fis);
			input = new GZIPInputStream(bis);

			return (PlatformIdent) storageRepositoryDefinition.getXstream().fromXML(input);
		} finally {
			try {
				if (null != input) {
					input.close();
				}
				if (null != bis) {
					bis.close();
				}
				if (null != fis) {
					fis.close();
				}
			} catch (IOException e) {
				// ignore the exception
			}
		}
	}

	/**
	 * Not supported.
	 */
	public List<? extends DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	public DefaultData getLastDataObject(DefaultData template) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	public List<? extends DefaultData> getDataObjectsSinceId(DefaultData template) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	public List<? extends DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	public List<? extends DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		throw new UnsupportedOperationException();
	}

}
