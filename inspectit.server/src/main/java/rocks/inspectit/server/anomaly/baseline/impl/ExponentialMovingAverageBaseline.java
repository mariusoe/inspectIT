package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;

/**
 * @author Marius Oehler
 *
 */
@Component
public class ExponentialMovingAverageBaseline extends AbstractBaseline<ExponentialMovingAverageBaselineDefinition> {

	private double baseline = Double.NaN;

	private double trend = Double.NaN;

	private boolean initialized = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AnomalyProcessingContext context, long time) {
		// if (!isWarmedUp(context)) {
		// return;
		// }

		// if (!initialized) {
		// initialized = true;
		//
		// long initializationWindow = * context.getConfiguration().getIntervalBaselineProcessing()
		// * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;
		// double initValue = context.getMetricProvider().getValue(time, initializationWindow,
		// TimeUnit.SECONDS);
		//
		// baseline = initValue;
		// trend = 0D;
		// }

		long aggregationWindow = context.getConfiguration().getIntervalBaselineProcessing() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;

		MetricFilter filter = new MetricFilter();
		if (context.isWarmedUp() && getDefinition().isExcludeCriticalData()) {
			filter.setUpperLimit(context.getThreshold().getThreshold(context, ThresholdType.UPPER_CRITICAL));
		}

		double value = context.getMetricProvider().getValue(filter, time, aggregationWindow, TimeUnit.SECONDS);

		if (Double.isNaN(value)) {
			return;
		}

		System.out.println(value);

		if (Double.isNaN(baseline)) {
			baseline = value;
		} else if (Double.isNaN(trend)) {
			trend = value - baseline;
			baseline = value;
		} else {
			double nextValue;
			if (context.isWarmedUp()) {
				nextValue = (getDefinition().getSmoothingFactor() * value) + ((1 - getDefinition().getSmoothingFactor()) * (baseline + trend));
				trend = (getDefinition().getTrendSmoothingFactor() * (nextValue - baseline)) + ((1 - getDefinition().getTrendSmoothingFactor()) * trend);
			} else {
				nextValue = (getDefinition().getSmoothingFactor() * value) + ((1 - getDefinition().getSmoothingFactor()) * baseline);
				trend = 0;
			}
			baseline = nextValue;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		return baseline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}

}
