package info.novatec.inspectit.cmr.anomaly.strategy;

import info.novatec.inspectit.cmr.anomaly.utils.QueryHelper;
import info.novatec.inspectit.cmr.tsdb.ITimeSeriesDatabase;

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
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractAnomalyDetectionStrategy.class);

	/**
	 * Determines whether the strategy is running. Lock to prevent parallel executions.
	 */
	private final AtomicBoolean isExecuting = new AtomicBoolean(false);

	/**
	 * The time series database to use.
	 */
	protected ITimeSeriesDatabase timeSeriesDatabase;

	/**
	 * Helper for easily querying of the time series database.
	 */
	protected QueryHelper queryHelper;

	/**
	 * The starting time of the current execution.
	 */
	private long time;

	/**
	 * The time of the latest execution.
	 */
	private long lastExecutionTime;

	/**
	 * The time which was used in the last execution.
	 */
	private long lastTime = -1;

	/**
	 * The actual logic to analyze the data.
	 *
	 * @return the detection result
	 */
	protected abstract DetectionResult onAnalysis();

	/**
	 * Returns the name of this detection strategy.
	 *
	 * @return the name
	 */
	public abstract String getStrategyName();

	/**
	 * Initializes all necessary variables.
	 *
	 * @param timeSeriesDatabase
	 *            the time series database to use
	 */
	public void initialization(ITimeSeriesDatabase timeSeriesDatabase) {
		this.timeSeriesDatabase = timeSeriesDatabase;
		this.queryHelper = new QueryHelper(timeSeriesDatabase);
	}

	/**
	 * Starts the anomaly detection.
	 *
	 * @param time
	 *            the time to use as current time in the detection
	 */
	public void execute(long time) {
		if (isExecuting.compareAndSet(false, true)) {
			try {
				// update variables
				this.time = time;

				queryHelper.setCurrentTime(time);

				// --- execution of the actual strategy logic
				onPreExecution();
				DetectionResult detectionResult = onAnalysis();
				onPostExecution(detectionResult);
			} finally {
				lastExecutionTime = System.currentTimeMillis();
				lastTime = time;

				isExecuting.set(false);
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("The detection strategy is running and cannot be started again before the previous run has been ended.");
			}
		}
	}

	/**
	 * Returns the minimum duration [ms] between two executions of this strategy. Default: 0
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
		if (log.isDebugEnabled()) {
			log.debug("onPreExecution");
		}
	}

	/**
	 * Will be executed after the {@link #onAnalysis()}.
	 *
	 * @param detectionResult
	 *            the result of the current analysis
	 */
	protected void onPostExecution(DetectionResult detectionResult) {
		if (log.isDebugEnabled()) {
			log.debug("onPostExecution");
		}

		if (log.isDebugEnabled()) {
			log.debug("Result of the anomaly detection: {}", detectionResult);
		}
	}

	/**
	 * Gets {@link #time}.
	 *
	 * @return {@link #time}
	 */
	protected long getTime() {
		return time;
	}

	/**
	 * Gets {@link #lastExecutionTime}.
	 *
	 * @return {@link #lastExecutionTime}
	 */
	public long getLastExecutionTime() {
		return lastExecutionTime;
	}

	/**
	 * Gets {@link #deltaTime}.
	 *
	 * @return {@link #deltaTime} or -1 if the strategy was never called before
	 */
	protected long getDeltaTime() {
		if (lastTime < 0) {
			return -1;
		}
		return time - lastTime;
	}

	/**
	 * Gets {@link #lastTime}.
	 *
	 * @return {@link #lastTime} or -1 if the strategy was never called before
	 */
	protected long getLastTime() {
		return lastTime;
	}

}
