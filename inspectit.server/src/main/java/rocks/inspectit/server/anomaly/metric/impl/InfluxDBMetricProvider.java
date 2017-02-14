package rocks.inspectit.server.anomaly.metric.impl;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.server.influx.util.QueryResultWrapper;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class InfluxDBMetricProvider extends AbstractMetricProvider<InfluxDBMetricDefinition> {

	@Log
	private Logger log;

	@Autowired
	private InfluxDBDao influx;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue(long timeWindow, TimeUnit unit) {
		return getValue(-1L, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue(long time, long timeWindow, TimeUnit unit) {
		return getValue(null, time, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue(MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
		String select = getDefinition().getFunction().name() + "(\"" + getDefinition().getField() + "\")";
		return getSingleValue(select, filter, time, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getStandardDeviation(long timeWindow, TimeUnit unit) {
		return getStandardDeviation(-1L, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getStandardDeviation(long time, long timeWindow, TimeUnit unit) {
		return getStandardDeviation(null, time, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getStandardDeviation(MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
		String select = "STDDEV(\"" + getDefinition().getField() + "\")";
		return getSingleValue(select, filter, time, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getPercentile(double percentile, long time, long timeWindow, TimeUnit unit) {
		String select = "PERCENTILE(\"" + getDefinition().getField() + "\", " + percentile + ")";
		return getSingleValue(select, null, time, timeWindow, unit);
	}

	private double getSingleValue(String select, MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(select).append(" FROM ").append(getDefinition().getMeasurement());

		if (time < 0) {
			queryBuilder.append(" WHERE time > now() - ").append(unit.toSeconds(timeWindow)).append('s');
		} else {
			queryBuilder.append(" WHERE time <= ").append(time).append("ms AND time > ").append(time - unit.toMillis(timeWindow)).append("ms");
		}

		if (MapUtils.isNotEmpty(getDefinition().getTagMap())) {
			for (Entry<String, String> entry : getDefinition().getTagMap().entrySet()) {
				queryBuilder.append(" AND \"").append(entry.getKey()).append("\" = '").append(entry.getValue()).append('\'');
			}
		}

		if (filter != null) {
			if (!Double.isNaN(filter.getLowerLimit())) {
				queryBuilder.append(" AND \"").append(getDefinition().getField()).append("\" >= ").append(filter.getLowerLimit());
			}
			if (!Double.isNaN(filter.getUpperLimit())) {
				queryBuilder.append(" AND \"").append(getDefinition().getField()).append("\" <= ").append(filter.getUpperLimit());
			}
		}

		QueryResultWrapper queryResult = new QueryResultWrapper(influx.query(queryBuilder.toString()));

		if (queryResult.isEmpty()) {
			return Double.NaN;
		}

		return queryResult.getDouble(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getRawValues(long time, long timeWindow, TimeUnit unit) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT \"").append(getDefinition().getField()).append("\" FROM ").append(getDefinition().getMeasurement());

		if (time < 0) {
			queryBuilder.append(" WHERE time > now() - ").append(unit.toSeconds(timeWindow)).append('s');
		} else {
			queryBuilder.append(" WHERE time <= ").append(time).append("ms AND time > ").append(time - unit.toMillis(timeWindow)).append("ms");
		}

		if (MapUtils.isNotEmpty(getDefinition().getTagMap())) {
			for (Entry<String, String> entry : getDefinition().getTagMap().entrySet()) {
				queryBuilder.append(" AND \"").append(entry.getKey()).append("\" = '").append(entry.getValue()).append('\'');
			}
		}

		queryBuilder.append(" ORDER BY time ASC");

		QueryResultWrapper queryResult = new QueryResultWrapper(influx.query(queryBuilder.toString()));

		log.info("queried {} elements", queryResult.getRowCount());

		if (queryResult.isEmpty()) {
			return new double[0];
		}

		double[] result = new double[queryResult.getRowCount()];

		for (int i = 0; i < queryResult.getRowCount(); i++) {
			result[i] = queryResult.getDouble(i, 1);
		}

		return result;
	}
}
