/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.anomaly.utils.AnomalyUtils;
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
	 * The decay factor used to calculate the ewma.
	 */
	private double ewmaDecayFactor;

	/**
	 * The system has to be "normal" for this time to reset its state to normal. [miliseconds]
	 */
	private long cooldownDuration;

	/**
	 * The time when the last problem occured.
	 */
	private long lastProblemTime = 0;

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
	 * {@inheritDoc}
	 */
	@Override
	protected void onPreExecution() {
		ewmaDecayFactor = 0.1D;
		cooldownDuration = 30000;
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
			long currentTime = System.currentTimeMillis();
			Builder dataBuilder = Point.measurement("anomaly_meta").time(currentTime, TimeUnit.MILLISECONDS);

			// requesting data for ewma
			double latestEwma = influx.querySingleDouble("select LAST(ewma) from anomaly_meta where time > now() - 30s");
			if (Double.isNaN(latestEwma)) {
				latestEwma = currentData;
			}

			// calculating ewma
			double newEwma = AnomalyUtils.calculateExponentialMovingAverage(ewmaDecayFactor, latestEwma, currentData);
			dataBuilder.field("ewma", newEwma);

			DetectionResult detectionResult = DetectionResult.make(Status.UNKNOWN);

			// calculating alerting tube
			double dynamicStdDev = influx.querySingleDouble("SELECT STDDEV(total_cpu_usage) FROM cpu_information WHERE time > now() - 600s");
			if (!Double.isNaN(dynamicStdDev)) {
				dataBuilder.field("stddev", dynamicStdDev);

				double latestStdDevEwma = influx.querySingleDouble("SELECT LAST(ewma_stddev) FROM anomaly_meta WHERE time > now() - 30s");
				if (Double.isNaN(latestStdDevEwma)) {
					latestStdDevEwma = dynamicStdDev;
				}

				// calculating stddev ewma
				double newStdDevEwma = AnomalyUtils.calculateExponentialMovingAverage(ewmaDecayFactor, latestStdDevEwma, dynamicStdDev);
				dataBuilder.field("ewma_stddev", newStdDevEwma);

				// ############## check for an anomaly

				boolean problemIsActive = influx.querySingleBoolean("SELECT LAST(problem) FROM anomaly_problems");

				if (Math.abs(newEwma - currentData) > newStdDevEwma) {
					lastProblemTime = currentTime;

					if (!problemIsActive) {
						// problem detected
						Builder problemBuilder = Point.measurement("anomaly_problems").time(currentTime, TimeUnit.MILLISECONDS);

						problemBuilder.tag("description", "out of stddev-tube");
						problemBuilder.field("problem", true);

						influx.write(problemBuilder.build());
					}

					detectionResult = DetectionResult.make(Status.CRITICAL);
				} else {
					if (problemIsActive && currentTime - lastProblemTime > cooldownDuration) {
						// problem is over
						Builder problemBuilder = Point.measurement("anomaly_problems").time(currentTime, TimeUnit.MILLISECONDS);

						problemBuilder.tag("description", "back to normal");
						problemBuilder.field("problem", false);

						influx.write(problemBuilder.build());
					}
					detectionResult = DetectionResult.make(Status.NORMAL);
				}

			}

			influx.write(dataBuilder.build());
			return detectionResult;
		}
	}
}
