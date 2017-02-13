package rocks.inspectit.server.anomaly.metric.provider;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.metric.definition.InfluxDBMetricDefinition;
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
	public double getValue(long timeWindow, TimeUnit unit) {
		return getValue(-1L, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue(long time, long timeWindow, TimeUnit unit) {
		String select = getMetricDefinition().getFunction().name() + "(\"" + getMetricDefinition().getField() + "\")";
		return getSingleValue(select, time, timeWindow, unit);
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
		String select = "STDDEV(\"" + getMetricDefinition().getField() + "\")";
		return getSingleValue(select, time, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getPercentile(double percentile, long time, long timeWindow, TimeUnit unit) {
		String select = "PERCENTILE(\"" + getMetricDefinition().getField() + "\", " + percentile + ")";
		return getSingleValue(select, time, timeWindow, unit);
	}

	private double getSingleValue(String select, long time, long timeWindow, TimeUnit unit) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(select).append(" FROM ").append(getMetricDefinition().getMeasurement());

		if (time < 0) {
			queryBuilder.append(" WHERE time > now() - ").append(unit.toSeconds(timeWindow)).append('s');
		} else {
			queryBuilder.append(" WHERE time <= ").append(time).append("ms AND time > ").append(time - unit.toMillis(timeWindow)).append("ms");
		}

		if (MapUtils.isNotEmpty(getMetricDefinition().getTagMap())) {
			for (Entry<String, String> entry : getMetricDefinition().getTagMap().entrySet()) {
				queryBuilder.append(" AND \"").append(entry.getKey()).append("\" = '").append(entry.getValue()).append('\'');
			}
		}

		QueryResultWrapper queryResult = new QueryResultWrapper(influx.query(queryBuilder.toString()));

		if (queryResult.isEmpty()) {
			return Double.NaN;
		}

		return queryResult.getDouble(0, 1);
	}
}
