package info.novatec.inspectit.cmr.jetty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;

/**
 * A simple file upload servlet that depends on the the org.mortbay.servlet.MultiPartFilter. The
 * filter should prepare the files that are sent in the "multipart/form-data" encoding as a list of
 * files in the request attribute {@value #MULTI_PART_FILTER_FILES}.
 * <p>
 * This servlet can be used for uploading any files. The original name and extension of the file is
 * not saved. The file will have the arbitrary name and the extension of the files can be defined by
 * setting the {@link #extension} property. Directory where the files can be saved on the server is
 * defined by the {@link #directoryToStore} property.
 * 
 * @author Ivan Senic
 * 
 */
public class FileUploadServlet extends HttpServlet implements InitializingBean {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5619516365594064035L;

	/**
	 * ID of the HTTP request attribute that holds the uploaded files. The attribute will be set by
	 * the org.mortbay.servlet.MultiPartFilter.
	 */
	private static final String MULTI_PART_FILTER_FILES = "org.mortbay.servlet.MultiPartFilter.files";

	/**
	 * Directory where the files will be stored.
	 */
	private String directoryToStore;

	/**
	 * Extension to add to the files that have been upload. If it is not set, no extension will be
	 * added.
	 */
	private String extension;

	/**
	 * {@inheritDoc}
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<?> files = (List<?>) req.getAttribute(MULTI_PART_FILTER_FILES);
		if (null != files) {
			for (int i = 0; i < files.size(); i++) {
				File file = (File) files.get(i);
				StringBuilder nameBuffer = new StringBuilder();
				nameBuffer.append(directoryToStore);
				nameBuffer.append(File.separatorChar);
				nameBuffer.append(file.getName());
				if (null != extension) {
					nameBuffer.append(extension);
				}
				File outputFile = new File(nameBuffer.toString());
				if (outputFile.exists()) {
					throw new IOException("Upload file already exists. Aborting the upload.");
				}
				file.renameTo(outputFile);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	/**
	 * Sets {@link #directoryToStore}.
	 * 
	 * @param directoryToStore
	 *            New value for {@link #directoryToStore}
	 */
	public void setDirectoryToStore(String directoryToStore) {
		this.directoryToStore = directoryToStore;
	}

	/**
	 * Sets {@link #extension}.
	 * 
	 * @param extension
	 *            New value for {@link #extension}
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Files.createDirectories(Paths.get(directoryToStore));
	}
}
