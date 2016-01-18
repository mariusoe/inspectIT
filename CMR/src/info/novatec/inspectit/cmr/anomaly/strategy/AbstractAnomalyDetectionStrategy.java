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
		onAnalysis();
		onPostExecution();
	}

	/**
	 * Will be executed before the {@link #onAnalysis()}.
	 */
	protected void onPreExecution() {
		log.info("onPreExecution");
	}

	/**
	 * The actual logic to analyze the data.
	 */
	protected abstract void onAnalysis();

	/**
	 * Will be executed after the {@link #onAnalysis()}.
	 */
	protected void onPostExecution() {
		log.info("onPostExecution");
	}
}
