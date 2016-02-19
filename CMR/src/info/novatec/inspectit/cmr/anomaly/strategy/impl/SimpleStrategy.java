/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.influxdb.InfluxDBService;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
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
		// requesting data
		double currentData = influx.querySingleDouble("select mean(total_cpu_usage) from cpu_information where time > now() - 5s group by time(5s) LIMIT 1");

		if (Double.isNaN(currentData)) {
			log.debug("Cannot query current data.");
			return DetectionResult.make(Status.UNKNOWN);
		} else {
			Builder dataBuilder = Point.measurement("anomaly_meta").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

			// requesting data for ewma
			double latestEwma = influx.querySingleDouble("select LAST(ewma) from anomaly_meta where time > now() - 30s");
			if (Double.isNaN(latestEwma)) {
				latestEwma = currentData;
			}

			// calculating ewma
			double newEwma = calculateEwma(0.1D, latestEwma, currentData);
			dataBuilder.field("ewma", newEwma);

			// calculating alerting tube
			double dynamicStdDev = influx.querySingleDouble("SELECT STDDEV(total_cpu_usage) FROM cpu_information WHERE time > now() - 600s");
			if (!Double.isNaN(dynamicStdDev)) {

				double latestStdDevEwma = influx.querySingleDouble("SELECT LAST(ewma_stddev) FROM anomaly_meta WHERE time > now() - 30s");
				if (Double.isNaN(latestStdDevEwma)) {
					latestStdDevEwma = dynamicStdDev;
				}

				// calculating stddev ewma
				double newStdDevEwma = calculateEwma(0.1D, latestStdDevEwma, dynamicStdDev);
				dataBuilder.field("ewma_stddev", newStdDevEwma);
			}

			influx.write(dataBuilder.build());

			return DetectionResult.make(Status.NORMAL);
		}
	}
}
