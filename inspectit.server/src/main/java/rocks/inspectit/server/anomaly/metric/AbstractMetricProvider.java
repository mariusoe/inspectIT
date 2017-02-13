package rocks.inspectit.server.anomaly.metric;

import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.metric.definition.AbstractMetricDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractMetricProvider<E extends AbstractMetricDefinition> {

	private E metricDefinition;

	/**
	 * Gets {@link #metricDefinition}.
	 *
	 * @return {@link #metricDefinition}
	 */
	public E getMetricDefinition() {
		return this.metricDefinition;
	}

	/**
	 * Sets {@link #metricDefinition}.
	 *
	 * @param metricDefinition
	 *            New value for {@link #metricDefinition}
	 */
	public void setMetricDefinition(E metricDefinition) {
		this.metricDefinition = metricDefinition;
	}

	public abstract double getValue(long timeWindow, TimeUnit unit);

	public abstract double getValue(long time, long timeWindow, TimeUnit unit);

	public abstract double getStandardDeviation(long time, long timeWindow, TimeUnit unit);

	public abstract double getStandardDeviation(long timeWindow, TimeUnit unit);

	public abstract double getPercentile(double percentile, long time, long timeWindow, TimeUnit unit);

}
