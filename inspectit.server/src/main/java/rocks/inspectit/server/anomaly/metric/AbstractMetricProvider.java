package rocks.inspectit.server.anomaly.metric;

import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.server.anomaly.definition.metric.MetricDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractMetricProvider<E extends MetricDefinition> extends AbstractDefinitionAware<E> {

	public abstract double getValue(long timeWindow, TimeUnit unit);

	public abstract double getValue(long time, long timeWindow, TimeUnit unit);

	public abstract double getValue(MetricFilter filter, long time, long timeWindow, TimeUnit unit);

	public abstract double getStandardDeviation(long timeWindow, TimeUnit unit);

	public abstract double getStandardDeviation(long time, long timeWindow, TimeUnit unit);

	public abstract double getStandardDeviation(MetricFilter filter, long time, long timeWindow, TimeUnit unit);

	public abstract double getPercentile(double percentile, long time, long timeWindow, TimeUnit unit);

	public abstract double[] getRawValues(long time, long timeWindow, TimeUnit unit);
}
