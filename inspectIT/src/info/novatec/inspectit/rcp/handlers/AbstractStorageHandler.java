package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.repository.service.cmr.InvocationDataAccessService;
import info.novatec.inspectit.rcp.repository.service.storage.StorageNamingConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * Abstract storage handler class which provides some common methods to store
 * data into files.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractStorageHandler extends AbstractHandler {

	/**
	 * The xstream object which is used to transform the objects into a saveable
	 * state.
	 */
	private XStream xstream = new XStream(new JettisonMappedXmlDriver());

	/**
	 * Save invocation sequences into a specific directory.
	 * 
	 * @param dataAccessService
	 *            the service which is used to get the details of these
	 *            invocation sequences.
	 * @param dir
	 *            the directory to save the invocation sequences to.
	 * @param iterator
	 *            the iterator over the invocation sequences.
	 * @throws ExecutionException
	 *             if an exception occurred during execution.
	 */
	protected void saveInvocationSequences(InvocationDataAccessService dataAccessService, File dir, Iterator<InvocationSequenceData> iterator) throws ExecutionException {
		List<InvocationSequenceData> invocations = new ArrayList<InvocationSequenceData>();
		for (; iterator.hasNext();) {
			InvocationSequenceData invocationSequenceData = iterator.next();
			invocations.add(invocationSequenceData);

			OutputStream fos = null;
			OutputStream bos = null;

			// create path
			StringBuilder path = new StringBuilder();
			path.append(dir.getAbsolutePath());
			path.append(File.separator);
			path.append(invocationSequenceData.getTimeStamp().getTime());
			path.append(StorageNamingConstants.FILE_ENDING_INVOCATIONS);

			// create file
			File file = new File(path.toString());

			// Now load the whole tree for each one and save it
			InputStream input = dataAccessService.getStreamForInvocationSequence(invocationSequenceData);
			try {
				fos = new FileOutputStream(file);
				bos = new BufferedOutputStream(fos);

				byte[] b = new byte[4096];
				int read = 0;

				// Read the whole stream and write it into the buffered
				// stream
				while ((read = input.read(b)) != -1) {
					bos.write(b, 0, read);
				}
				bos.flush();
			} catch (FileNotFoundException e) {
				throw new ExecutionException("File not found while saving invocation sequences!", e);
			} catch (IOException e) {
				throw new ExecutionException("IO Exception while saving invocation sequences!", e);
			} finally {
				try {
					if (null != fos) {
						fos.close();
					}
					if (null != bos) {
						bos.close();
					}
					if (null != input) {
						input.close();
					}
				} catch (IOException e) {
					// we do not care about this
				}
			}
		}

		saveInvocationOverview(dir, invocations);
	}

	/**
	 * Save the overview list of the invocation sequences.
	 * 
	 * @param dir
	 *            the directory to save to.
	 * @param invocations
	 *            the list of invocations to save.
	 * @throws ExecutionException
	 *             if an exception occurred during execution.
	 */
	private void saveInvocationOverview(File dir, List<InvocationSequenceData> invocations) throws ExecutionException {
		StringBuilder path = new StringBuilder();
		path.append(dir.getAbsolutePath());
		path.append(File.separator);
		path.append(StorageNamingConstants.FILE_NAME_INVOCATION_OVERVIEW);

		streamToFile(path.toString(), invocations);
	}

	/**
	 * Saves the platform object into the specified directory.
	 * 
	 * @param dir
	 *            the directory to save to.
	 * @param platformIdent
	 *            the platform ident object to save.
	 * @throws ExecutionException
	 *             if an exception occurred during execution.
	 */
	protected void savePlatform(File dir, PlatformIdent platformIdent) throws ExecutionException {
		StringBuilder path = new StringBuilder();
		path.append(dir.getAbsolutePath());
		path.append(File.separator);
		path.append(StorageNamingConstants.FILE_NAME_PLATFORM);

		streamToFile(path.toString(), platformIdent);
	}

	/**
	 * Streams an object into the file denoted by the specified path.
	 * 
	 * @param path
	 *            the path to stream the object to.
	 * @param object
	 *            the object to stream/save.
	 * @throws ExecutionException
	 *             if an exception occurred during execution.
	 */
	private void streamToFile(String path, Object object) throws ExecutionException {
		// create file
		File file = new File(path);

		OutputStream fos = null;
		OutputStream bos = null;
		OutputStream output = null;
		try {
			// create streams
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			output = new GZIPOutputStream(bos);

			// store into json file
			xstream.toXML(object, output);
			output.flush();
		} catch (IOException e) {
			throw new ExecutionException("IO Exception while saving invocation sequences!", e);
		} finally {
			try {
				if (null != output) {
					output.close();
				}
				if (null != bos) {
					bos.close();
				}
				if (null != fos) {
					fos.close();
				}
			} catch (IOException e) {
				// ignore the exception
			}
		}
	}

	/**
	 * The directory to delete, cannot be empty!
	 * 
	 * @param path
	 *            the path to the directory.
	 */
	protected void deleteDirectory(File path) {
		if (!path.isDirectory()) {
			throw new RuntimeException("Cannot delete path because it is not a folder: " + path.getAbsolutePath());
		}
		if ("".equals(path.getName())) {
			throw new RuntimeException("Cannot delete root folder!");
		}

		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				files[i].delete();
			}
			path.delete();
		}
	}

}
