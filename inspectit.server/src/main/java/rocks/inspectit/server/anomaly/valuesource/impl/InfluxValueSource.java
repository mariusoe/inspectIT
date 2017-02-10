package rocks.inspectit.server.anomaly.valuesource.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.valuesource.ValueSource;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.server.influx.util.QueryResultWrapper;

/**
 * @author Marius Oehler
 *
 */
@Component
public class InfluxValueSource extends ValueSource {

	public enum Function {
		MEAN, MIN;
	};

	private String measurement;

	private Map<String, String> tagMap;

	private Function function;

	@Autowired
	private InfluxDBDao influx;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT ").append(function.name()).append("(value) FROM ").append(measurement).append(" WHERE time > now() - ").append(getAggregationWindowLength()).append('s');

		if (MapUtils.isNotEmpty(tagMap)) {
			for (Entry<String, String> entry : tagMap.entrySet()) {
				queryString.append(" AND \"").append(entry.getKey()).append("\" = '").append(entry.getValue()).append('\'');
			}
		}

		QueryResultWrapper queryResult = new QueryResultWrapper(influx.query(queryString.toString()));

		if (queryResult.isEmpty()) {
			return Double.NaN;
		}

		return queryResult.getDouble(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getValues(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("...");
		}

		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT ").append(function.name()).append("(value) FROM ").append(measurement).append(" WHERE time > now() - ");
		queryString.append(count * getAggregationWindowLength());
		queryString.append('s');

		if (MapUtils.isNotEmpty(tagMap)) {
			for (Entry<String, String> entry : tagMap.entrySet()) {
				queryString.append(" AND \"").append(entry.getKey()).append("\" = '").append(entry.getValue()).append('\'');
			}
		}
		queryString.append("GROUP BY time(").append(getAggregationWindowLength()).append("s) ORDER BY time ASC");

		QueryResultWrapper queryResult = new QueryResultWrapper(influx.query(queryString.toString()));

		if (queryResult.isEmpty()) {
			return null;
		}

		double[] result = new double[count];

		for (int i = 0; i < count; i++) {
			result[i] = queryResult.getDouble(i, 1);
		}

		return result;
	}

	/**
	 * Gets {@link #measurement}.
	 *
	 * @return {@link #measurement}
	 */
	public String getMeasurement() {
		return this.measurement;
	}

	/**
	 * Sets {@link #measurement}.
	 *
	 * @param measurement
	 *            New value for {@link #measurement}
	 */
	public void setMeasurement(String measurement) {
		this.measurement = measurement;
	}

	/**
	 * Gets {@link #tagMap}.
	 *
	 * @return {@link #tagMap}
	 */
	public Map<String, String> getTagMap() {
		return this.tagMap;
	}

	/**
	 * Sets {@link #tagMap}.
	 *
	 * @param tagMap
	 *            New value for {@link #tagMap}
	 */
	public void setTagMap(Map<String, String> tagMap) {
		this.tagMap = tagMap;
	}

	/**
	 * Gets {@link #function}.
	 *
	 * @return {@link #function}
	 */
	public Function getFunction() {
		return this.function;
	}

	/**
	 * Sets {@link #function}.
	 *
	 * @param function
	 *            New value for {@link #function}
	 */
	public void setFunction(Function function) {
		this.function = function;
	}
}
