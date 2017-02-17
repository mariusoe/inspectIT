package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class ExponentialMovingAverageBaseline extends AbstractBaseline<ExponentialMovingAverageBaselineDefinition> {

	private double baseline = Double.NaN;

	private double trend = Double.NaN;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingContext context, long time) {
		long aggregationWindow = context.getConfiguration().getIntervalLongProcessing() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;

		MetricFilter filter = new MetricFilter();
		if (context.isWarmedUp()) {
			// TODO check for thd
			if (getDefinition().isExcludeWarningData()) {
				filter.setUpperLimit(context.getThreshold().getThreshold(context, ThresholdType.UPPER_WARNING));
			} else if (getDefinition().isExcludeCriticalData()) {
				filter.setUpperLimit(context.getThreshold().getThreshold(context, ThresholdType.UPPER_CRITICAL));
			}
		}

		double value = context.getMetricProvider().getValue(filter, time, aggregationWindow, TimeUnit.SECONDS);

		if (Double.isNaN(value)) {
			return;
		}

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
