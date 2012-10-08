package info.novatec.inspectit.rcp.storage.util;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Utility class for uploading data.
 * 
 * @author Ivan Senic
 * 
 */
public class DataUploader {

	/**
	 * Upload servlet mapping.
	 */
	private static final String UPLOAD_SERVLET = "/fileupload";

	/**
	 * Uploads the file to the {@link CmrRepositoryDefinition} storage upload folder.
	 * <p>
	 * File will be uploaded to the upload folder with the suggested path that is relative
	 * relativizePath, with addition of tmpDir to the beginning of the path.
	 * 
	 * @param fileToUpload
	 *            File to upload.
	 * @param relativizePath
	 *            Path to relativize file path.
	 * @param tmpDir
	 *            Sub-directory in the upload folder to put files into.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @throws Exception
	 *             If file to upload does not exist or exception occurs during the upload.
	 */
	public void uploadFileToStorageUploads(Path fileToUpload, Path relativizePath, String tmpDir, CmrRepositoryDefinition cmrRepositoryDefinition) throws Exception {
		List<Path> list = new ArrayList<Path>(1);
		list.add(fileToUpload);
		this.uploadFileToStorageUploads(list, relativizePath, tmpDir, cmrRepositoryDefinition);
	}

	/**
	 * Uploads the files to the {@link CmrRepositoryDefinition} storage upload folder.
	 * <p>
	 * Every file will be uploaded with the suggested path that is relative relativizePath, with
	 * addition of tmpDir to the beginning of the path.
	 * 
	 * @param filesToUpload
	 *            Files to upload as path list.
	 * @param relativizePath
	 *            Path to relativize file path.
	 * @param tmpDir
	 *            Sub-directory in the upload folder to put files into.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @throws Exception
	 *             If file to upload does not exist or exception occurs during the upload.
	 */
	public void uploadFileToStorageUploads(List<Path> filesToUpload, Path relativizePath, String tmpDir, CmrRepositoryDefinition cmrRepositoryDefinition) throws Exception {
		String uri = getServerUri(cmrRepositoryDefinition) + UPLOAD_SERVLET;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpPost httpPost = new HttpPost(uri);
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			for (Path file : filesToUpload) {
				if (Files.notExists(file)) {
					throw new IOException("File to upload (" + file + ") does not exist.");
				}
				FileBody bin = new FileBody(file.toFile());
				StringBuilder pathString = new StringBuilder(relativizePath.relativize(file).toString());
				if (null != tmpDir) {
					pathString.insert(0, tmpDir + File.separator);
				}
				entity.addPart(pathString.toString(), bin);
			}
			httpPost.setEntity(entity);
			httpClient.execute(httpPost);
		} finally {
			httpClient.getConnectionManager().shutdown();
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

}
