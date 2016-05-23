package rocks.inspectit.server.anomaly.strategy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import rocks.inspectit.server.anomaly.strategy.DetectionResult;
import rocks.inspectit.server.anomaly.strategy.DetectionResult.Status;

/**
 * Concrete implementation of {@link AbstractAnomalyDetectionStrategy}. This is only a dummy
 * strategy for testing purpose.
 *
 * @author Marius Oehler
 *
 */
public class DummyStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractAnomalyDetectionStrategy.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStrategyName() {
		return "DummyStrategy";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetectionResult onAnalysis() {
		log.info("start analysis..");

		try {
			log.info("I'm doing some load..");
			Thread.sleep((long) (Math.random() * 1000));
		} catch (Exception e) {
		}

		return DetectionResult.make(Status.UNKNOWN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getExecutionInterval() {
		return 15000;
	}
}
