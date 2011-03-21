package info.novatec.inspectit.storage;

/**
 * This interface serves as the connection between {@link StorageData} and {@link LocalStorageData},
 * in sens that both should be able to provide the ID of storage.
 * 
 * @author Ivan Senic
 * 
 */
public interface IStorageIdProvider {

	/**
	 * @return Returns the storage id.
	 */
	String getId();

	/**
	 * Returns the name of the directory where storage data is.
	 * 
	 * @return Returns the name of the directory where storage data is.
	 */
	String getStorageFolder();

}
