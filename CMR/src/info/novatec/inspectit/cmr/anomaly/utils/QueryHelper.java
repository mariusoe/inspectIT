/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.utils;

import info.novatec.inspectit.cmr.tsdb.ITimeSeriesDatabase;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.QueryResult;

/**
 * Helper class for easily querying the time series database.
 *
 * @author Marius Oehler
 *
 */
public class QueryHelper {

	/**
	 * The used {@link ITimeSeriesDatabase}.
	 */
	private final ITimeSeriesDatabase database;

	/**
	 * The time which is used in the query. This will be interpreted as the current time.
	 */
	private long currentTime;

	/**
	 * @param database
	 *            the used {@link ITimeSeriesDatabase}
	 */
	public QueryHelper(ITimeSeriesDatabase database) {
		super();
		this.database = database;
	}

	/**
	 * Sets {@link #currentTime}.
	 *
	 * @param currentTime
	 *            New value for {@link #currentTime}
	 */
	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}

	/**
	 * Generates a time filter for a WHERE clause based on the given values. The origin of the used
	 * time will be provided by the {@link #getTime()} method.
	 *
	 * @param timeDuration
	 *            the size of the time span
	 * @param timeUnit
	 *            the time unit of the timeDuration
	 * @return the generated WHERE clause
	 */
	public String timeFilter(long timeDuration, TimeUnit timeUnit) {
		long startTime = currentTime - timeUnit.toMillis(timeDuration);
		return "time > " + startTime + "ms AND time < " + currentTime + "ms";
	}

	/**
	 * Returns a filter clause like "time < now". In this case 'now' is the current time.
	 *
	 * @return filter clause
	 */
	public String timeFilterNow() {
		return "time < " + currentTime + "ms";
	}

	/**
	 * Wrapper to easily query a data field from the used time series database.
	 *
	 * @param field
	 *            field to query
	 * @param measurement
	 *            the measurement of the field
	 * @param timeSpan
	 *            the duration of the time span
	 * @param timeUnit
	 *            the time unit of the timeSpan
	 * @return the value of the field or {@link Double#NaN} if no data was found
	 */
	public double queryDouble(String field, String measurement, long timeSpan, TimeUnit timeUnit) {
		return queryDouble(field, measurement, timeFilter(timeSpan, timeUnit));
	}

	/**
	 * Wrapper to easily query a data field from the used time series database.
	 *
	 * @param field
	 *            field to query
	 * @param measurement
	 *            the measurement of the field
	 * @param where
	 *            the filter clause
	 * @return the value of the field or {@link Double#NaN} if no data was found
	 */
	public double queryDouble(String field, String measurement, String where) {
		return database.queryDouble("SELECT " + field + " FROM " + measurement + " WHERE " + where + " LIMIT 1");
	}

	/**
	 * Wrapper to easily query data. The time filter matches the data located in the past.
	 *
	 * @param field
	 *            field to query
	 * @param measurement
	 *            the measurement of the field
	 * @return the resulting {@link QueryResult}
	 */
	public QueryResult query(String field, String measurement) {
		return database.query("SELECT " + field + " FROM " + measurement + " WHERE " + timeFilterNow());
	}

	/**
	 * Wrapper to easily query data in the given time span.
	 *
	 * @param field
	 *            field to query
	 * @param measurement
	 *            the measurement of the field
	 * @param timeSpan
	 *            the duration of the time span
	 * @param timeUnit
	 *            the time unit of the timeSpan
	 * @return the resulting {@link QueryResult}
	 */
	public QueryResult query(String field, String measurement, long timeSpan, TimeUnit timeUnit) {
		return database.query("SELECT " + field + " FROM " + measurement + " WHERE " + timeFilter(timeSpan, timeUnit));
	}

}
