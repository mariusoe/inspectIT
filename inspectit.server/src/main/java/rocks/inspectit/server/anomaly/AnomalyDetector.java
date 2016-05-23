package rocks.inspectit.server.anomaly;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import rocks.inspectit.server.util.Converter;

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
	 * Collection consisting of loaded {@link AbstractAnomalyDetectionStrategy}.
	 */
	private final Collection<AbstractAnomalyDetectionStrategy> detectionStrategies;

	/**
	 * Constructor.
	 *
	 * @param detectionStrategies
	 *            the implemented detection strategies.
	 */
	public AnomalyDetector(Collection<AbstractAnomalyDetectionStrategy> detectionStrategies) {
		if (detectionStrategies == null) {
			throw new IllegalArgumentException("detectionStrategies cannot be NULL.");
		}

		this.detectionStrategies = detectionStrategies;

		if (log.isInfoEnabled()) {
			if (detectionStrategies.isEmpty()) {
				log.info("||-No detection strategies are registered");
			} else {
				log.info("||-Following anomaly detection strategies are registered:");
				for (AbstractAnomalyDetectionStrategy strategy : detectionStrategies) {
					log.info("|||-" + strategy.getStrategyName() + " (Interval: " + strategy.getExecutionInterval() + " ms)");
				}
			}
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
