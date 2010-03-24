package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class StorageInvocationDataAccessService implements IInvocationDataAccessService {

	/**
	 * The definition.
	 */
	private StorageRepositoryDefinition storageRepositoryDefinition;

	/**
	 * Only constructor which needs the definition to be passed.
	 * 
	 * @param storageRepositoryDefinition
	 *            the definition.
	 */
	public StorageInvocationDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition) {
		this.storageRepositoryDefinition = storageRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		File file = new File(storageRepositoryDefinition.getPath() + File.separator + StorageNamingConstants.FILE_NAME_INVOCATION_OVERVIEW);
		if (file.exists()) {
			if (file.isFile()) {
				try {
					List<InvocationSequenceData> data = (List<InvocationSequenceData>) loadInvocationSequenceData(file);

					// filter the collection
					for (Iterator<InvocationSequenceData> iterator = data.iterator(); iterator.hasNext();) {
						InvocationSequenceData invocationSequenceData = iterator.next();
						if (methodId != invocationSequenceData.getMethodIdent()) {
							iterator.remove();
						}
					}

					// now sort
					if (data.size() > limit) {
						Collections.sort(data, new Comparator<InvocationSequenceData>() {
							@Override
							public int compare(InvocationSequenceData o1, InvocationSequenceData o2) {
								Long timeOne = Long.valueOf(o1.getTimeStamp().getTime());
								Long timeTwo = Long.valueOf(o2.getTimeStamp().getTime());
								return timeOne.compareTo(timeTwo);
							}
						});
					}

					return data;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException("ERROR: folder should be a file for displaying: " + file.getAbsolutePath());
			}
		} else {
			throw new RuntimeException("ERROR: file does not exist: " + file.getAbsolutePath());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit) {
		File file = new File(storageRepositoryDefinition.getPath() + File.separator + StorageNamingConstants.FILE_NAME_INVOCATION_OVERVIEW);
		if (file.exists()) {
			if (file.isFile()) {
				try {
					List<InvocationSequenceData> data = (List<InvocationSequenceData>) loadInvocationSequenceData(file);

					// sort the collection
					if (data.size() > limit) {
						Collections.sort(data, new Comparator<InvocationSequenceData>() {
							@Override
							public int compare(InvocationSequenceData o1, InvocationSequenceData o2) {
								Long timeOne = Long.valueOf(o1.getTimeStamp().getTime());
								Long timeTwo = Long.valueOf(o2.getTimeStamp().getTime());
								return timeOne.compareTo(timeTwo);
							}
						});
					}

					return data;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException("ERROR: folder should be a file for displaying: " + file.getAbsolutePath());
			}
		} else {
			throw new RuntimeException("ERROR: file does not exist: " + file.getAbsolutePath());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getInvocationSequenceDetail(InvocationSequenceData template) {
		File file = new File(storageRepositoryDefinition.getPath() + File.separator + template.getTimeStamp().getTime() + StorageNamingConstants.FILE_ENDING_INVOCATIONS);
		try {
			InvocationSequenceData data = (InvocationSequenceData) loadInvocationSequenceData(file);
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Object loadInvocationSequenceData(File fileObject) throws IOException {
		InputStream fis = null;
		InputStream bis = null;
		InputStream input = null;
		try {
			fis = new FileInputStream(fileObject);
			bis = new BufferedInputStream(fis);
			input = new GZIPInputStream(bis);

			return storageRepositoryDefinition.getXstream().fromXML(input);
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

}