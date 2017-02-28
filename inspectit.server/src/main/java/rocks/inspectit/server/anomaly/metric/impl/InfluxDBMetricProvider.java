package rocks.inspectit.server.anomaly.metric.impl;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.server.influx.util.QueryResultWrapper;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.InfluxDBMetricDefinition;

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

	private double currentValue = Double.NaN;

	private DescriptiveStatistics valueStatistics = new DescriptiveStatistics();

	private long lastTime = 0L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void next(ProcessingUnitContext context, long time) {
		if (lastTime <= 0L) {
			initialize(context, time);
			return;
		}

		String selectString = getDefinition().getFunction().name() + "(\"" + getDefinition().getField() + "\")";

		QueryResultWrapper queryResult = query(selectString, null, lastTime, time);

		lastTime = time;

		if (queryResult.isEmpty()) {
			currentValue = getDefinition().getDefaultValue();
		} else {
			currentValue = queryResult.getDouble(0, 1);
		}

		valueStatistics.addValue(currentValue);
	}

	private void initialize(ProcessingUnitContext context, long time) {
		valueStatistics.setWindowSize(context.getConfiguration().getIntervalLongProcessingMultiplier());
		lastTime = time;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		return currentValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getIntervalValue() {
		switch (getDefinition().getFunction()) {
		case MAX:
			return valueStatistics.getMax();
		case MIN:
			return valueStatistics.getMin();
		case MEDIAN: // basically not correct
		case COUNT:
		case MEAN:
		case SUM:
		default:
			return valueStatistics.getMean();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}

	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// public double getValue(long timeWindow, TimeUnit unit) {
	// return getValue(-1L, timeWindow, unit);
	// }
	//
	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// public double getValue(long time, long timeWindow, TimeUnit unit) {
	// return getValue(null, time, timeWindow, unit);
	// }
	//
	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// public double getValue(MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
	// String select = getDefinition().getFunction().name() + "(\"" + getDefinition().getField() +
	// "\")";
	// return getDoubleValue(select, filter, time, timeWindow, unit);
	// }

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
		String select;
		String from;
		QueryResultWrapper queryResult;

		if (getDefinition().isOpperateOnAggregation()) {
			select = "STDDEV(\"value\")";
			from = "inspectit_anomaly";
			queryResult = query(select, from, getDefinition().getParentConfiguration().getId(), filter, time - unit.toMillis(timeWindow), time);
		} else {
			select = "STDDEV(\"" + getDefinition().getField() + "\")";
			from = getDefinition().getMeasurement();
			queryResult = query(select, from, null, filter, time - unit.toMillis(timeWindow), time);
		}


		if (queryResult.isEmpty()) {
			return Double.NaN;
		}

		return queryResult.getDouble(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getPercentile(double percentile, long time, long timeWindow, TimeUnit unit) {
		String select = "PERCENTILE(\"" + getDefinition().getField() + "\", " + percentile + ")";
		return getDoubleValue(select, null, time, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCount(long time, long timeWindow, TimeUnit unit) {
		return getCount(null, time, timeWindow, unit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCount(MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
		String select = "COUNT(\"" + getDefinition().getField() + "\")";
		return getLongValue(select, filter, time, timeWindow, unit);
	}

	private double getDoubleValue(String select, MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
		QueryResultWrapper queryResult = query(select, filter, time, timeWindow, unit);

		if (queryResult.isEmpty()) {
			return Double.NaN;
		}

		return queryResult.getDouble(0, 1);
	}

	private long getLongValue(String select, MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
		QueryResultWrapper queryResult = query(select, filter, time, timeWindow, unit);

		if (queryResult.isEmpty()) {
			return 0;
		}

		return queryResult.getDouble(0, 1).longValue();
	}

	private QueryResultWrapper query(String select, MetricFilter filter, long time, long timeWindow, TimeUnit unit) {
		long fromTime = time - unit.toMillis(timeWindow);
		return query(select, filter, fromTime, time);
	}

	private QueryResultWrapper query(String select, MetricFilter filter, long fromTime, long toTime) {
		return query(select, getDefinition().getMeasurement(), null, filter, fromTime, toTime);
	}

	private QueryResultWrapper query(String select, String from, String cid, MetricFilter filter, long fromTime, long toTime) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(select).append(" FROM ").append(from);

		queryBuilder.append(" WHERE time > ").append(fromTime).append("ms AND time <= ").append(toTime).append("ms");

		if (MapUtils.isNotEmpty(getDefinition().getTags())) {
			for (Entry<String, String> entry : getDefinition().getTags().entrySet()) {
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

		if (cid != null) {
			queryBuilder.append(" AND \"configuration_id\" = '").append(cid).append('\'');
		}

		return new QueryResultWrapper(influx.query(queryBuilder.toString()));
	}
}
