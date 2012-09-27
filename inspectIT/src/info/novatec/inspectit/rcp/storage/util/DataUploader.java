package info.novatec.inspectit.rcp.storage.util;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.io.File;
import java.io.IOException;

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
	 * 
	 * @param fileToUpload
	 *            File to upload.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @throws Exception
	 *             If file to upload does not exist or exception occurs during the upload.
	 */
	public void uploadFileToStorageUploads(File fileToUpload, CmrRepositoryDefinition cmrRepositoryDefinition) throws Exception {
		if (!fileToUpload.exists()) {
			throw new IOException("File to upload does not exist.");
		}
		String uri = getServerUri(cmrRepositoryDefinition) + UPLOAD_SERVLET;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpPost httpPost = new HttpPost(uri);
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			FileBody bin = new FileBody(fileToUpload);
			entity.addPart("attachment", bin);
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
