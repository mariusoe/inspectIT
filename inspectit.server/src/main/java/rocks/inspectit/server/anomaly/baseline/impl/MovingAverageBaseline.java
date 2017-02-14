package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;
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
		statistics.setWindowSize(getBaselineDefinition().getWindowSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AnomalyProcessingContext context, long time) {
		long aggregationWindow = context.getConfiguration().getIntervalBaselineProcessing() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;

		double value = context.getMetricProvider().getValue(time, aggregationWindow, TimeUnit.SECONDS);

		statistics.addValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		return statistics.getMean();
	}
}
