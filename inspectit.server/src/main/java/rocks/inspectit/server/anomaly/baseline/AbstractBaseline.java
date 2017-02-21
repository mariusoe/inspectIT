package rocks.inspectit.server.anomaly.baseline;

import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.BaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractBaseline<E extends BaselineDefinition> extends AbstractDefinitionAware<E> {

	public abstract void process(ProcessingContext context, long time);

	public abstract double getBaseline();

	public double getValue(ProcessingContext context, long time) {
		if (context.getConfiguration().isOperateOnAggregation()) {
			if (context.getValueStatistics().getN() > 0) {
				return context.getValueStatistics().getMean();
			} else {
				return Double.NaN;
			}
		} else {
			int intervalLength = context.getConfiguration().getIntervalShortProcessing() * context.getConfiguration().getIntervalLongProcessingMultiplier();
			long aggregationWindow = intervalLength * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;

			MetricFilter filter = new MetricFilter();

			if (getDefinition().isExcludeWarningData()) {
				if (context.getThreshold().providesThreshold(ThresholdType.UPPER_WARNING)) {
					filter.setUpperLimit(context.getThreshold().getThreshold(context, ThresholdType.UPPER_WARNING));
				}
				if (context.getThreshold().providesThreshold(ThresholdType.LOWER_WARNING)) {
					filter.setLowerLimit(context.getThreshold().getThreshold(context, ThresholdType.LOWER_WARNING));
				}
			}

			if (getDefinition().isExcludeCriticalData()) {
				if (context.getThreshold().providesThreshold(ThresholdType.UPPER_CRITICAL) && Double.isNaN(filter.getUpperLimit())) {
					filter.setUpperLimit(context.getThreshold().getThreshold(context, ThresholdType.UPPER_CRITICAL));
				}
				if (context.getThreshold().providesThreshold(ThresholdType.LOWER_CRITICAL) && Double.isNaN(filter.getUpperLimit())) {
					filter.setLowerLimit(context.getThreshold().getThreshold(context, ThresholdType.LOWER_CRITICAL));
				}
			}

			return context.getMetricProvider().getValue(filter, time, aggregationWindow, TimeUnit.SECONDS);
		}
	}
}
