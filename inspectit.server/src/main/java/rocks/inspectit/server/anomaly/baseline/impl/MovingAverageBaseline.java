package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.definition.baseline.MovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class MovingAverageBaseline extends AbstractBaseline<MovingAverageBaselineDefinition> {

	@Log
	private Logger log;

	private DescriptiveStatistics statistics = new DescriptiveStatistics();

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
		long aggregationWindow = context.getConfiguration().getIntervalBaselineProcessing() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;

		MetricFilter filter = new MetricFilter();
		if (getDefinition().isExcludeCriticalData()) {
			filter.setUpperLimit(context.getThreshold().getThreshold(context, ThresholdType.UPPER_CRITICAL));
			filter.setLowerLimit(context.getThreshold().getThreshold(context, ThresholdType.LOWER_CRITICAL));
		}

		double value = context.getMetricProvider().getValue(filter, time, aggregationWindow, TimeUnit.SECONDS);

		if (!Double.isNaN(value)) {
			statistics.addValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		return statistics.getMean();
	}
}
