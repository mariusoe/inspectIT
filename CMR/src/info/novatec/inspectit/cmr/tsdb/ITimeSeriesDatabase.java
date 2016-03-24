/**
 *
 */
package info.novatec.inspectit.cmr.tsdb;

import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;

/**
 * Interface for time series database adapters.
 *
 * @author Marius Oehler
 *
 */
public interface ITimeSeriesDatabase {

	/**
	 * Executes the given query against the connected InfluxDb.
	 *
	 * @param query
	 *            the query to execute
	 * @return the result of the query
	 */
	// TODO: replace QueryResult class
	QueryResult query(String query);

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

	/**
	 * Inserts the given {@link Point} into the database.
	 *
	 * @param dataPoint
	 *            {@link Point} to insert
	 */
	// TODO replace Point class
	void insert(Point dataPoint);

	/**
	 * Returns whether the implementation of the time series database is batching the insertion.
	 *
	 * @return true if the time series database uses batching
	 */
	boolean isBatching();

	/**
	 * Disables the batching functionality of the time series database.
	 */
	void disableBatching();
}
