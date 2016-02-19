package info.novatec.inspectit.cmr.anomaly.strategy;

import info.novatec.inspectit.cmr.influxdb.InfluxDBService;

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
	 * Placeholder class for the Java keyword void.
	 *
	 */
	public static final class Void {
		/**
		 * Hidden constructor.
		 */
		private Void() {
		}
	}

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractAnomalyDetectionStrategy.class);

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	protected InfluxDBService influx;

	/**
	 * Constructor.
	 *
	 * @param influxDb
	 *            the influx db service
	 */
	public AbstractAnomalyDetectionStrategy(InfluxDBService influxDb) {
		super();
		influx = influxDb;
	}

	/**
	 * Starts the anomaly detection.
	 */
	public void execute() {
		onPreExecution();
		DetectionResult detectionResult = onAnalysis();
		onPostExecution(detectionResult);
	}

	/**
	 * Returns the name of this detection strategy.
	 *
	 * @return the name
	 */
	public abstract String getStrategyName();

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
}
