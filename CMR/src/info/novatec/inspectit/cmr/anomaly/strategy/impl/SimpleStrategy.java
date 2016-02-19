/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.influxdb.InfluxDBService;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marius Oehler
 *
 */
public class SimpleStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(SimpleStrategy.class);

	/**
	 * @param influxDb
	 *            the InfluxDB service
	 */
	public SimpleStrategy(InfluxDBService influxDb) {
		super(influxDb);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStrategyName() {
		return "SimpleStrategy";
	}

	/**
	 * Calculates the new exponentially weighted moving average based on the given data.
	 *
	 * @param decayFactor
	 *            the decay factor of old data
	 * @param currentEwma
	 *            the current ewma value
	 * @param currentDataValue
	 *            the current data value
	 * @return the new exponentially weighted moving average
	 */
	private double calculateEwma(double decayFactor, double currentEwma, double currentDataValue) {
		return decayFactor * currentDataValue + (1 - decayFactor) * currentEwma;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DetectionResult onAnalysis() {

		double currentData = influx.querySingleDouble("select mean(total_cpu_usage) from cpu_information where time > now() - 5s group by time(5s) LIMIT 1");

		if (Double.isNaN(currentData)) {
			log.debug("Cannot query current data.");
			return DetectionResult.make(Status.UNKNOWN);
		} else {

			double latestEwma = influx.querySingleDouble("select LAST(ewma) from anomaly_meta where time > now() - 30s");
			if (Double.isNaN(latestEwma)) {
				latestEwma = currentData;
			}

			double newEwma = calculateEwma(0.1D, latestEwma, currentData);
			influx.write(Point.measurement("anomaly_meta").field("ewma", newEwma).build());

			return DetectionResult.make(Status.NORMAL);
		}
	}
}
