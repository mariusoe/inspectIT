package info.novatec.inspectit.rcp.repository.service.storage;

import java.io.File;

/**
 * This interface contains some constants about folder and file names etc.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface StorageNamingConstants {

	/**
	 * The directory if file-based storage is selected.
	 */
	String DEFAULT_STORAGE_DIRECTORY = "data" + File.separator;
	
	/**
	 * The directory if file-based storage is selected.
	 */
	String DEFAULT_TEMP_DIRECTORY = "tmp" + File.separator;

	/**
	 * The name of the file containing the platform definition information like
	 * sensors etc.
	 */
	String FILE_NAME_PLATFORM = "platform.it";

	/**
	 * The file ending for a single invocation.
	 */
	String FILE_ENDING_INVOCATIONS = ".inv";

	/**
	 * The name of the invocation overview file.
	 */
	String FILE_NAME_INVOCATION_OVERVIEW = "overview" + FILE_ENDING_INVOCATIONS;
	
	/**
	 * The file ending for a whole data set
	 */
	String FILE_ENDING_DATA = ".itd";

}
