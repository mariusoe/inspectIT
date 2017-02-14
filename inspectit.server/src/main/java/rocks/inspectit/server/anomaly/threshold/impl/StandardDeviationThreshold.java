package rocks.inspectit.server.anomaly.threshold.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.definition.threshold.StandardDeviationThresholdDefinition;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.UnsupportedThresholdTypeException;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AnomalyProcessingContext context, long time) {
		long aggregationWindow = AnomalyDetectionSystem.PROCESSING_INTERVAL_S * context.getConfiguration().getIntervalBaselineProcessing();

		// double value = context.getMetricProvider().getStandardDeviation(filter, time,
		// aggregationWindow, TimeUnit.SECONDS);

		// if ((standardDeviation != 0D) && (value > getThreshold(context,
		// ThresholdType.UPPER_CRITICAL))) {
		// return;
		// }
		//
		// if (getDefinition().isUseResiduals()) {
		// value = value - context.getBaseline().getBaseline();
		// }

		// double[] values = context.getMetricProvider().getRawValues(time, aggregationWindow,
		// TimeUnit.SECONDS);
		//
		// double upper = getThreshold(context, ThresholdType.UPPER_CRITICAL);
		//
		// DescriptiveStatistics residualStats = new DescriptiveStatistics();
		// for (double value : values) {
		// if (getDefinition().isExcludeCriticalData()) {
		// if (value > upper) {
		// continue;
		// }
		// }
		// residualStats.addValue(value - context.getBaseline().getBaseline());
		// }
		//
		// double value = residualStats.getStandardDeviation();

		MetricFilter filter = new MetricFilter();
		if (getDefinition().isExcludeCriticalData()) {
			filter.setUpperLimit(getThreshold(context, ThresholdType.UPPER_WARNING));
		}

		double value = context.getMetricProvider().getStandardDeviation(filter, time, aggregationWindow, TimeUnit.SECONDS);

		log.info("{}", value);

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
	public double getThreshold(AnomalyProcessingContext context, ThresholdType type) {
		switch (type) {
		case LOWER_CRITICAL:
			double lowerCritical = context.getBaseline().getBaseline() - (standardDeviation * getDefinition().getSigmaAmountCritical());
			return lowerCritical;
		case LOWER_WARNING:
			return Double.NaN;
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
}
