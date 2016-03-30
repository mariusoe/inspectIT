package info.novatec.inspectit.cmr.anomaly;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.util.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class executes the anomaly detection and functions as a manager.
 *
 * @author Marius Oehler
 *
 */
public class AnomalyDetector {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AnomalyDetector.class);;

	/**
	 * List consisting of loaded {@link AbstractAnomalyDetectionStrategy}.
	 */
	private final AbstractAnomalyDetectionStrategy[] detectionStrategies;

	/**
	 * Constructor.
	 *
	 * @param detectionStrategies
	 *            the implemented detection strategies.
	 */
	@SafeVarargs
	public AnomalyDetector(AbstractAnomalyDetectionStrategy... detectionStrategies) {
		if (detectionStrategies == null) {
			throw new IllegalArgumentException("detectionStrategies cannot be NULL.");
		}

		this.detectionStrategies = detectionStrategies;

		log.info("|-Following anomaly detection strategies are registered:");
		for (AbstractAnomalyDetectionStrategy strategy : detectionStrategies) {
			log.info("||-" + strategy.getStrategyName() + " (Interval: " + strategy.getExecutionInterval() + " ms)");
		}
	}

	/**
	 * Starts the anomaly detection.
	 *
	 * @param startTime
	 *            The start time of the anomaly detection. In most cases, this is the current time.
	 */
	public void execute(long startTime) {
		if (log.isDebugEnabled()) {
			log.debug("Starting anomaly detection.");
		}

		long currentTime = System.currentTimeMillis();

		for (AbstractAnomalyDetectionStrategy strategy : detectionStrategies) {
			if (log.isDebugEnabled()) {
				log.debug("Executing detection strategy: {}", strategy.getStrategyName());
			}

			long nextExecutionTime = strategy.getLastExecutionTime() + strategy.getExecutionInterval();
			if (currentTime >= nextExecutionTime) {
				try {
					strategy.execute(startTime);
				} catch (Exception e) {
					if (log.isErrorEnabled()) {
						log.error("The detection strategy {} failed.." + strategy.getStrategyName(), e);
					}
				}
			}
		}

		long totalDuration = System.nanoTime() - startTime;

		if (log.isDebugEnabled()) {
			log.debug("Anomaly detection took " + Converter.nanoToMilliseconds(totalDuration) + " ms");
		}
	}
}
