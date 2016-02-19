package info.novatec.inspectit.cmr.influxdb;

import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service class to access and push data to an InfluxDB.
 *
 * @author Marius Oehler
 *
 */
@Component
public class InfluxDBService implements InitializingBean {

	/**
	 * After this duration, the batch have to be flushed.
	 */
	private static final int BATCH_FLUSH_TIMER = 100;

	/**
	 * Size of the {@link Point} buffer.
	 */
	private static final int BATCH_BUFFER_SIZE = 2000;

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Host where InfluxDB is running.
	 */
	@Value("${influxdb.host}")
	private String host;

	/**
	 * Port of the running InfluxDB.
	 */
	@Value("${influxdb.port}")
	private int port;

	/**
	 * InfluxDB user.
	 */
	@Value("${influxdb.user}")
	private String user;

	/**
	 * Password of the InfluxDB user.
	 */
	@Value("${influxdb.passwd}")
	private String password;

	/**
	 * Database to use.
	 */
	@Value("${influxdb.database}")
	private String database;

	/**
	 * The retention policy to use.
	 */
	@Value("${influxdb.retentionPolicy}")
	private String retentionPolicy;

	/**
	 * Configured {@link InfluxDB} instance.
	 */
	private InfluxDB influxDB;

	@Override
	public void afterPropertiesSet() throws Exception {
		influxDB = InfluxDBFactory.connect("http://" + host + ":" + port, user, password);

		// Flush every BATCH_BUFFER_SIZE Points, at least every BATCH_FLUSH_TIMER
		influxDB.enableBatch(BATCH_BUFFER_SIZE, BATCH_FLUSH_TIMER, TimeUnit.MILLISECONDS);

		// Create database if it not exists
		// influxDB.createDatabase(database);

		if (log.isInfoEnabled()) {
			log.info("|-InfluxDB Service active...");
		}
	}

	/**
	 * Inserts the given {@link Point} into the database.
	 *
	 * @param dataPoint
	 *            {@link Point} to insert
	 */
	public void write(Point dataPoint) {
		if (log.isDebugEnabled()) {
			log.debug("Write data to InfluxDB: {}", dataPoint.toString());
		}
		influxDB.write(database, retentionPolicy, dataPoint);
	}

	/**
	 * Executes the given query against the connected InfluxDb.
	 *
	 * @param query
	 *            the query to execute
	 * @return the result of the query
	 */
	public QueryResult query(String query) {
		return influxDB.query(new Query(query, database));
	}

	/**
	 * Queries the database for a single value. Only the first field will be returned!
	 *
	 * @param query
	 *            query to execute
	 * @return the found object or <code>null</code> if the result was empty
	 */
	public Object querySingle(String query) {
		QueryResult queryResult = query(query);
		if (queryResult.hasError()) {
			log.warn("Query [{}] failed - Error: {}", query, queryResult.getError());
			return null;
		} else {
			try {
				return queryResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
			} catch (NullPointerException e) {
				return null;
			}
		}
	}

	/**
	 * Queries the database for a single double value. Only the first field will be returned!
	 * Basically, this is a wrapper of the {@link #querySingle(String)} method.
	 *
	 * @param query
	 *            query to execute
	 * @return the found double or <code>Double.NaN</code> if the result was empty
	 */
	public double querySingleDouble(String query) {
		Object resultObject = querySingle(query);

		if (resultObject == null) {
			return Double.NaN;
		}

		if (resultObject instanceof Double) {
			return (double) resultObject;
		} else {
			return Double.parseDouble(resultObject.toString());
		}
	}
}
