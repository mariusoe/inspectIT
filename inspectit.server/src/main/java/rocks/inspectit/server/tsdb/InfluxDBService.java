package rocks.inspectit.server.tsdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Service class to access and push data to an InfluxDB.
 *
 * @author Marius Oehler
 *
 */
@Component
public class InfluxDBService implements InitializingBean, ITimeSeriesDatabase {

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

		if (!datbaseExists()) {
			influxDB.createDatabase(database);
		}

		if (log.isInfoEnabled()) {
			log.info("|-InfluxDB Service active...");
		}
	}

	/**
	 * Checks whether the specified database exists.
	 *
	 * @return Returns true if the database exists
	 */
	private boolean datbaseExists() {
		List<String> databases = influxDB.describeDatabases();
		for (String databaseName : databases) {
			if (databaseName.equals(database)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insert(Point dataPoint) {
		if (log.isDebugEnabled()) {
			log.debug("Write data to InfluxDB: {}", dataPoint.toString());
		}

		influxDB.write(database, retentionPolicy, dataPoint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimeSeries query(String query) {
		return convertQueryResult(influxDB.query(new Query(query, database)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object queryObject(String query) {
		TimeSeries series = query(query);
		if (series == null || !series.hasData()) {
			return null;
		} else {
			return series.getFirst().get(1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double queryDouble(String query) {
		Object resultObject = queryObject(query);

		if (resultObject == null) {
			return Double.NaN;
		}

		if (resultObject instanceof Double) {
			return (double) resultObject;
		} else {
			return Double.parseDouble(resultObject.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean queryBoolean(String query) {
		Object resultObject = queryObject(query);

		if (resultObject == null) {
			return false;
		}

		if (resultObject instanceof Boolean) {
			return ((Boolean) resultObject).booleanValue();
		} else {
			return Boolean.parseBoolean(resultObject.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBatching() {
		return influxDB.isBatchEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disableBatching() {
		influxDB.disableBatch();
	}

	/**
	 * Converts the given InfluxDB's specific {@link QueryResult} into a {@link TimeSeries} object.
	 *
	 * @param queryResult
	 *            object to convert
	 * @return a {@link TimeSeries} object
	 */
	private TimeSeries convertQueryResult(QueryResult queryResult) {
		if (queryResult.hasError()) {
			log.warn("Query failed - Error: {}", queryResult.getError());
			return null;
		} else {
			if (queryResult.getResults() != null && !queryResult.getResults().isEmpty()) {
				Result result = queryResult.getResults().get(0);
				if (result != null) {
					if (result.hasError()) {
						throw new RuntimeException(queryResult.getError());
					} else {
						if (result.getSeries() != null && !result.getSeries().isEmpty()) {
							Series series = result.getSeries().get(0);

							List<DataPoint> dataList = new ArrayList<>();

							for (List<Object> data : series.getValues()) {
								dataList.add(new DataPoint(data));
							}

							return new TimeSeries(series.getColumns(), Collections.unmodifiableList(dataList));
						}
					}
				}

			}
		}
		return null;
	}
}
