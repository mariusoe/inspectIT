package info.novatec.inspectit.storage.processor.write;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

/**
 * Special type of processor that performs operations on the elements that have been written to
 * storage.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractWriteDataProcessor {

	/**
	 * Processes one {@link DefaultData} object. This method will check is
	 * {@link #canBeProcessed(DefaultData)} is true, and then delegate the processing to the
	 * {@link #processData(DefaultData)} method.
	 * 
	 * @param defaultData
	 *            Default data object.
	 */
	public void process(DefaultData defaultData) {
		if (canBeProcessed(defaultData)) {
			processData(defaultData);
		}
	}

	/**
	 * Concrete method for processing. IMplemented by sub-classeS.
	 * 
	 * @param defaultData
	 *            Default data object.
	 */
	protected abstract void processData(DefaultData defaultData);

	/**
	 * Returns if the {@link DefaultData} object can be processed by this
	 * {@link AbstractDataProcessor}.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @return True if data can be processed, false otherwise.
	 */
	public abstract boolean canBeProcessed(DefaultData defaultData);

	/**
	 * Called on the preparation of the storage.
	 * <p>
	 * Sublcasses may override.
	 * 
	 * @param storageManager
	 *            Storage manager to help in performing tasks.
	 * @param storageWriter
	 *            writer that is being finalized
	 * @param storageData
	 *            {@link StorageData} that represents storage to be finalized.
	 * @throws Exception
	 *             If any exception occurs.
	 */
	public void onPrepare(StorageManager storageManager, StorageWriter storageWriter, StorageData storageData) throws Exception {
	}

	/**
	 * Called on the finalization of the storage.
	 * <p>
	 * Sublcasses may override.
	 * 
	 * @param storageManager
	 *            Storage manager to help in performing tasks.
	 * @param storageWriter
	 *            writer that is being finalized
	 * @param storageData
	 *            {@link StorageData} that represents storage to be finalized.
	 * @throws Exception
	 *             If any exception occurs.
	 */
	public void onFinalization(StorageManager storageManager, StorageWriter storageWriter, StorageData storageData) throws Exception {
	}
}
