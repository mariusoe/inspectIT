package info.novatec.inspectit.cmr.anomaly;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.tsdb.ITimeSeriesDatabase;
import info.novatec.inspectit.cmr.util.Converter;

import java.util.ArrayList;
import java.util.List;

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
	private final Logger log;

	/**
	 * List consisting of loaded {@link AbstractAnomalyDetectionStrategy}.
	 */
	private final List<AbstractAnomalyDetectionStrategy> detectionStrategyList;

	/**
	 * Constructor.
	 *
	 * @param log
	 *            the logger to use
	 * @param timeSeriesDatabase
	 *            the time series database to use.
	 * @param detectionStrategies
	 *            the implemented detection strategies.
	 */
	public AnomalyDetector(Logger log, ITimeSeriesDatabase timeSeriesDatabase, String detectionStrategies) {
		if (log == null) {
			log = LoggerFactory.getLogger(getClass());
		}
		if (timeSeriesDatabase == null) {
			throw new IllegalArgumentException("timeSeriesDatabase cannot be NULL.");
		}
		if (detectionStrategies == null) {
			throw new IllegalArgumentException("detectionStrategies cannot be NULL.");
		}

		this.log = log;

		detectionStrategyList = new ArrayList<>();

		// load detection strategies
		for (String detectionStrategy : detectionStrategies.split("#")) {
			try {
				AbstractAnomalyDetectionStrategy strategy = (AbstractAnomalyDetectionStrategy) Class.forName(detectionStrategy).newInstance();
				strategy.initialization(timeSeriesDatabase);
				detectionStrategyList.add(strategy);

			} catch (ClassNotFoundException e) {
				if (log.isWarnEnabled()) {
					log.warn(String.format("The detection strategy {} could not be loaded!", detectionStrategy));
				}
			} catch (InstantiationException | IllegalAccessException e) {
				if (log.isWarnEnabled()) {
					log.error(String.format("The detection strategy {} could not be instantiated!", detectionStrategy));
				}
			}
		}

		log.info("|-Following anomaly detection strategies are registered:");
		for (AbstractAnomalyDetectionStrategy strategy : detectionStrategyList) {
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
		if (log.isInfoEnabled()) {
			log.info("Starting anomaly detection.");
		}

		long currentTime = System.currentTimeMillis();

		for (AbstractAnomalyDetectionStrategy strategy : detectionStrategyList) {
			if (log.isInfoEnabled()) {
				log.info("Executing detection strategy: {}", strategy.getStrategyName());
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

		if (log.isInfoEnabled()) {
			log.info("Anomaly detection took " + Converter.nanoToMilliseconds(totalDuration) + " ms");
		}
	}
}
