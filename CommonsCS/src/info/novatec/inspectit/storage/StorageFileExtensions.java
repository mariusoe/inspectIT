package info.novatec.inspectit.storage;

/**
 * Extension for the files where storage data is stored.
 * 
 * @author Ivan Senic
 * 
 */
public interface StorageFileExtensions {

	/**
	 * Extension of the storage index files.
	 */
	String INDEX_FILE_EXT = ".index";

	/**
	 * Extension of the files that hold real data.
	 */
	String DATA_FILE_EXT = ".itdata";

	/**
	 * Extension of the local storage info files.
	 */
	String LOCAL_STORAGE_FILE_EXT = ".local";

	/**
	 * Extension of the storage info files.
	 */
	String STORAGE_FILE_EXT = ".storage";

	/**
	 * Extension of the storage agent files.
	 */
	String AGENT_FILE_EXT = ".agent";

	/**
	 * Extension for storage files that are zipped for export/import.
	 */
	String ZIP_STORAGE_EXT = ".itsd";

}
