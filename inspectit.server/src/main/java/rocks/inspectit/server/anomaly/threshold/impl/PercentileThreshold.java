package rocks.inspectit.server.anomaly.threshold.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.UnsupportedThresholdTypeException;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.PercentileThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class PercentileThreshold extends AbstractThreshold<PercentileThresholdDefinition> {

	private DescriptiveStatistics[] percentileStatistics = new DescriptiveStatistics[4];

	private double[] percentiles = new double[4];

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingContext context, long time) {
		int intervalLength = context.getConfiguration().getIntervalShortProcessing() * context.getConfiguration().getIntervalLongProcessingMultiplier();
		long timeWindow = AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS * intervalLength;

		if (!Double.isNaN(getDefinition().getUpperCriticalPercentile())) {
			double value = context.getMetricProvider().getPercentile(getDefinition().getUpperCriticalPercentile(), time, timeWindow, TimeUnit.SECONDS);
			addValue(context, value, 0);
		}

		if (!Double.isNaN(getDefinition().getUpperWarningPercentile())) {
			double value = context.getMetricProvider().getPercentile(getDefinition().getUpperWarningPercentile(), time, timeWindow, TimeUnit.SECONDS);
			addValue(context, value, 1);
		}

		if (!Double.isNaN(getDefinition().getLowerWarningPercentile())) {
			double value = context.getMetricProvider().getPercentile(getDefinition().getLowerWarningPercentile(), time, timeWindow, TimeUnit.SECONDS);
			addValue(context, value, 2);
		}

		if (!Double.isNaN(getDefinition().getLowerCriticalPercentile())) {
			double value = context.getMetricProvider().getPercentile(getDefinition().getLowerCriticalPercentile(), time, timeWindow, TimeUnit.SECONDS);
			addValue(context, value, 3);
		}
	}

	private void addValue(ProcessingContext context, double value, int percentileIndex) {
		if (!Double.isNaN(value)) {
			percentileStatistics[percentileIndex].addValue(value);

			if (!context.isWarmedUp() || Double.isNaN(percentiles[percentileIndex]) || !getDefinition().isExponentialSmoothed()) {
				percentiles[percentileIndex] = percentileStatistics[percentileIndex].getMean();
			} else {
				percentiles[percentileIndex] = (getDefinition().getSmoothingFactor() * percentileStatistics[percentileIndex].getMean())
						+ ((1 - getDefinition().getSmoothingFactor()) * percentiles[percentileIndex]);
			}
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getThreshold(ProcessingContext context, ThresholdType type) {
		switch (type) {
		case UPPER_CRITICAL:
			return percentiles[0];
		case UPPER_WARNING:
			return percentiles[1];
		case LOWER_WARNING:
			return percentiles[2];
		case LOWER_CRITICAL:
			return percentiles[3];
		default:
			throw new UnsupportedThresholdTypeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
		for (int i = 0; i < percentileStatistics.length; i++) {
			percentileStatistics[i] = new DescriptiveStatistics(getDefinition().getWindowSize());
		}
	}

}
