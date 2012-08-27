package info.novatec.inspectit.storage.nio.stream;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.storage.IStorageData;

import java.util.List;

/**
 * Class that is used for providing the correct instance of {@link ExtendedByteBufferOutputStream}
 * via Spring framework.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class StreamProvider {

	/**
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferOutputStream}
	 *         that has been prepared for use.
	 */
	public ExtendedByteBufferOutputStream getExtendedByteBufferOutputStream() {
		ExtendedByteBufferOutputStream stream = createExtendedByteBufferOutputStream();
		stream.prepare();
		return stream;
	}

	/**
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferOutputStream}.
	 */
	protected abstract ExtendedByteBufferOutputStream createExtendedByteBufferOutputStream();

	/**
	 * @param storageData
	 *            {@link IStorageData} to get the data for.
	 * @param descriptors
	 *            List of descriptors that point to the data.
	 * 
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferInputStream}.
	 */
	public ExtendedByteBufferInputStream getExtendedByteBufferInputStream(IStorageData storageData, List<IStorageDescriptor> descriptors) {
		ExtendedByteBufferInputStream stream = createExtendedByteBufferInputStream();
		stream.setStorageData(storageData);
		stream.setDescriptors(descriptors);
		stream.prepare();
		return stream;
	}

	/**
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferInputStream}.
	 */
	protected abstract ExtendedByteBufferInputStream createExtendedByteBufferInputStream();
}
