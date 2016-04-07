/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.anomaly.utils.AnomalyUtils;

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
	 * The decay factor used to calculate the exponential moving average.
	 */
	private double timeConstant;

	/**
	 * The system has to be "normal" for this time to reset its state to normal. [milliseconds]
	 */
	private long cooldownDuration;

	/**
	 * The time when the last problem occurred.
	 */
	private long lastProblemTime = 0;

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
		timeConstant = 5D;
		cooldownDuration = 30000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DetectionResult onAnalysis() {
		// requesting data
		double currentData = queryHelper.queryDouble("MEAN(total_cpu_usage)", "cpu_information", 5L, TimeUnit.SECONDS);

		if (Double.isNaN(currentData)) {
			log.debug("Cannot query current data.");
			return DetectionResult.make(Status.UNKNOWN);
		} else {
			Builder dataBuilder = Point.measurement("anomaly_meta").time(getTime(), TimeUnit.MILLISECONDS);

			// check if problem is active
			boolean problemIsActive = timeSeriesDatabase.queryBoolean("SELECT LAST(problem) FROM anomaly_problems");

			// requesting data for ewma
			double latestEwma = timeSeriesDatabase.queryDouble("SELECT LAST(ewma) FROM anomaly_meta WHERE time < " + getTime() + "ms");
			if (Double.isNaN(latestEwma)) {
				latestEwma = currentData;
			}

			// calculating new ewma
			double newEwma = AnomalyUtils.calculateExponentialMovingAverage(timeConstant, getDeltaTime(), latestEwma, currentData);
			latestEwma = newEwma;
			dataBuilder.addField("ewma", newEwma);

			log.info(String.format("%f -> %f | %f", latestEwma, newEwma, currentData));

			// distance ewma - current data
			double distanceCurrentEwma = currentData - latestEwma;
			if (!problemIsActive) {
				dataBuilder.addField("distance", distanceCurrentEwma);
			}

			// distance stddev
			double distanceStddev = queryHelper.queryDouble("STDDEV(distance)", "anomaly_meta", 5L, TimeUnit.MINUTES);
			if (!Double.isNaN(distanceStddev)) {
				dataBuilder.addField("distanceStddev", distanceStddev);
			}

			// ############## check for an anomaly

			if (Math.abs(distanceCurrentEwma) > 3 * distanceStddev) {
				lastProblemTime = getTime();

				if (!problemIsActive) {
					Builder problemBuilder = Point.measurement("anomaly_problems").time(getTime(), TimeUnit.MILLISECONDS);

					problemBuilder.tag("description", "out of stddev-tube");
					problemBuilder.addField("problem", true);

					timeSeriesDatabase.insert(problemBuilder.build());
				}
			} else {
				if (problemIsActive && getTime() - lastProblemTime > cooldownDuration) {
					Builder problemBuilder = Point.measurement("anomaly_problems").time(getTime(), TimeUnit.MILLISECONDS);

					problemBuilder.tag("description", "back to normal");
					problemBuilder.addField("problem", false);

					timeSeriesDatabase.insert(problemBuilder.build());
				}
			}

			if (problemIsActive) {
				double distanceRatio = Math.abs((currentData - latestEwma) / (latestEwma));
				dataBuilder.addField("distanceRatio", distanceRatio);
			}

			if (!Double.isNaN(distanceStddev)) {
				double outDistance = Math.max(Math.abs(distanceCurrentEwma) - 3 * distanceStddev, 0);
				dataBuilder.addField("outDistance", outDistance);

				if (currentData == 0 || outDistance == 0) {
					dataBuilder.addField("outDistanceRation", 0);
				} else {
					double outDistanceRation = 1 / Math.max(currentData, 0) * Math.abs(outDistance);
					outDistanceRation = (outDistance - newEwma) / newEwma;
					if (outDistanceRation < 0) {
						System.out.println();
					}
					dataBuilder.addField("outDistanceRation", outDistanceRation);
				}
			}

			DetectionResult detectionResult = DetectionResult.make(Status.UNKNOWN);

			timeSeriesDatabase.insert(dataBuilder.build());
			return detectionResult;
		}
	}
}
