package rocks.inspectit.server.anomaly.metric;

import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.context.ProcessingUnitContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.MetricDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractMetricProvider<E extends MetricDefinition> extends AbstractDefinitionAware<E> {

	public abstract void next(ProcessingUnitContext context, long time);

	public abstract double getValue();

	public abstract double getIntervalValue();

	public abstract double getStandardDeviation(long time, long timeWindow, TimeUnit unit);

	public abstract double getStandardDeviation(MetricFilter filter, long time, long timeWindow, TimeUnit unit);

	public abstract double getPercentile(double percentile, long time, long timeWindow, TimeUnit unit);

	public abstract long getCount(long time, long timeWindow, TimeUnit unit);

	public abstract long getCount(MetricFilter filter, long time, long timeWindow, TimeUnit unit);
}
