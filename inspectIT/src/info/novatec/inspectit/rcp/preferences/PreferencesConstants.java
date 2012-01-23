package info.novatec.inspectit.rcp.preferences;

/**
 * Interface that just holds the all inspectIT preferences keys.
 * 
 * @author Ivan Senic
 * 
 */
public interface PreferencesConstants {
	
	/**
	 * Token used to separate the objects when list of properties of objects that are saved.
	 */
	String PREF_OBJECT_SEPARATION_TOKEN = "|";

	/**
	 * Split regex for creating preference string.
	 */
	String PREF_SPLIT_REGEX = "#";

	/**
	 * Preference key for storing CMR repository definitions.
	 */
	String CMR_REPOSITORY_DEFINITIONS = "CMR_REPOSITORY_DEFINITIONS";

	/**
	 * Preference key for columns size of our tables.
	 */
	String TABLE_COLUMN_SIZE_CACHE = "TABLE_COLUMN_SIZE_CACHE";

	/**
	 * Preference key for hidden columns of our tables.
	 */
	String HIDDEN_TABLE_COLUMN_CACHE = "HIDDEN_TABLE_COLUMN_CACHE";

	/**
	 * Preference key for columns order of our tables.
	 */
	String TABLE_COLUMN_ORDER_CACHE = "TABLE_COLUMN_ORDER_CACHE";

}
