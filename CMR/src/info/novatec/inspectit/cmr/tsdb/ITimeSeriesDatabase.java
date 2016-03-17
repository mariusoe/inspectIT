/**
 *
 */
package info.novatec.inspectit.cmr.tsdb;

/**
 * Interface for time series database adapters.
 *
 * @author Marius Oehler
 *
 */
public interface ITimeSeriesDatabase {

	/**
	 * Queries the database for a single value. Only the first field will be returned!
	 *
	 * @param query
	 *            query to execute
	 * @return the found object or <code>null</code> if the result was empty
	 */
	Object queryObject(String query);

	/**
	 * Queries the database for a single boolean value. Only the first field will be returned!
	 * Basically, this is a wrapper of the {@link #queryObject(String)} method.
	 *
	 * @param query
	 *            query to execute
	 * @return the found boolean or <code>false</code> if the result was empty
	 */
	boolean queryBoolean(String query);

	/**
	 * Queries the database for a single double value. Only the first field will be returned!
	 * Basically, this is a wrapper of the {@link #queryObject(String)} method.
	 *
	 * @param query
	 *            query to execute
	 * @return the found double or <code>Double.NaN</code> if the result was empty
	 */
	double queryDouble(String query);

}
