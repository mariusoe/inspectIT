package info.novatec.inspectit.cmr.anomaly.strategy;

import info.novatec.inspectit.cmr.tsdb.ITimeSeriesDatabase;
import info.novatec.inspectit.cmr.tsdb.InfluxDBService;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract anomaly detection strategy to investigate data for anomalies.
 *
 * @author Marius Oehler
 *
 */
public abstract class AbstractAnomalyDetectionStrategy {

	/**
	 * Lock to prevent parallel executions.
	 */
	private final AtomicBoolean isExecuting = new AtomicBoolean(false);

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractAnomalyDetectionStrategy.class);

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	protected InfluxDBService influx;

	/**
	 * The time series database to use.
	 */
	protected ITimeSeriesDatabase timeSeriesDatabase;

	/**
	 * The starting time of the current execution.
	 */
	private long startTime;

	/**
	 * The time of the latest execution.
	 */
	private long lastExecutionTime;

	/**
	 * Initializes all necessary variables.
	 *
	 * @param timeSeriesDatabase
	 *            the time series database to use
	 */
	public void initialization(ITimeSeriesDatabase timeSeriesDatabase) {
		this.timeSeriesDatabase = timeSeriesDatabase;
	}

	/**
	 * Starts the anomaly detection.
	 *
	 * @param startTime
	 *            the starting time of the detection
	 */
	public void execute(long startTime) {
		if (isExecuting.compareAndSet(false, true)) {
			try {
				// different values because startTime can be different (e.g. in the past)
				this.startTime = startTime;
				lastExecutionTime = System.currentTimeMillis();

				onPreExecution();
				DetectionResult detectionResult = onAnalysis();
				onPostExecution(detectionResult);
			} finally {
				isExecuting.set(false);
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("The detection strategy is running and cannot be started again before the previous run has been ended.");
			}
		}
	}

	/**
	 * Returns the name of this detection strategy.
	 *
	 * @return the name
	 */
	public abstract String getStrategyName();

	/**
	 * Returns the minimum duration between two executions of this strategy. Default: 0
	 *
	 * @return the minimum interval in milliseconds
	 */
	public long getExecutionInterval() {
		return 0;
	}

	/**
	 * Will be executed before the {@link #onAnalysis()}.
	 */
	protected void onPreExecution() {
		log.info("onPreExecution");
	}

	/**
	 * The actual logic to analyze the data.
	 *
	 * @return the detection result
	 */
	protected abstract DetectionResult onAnalysis();

	/**
	 * Will be executed after the {@link #onAnalysis()}.
	 *
	 * @param detectionResult
	 *            the result of the current analysis
	 */
	protected void onPostExecution(DetectionResult detectionResult) {
		log.info("onPostExecution");

		if (log.isInfoEnabled()) {
			log.info("Result of the anomaly detection: {}", detectionResult);
		}
	}

	/**
	 * Gets {@link #startTime}.
	 *
	 * @return {@link #startTime}
	 */
	protected long getStartTime() {
		return startTime;
	}

	/**
	 * Gets {@link #lastExecutionTime}.
	 *
	 * @return {@link #lastExecutionTime}
	 */
	public long getLastExecutionTime() {
		return lastExecutionTime;
	}
}
