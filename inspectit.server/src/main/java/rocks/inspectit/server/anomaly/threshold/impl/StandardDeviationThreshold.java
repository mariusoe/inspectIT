package rocks.inspectit.server.anomaly.threshold.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.UnsupportedThresholdTypeException;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.StandardDeviationThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class StandardDeviationThreshold extends AbstractThreshold<StandardDeviationThresholdDefinition> {

	@Log
	private Logger log;

	private DescriptiveStatistics statistics = new DescriptiveStatistics();

	private double standardDeviation = Double.NaN;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
		statistics.setWindowSize(getDefinition().getWindowSize());
	}

	private MetricFilter getMetricFilter(ProcessingContext context) {
		MetricFilter filter = new MetricFilter();

		if (getDefinition().isExcludeWarningData()) {
			if (providesThreshold(ThresholdType.UPPER_WARNING)) {
				filter.setUpperLimit(getThreshold(context, ThresholdType.UPPER_WARNING));
			}
			if (providesThreshold(ThresholdType.LOWER_WARNING)) {
				filter.setLowerLimit(getThreshold(context, ThresholdType.LOWER_WARNING));
			}
		}

		if (getDefinition().isExcludeCriticalData()) {
			if (providesThreshold(ThresholdType.UPPER_CRITICAL) && Double.isNaN(filter.getUpperLimit())) {
				filter.setUpperLimit(getThreshold(context, ThresholdType.UPPER_CRITICAL));
			}
			if (providesThreshold(ThresholdType.LOWER_CRITICAL) && Double.isNaN(filter.getUpperLimit())) {
				filter.setLowerLimit(getThreshold(context, ThresholdType.LOWER_CRITICAL));
			}
		}

		return filter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingContext context, long time) {
		double value;
		if (context.getConfiguration().isOperateOnAggregation()) {
			value = context.getValueStatistics().getStandardDeviation();
		} else {

			int intervalLength = context.getConfiguration().getIntervalShortProcessing() * context.getConfiguration().getIntervalLongProcessingMultiplier();
			long aggregationWindow = AnomalyDetectionSystem.PROCESSING_INTERVAL_S * intervalLength;

			MetricFilter filter = getMetricFilter(context);
			value = context.getMetricProvider().getStandardDeviation(filter, time, aggregationWindow, TimeUnit.SECONDS);
		}

		if (!Double.isNaN(value)) {
			statistics.addValue(value);

			if (Double.isNaN(standardDeviation) || !getDefinition().isExponentialSmoothed()) {
				standardDeviation = statistics.getMean();
			} else {
				standardDeviation = (getDefinition().getSmoothingFactor() * statistics.getMean()) + ((1 - getDefinition().getSmoothingFactor()) * standardDeviation);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getThreshold(ProcessingContext context, ThresholdType type) {
		switch (type) {
		case LOWER_CRITICAL:
			double lowerCritical = context.getBaseline().getBaseline() - (standardDeviation * getDefinition().getSigmaAmountCritical());
			return lowerCritical;
		case LOWER_WARNING:
			double lowerWarning = context.getBaseline().getBaseline() - (standardDeviation * getDefinition().getSigmaAmountWarning());
			return lowerWarning;
		case UPPER_CRITICAL:
			double upperCritical = context.getBaseline().getBaseline() + (standardDeviation * getDefinition().getSigmaAmountCritical());
			return upperCritical;
		case UPPER_WARNING:
			double upperWarning = context.getBaseline().getBaseline() + (standardDeviation * getDefinition().getSigmaAmountWarning());
			return upperWarning;
		default:
			throw new UnsupportedThresholdTypeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean providesThreshold(rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType type) {
		switch (type) {
		case LOWER_CRITICAL:
		case LOWER_WARNING:
		case UPPER_CRITICAL:
		case UPPER_WARNING:
			return true;
		default:
			throw new UnsupportedThresholdTypeException();
		}
	}
}
