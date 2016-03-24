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

	// private String since(long since) {
	// return "time > " + getTime() + "ms - " + since + "ms AND time < " + getTime() + "ms";
	// }

	private String timeFilter(long windowDuration, TimeUnit timeUnit) {
		long startTime = getTime() - timeUnit.toMillis(windowDuration);
		return "time > " + startTime + "ms AND time < " + getTime() + "ms";
	}

	// double latestEwma = Double.NaN;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DetectionResult onAnalysis() {
		// requesting data
		double currentData = timeSeriesDatabase.queryDouble("SELECT MEAN(total_cpu_usage) FROM cpu_information WHERE " + timeFilter(5L, TimeUnit.SECONDS) + " LIMIT 1");

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

			System.out.println(String.format("%f -> %f | %f", latestEwma, newEwma, currentData));

			// distance ewma - current data
			double distanceCurrentEwma = currentData - latestEwma;
			if (!problemIsActive) {
				dataBuilder.addField("distance", distanceCurrentEwma);
			}

			// distance stddev
			double distanceStddev = timeSeriesDatabase.queryDouble("SELECT STDDEV(distance) FROM anomaly_meta WHERE " + timeFilter(5L, TimeUnit.MINUTES));
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
			}

			DetectionResult detectionResult = DetectionResult.make(Status.UNKNOWN);

			// calculating alerting tube
			// double stddev = timeSeriesDatabase.queryDouble("SELECT STDDEV(distance) FROM
			// anomaly_meta WHERE " + since(600000));
			// if (!Double.isNaN(stddev)) {
			// dataBuilder.field("stddev", stddev);

			// double latestStdDevEwma = timeSeriesDatabase.queryDouble("SELECT
			// LAST(ewma_stddev) FROM anomaly_meta WHERE time > now() - 30s");
			// if (Double.isNaN(latestStdDevEwma)) {
			// latestStdDevEwma = dynamicStdDev;
			// }
			//
			// // calculating stddev ewma
			// double newStdDevEwma =
			// AnomalyUtils.calculateExponentialMovingAverage(ewmaDecayFactor, latestStdDevEwma,
			// dynamicStdDev);
			// dataBuilder.field("ewma_stddev", newStdDevEwma);

			// ############## check for an anomaly

			// boolean problemIsActive = timeSeriesDatabase.queryBoolean("SELECT LAST(problem)
			// FROM anomaly_problems");
			//
			// if (Math.abs(newEwma - currentData) > newStdDevEwma) {
			// lastProblemTime = getTime();
			//
			// if (!problemIsActive) {
			// // problem detected
			// Builder problemBuilder = Point.measurement("anomaly_problems").time(getTime(),
			// TimeUnit.MILLISECONDS);
			//
			// problemBuilder.tag("description", "out of stddev-tube");
			// problemBuilder.field("problem", true);
			//
			// timeSeriesDatabase.insert(problemBuilder.build());
			// }
			//
			// detectionResult = DetectionResult.make(Status.CRITICAL);
			// } else {
			// if (problemIsActive && getTime() - lastProblemTime > cooldownDuration) {
			// // problem is over
			// Builder problemBuilder = Point.measurement("anomaly_problems").time(getTime(),
			// TimeUnit.MILLISECONDS);
			//
			// problemBuilder.tag("description", "back to normal");
			// problemBuilder.field("problem", false);
			//
			// timeSeriesDatabase.insert(problemBuilder.build());
			// }
			// detectionResult = DetectionResult.make(Status.NORMAL);
			// }

			// }

			timeSeriesDatabase.insert(dataBuilder.build());
			return detectionResult;

			/****
			 * ###########################################################################
			 * ###########################################################################
			 * ###########################################################################
			 */

			/*
			 * double value = Math.max(0, dataPoint.getValue()); if (Double.isNaN(latestEwma)) {
			 * latestEwma = value; } latestEwma =
			 * AnomalyUtils.calculateExponentialMovingAverage(ewmaDecayFactor, latestEwma, value);
			 * if (!problemIsActive) { cacheEwma.insert(latestEwma);
			 * cacheData.insert(dataPoint.getValue()); } StatisticUtils stats = new
			 * StatisticUtils(); for (int i = 0; i < cacheData.getDataCache().length; i++) { if
			 * (!Double.isNaN(cacheData.getDataCache()[i])) { double delta =
			 * cacheData.getDataCache()[i] - cacheEwma.getDataCache()[i]; stats.insert(delta); } }
			 * if (Double.isNaN(latestStddev)) { latestStddev = stats.getStandardDeviation(); }
			 * latestStddev = AnomalyUtils.calculateExponentialMovingAverage(0.1D, latestStddev,
			 * stats.getStandardDeviation()); // latestStddev = stats.getStandardDeviation();
			 * Builder builder = Point.measurement("anomaly_meta").time(dataPoint.getTime(),
			 * TimeUnit.MILLISECONDS); builder.field("ewma", latestEwma); builder.field("stddev",
			 * latestStddev); // builder.field("stddev_tube", latestEwma + latestStddev); Builder
			 * builderProblem = Point.measurement("anomaly_problems").time(dataPoint.getTime(),
			 * TimeUnit.MILLISECONDS); if (Math.abs(latestEwma - value) > 3 * latestStddev) {
			 * lastProblemTime = dataPoint.getTime(); if (!problemIsActive) { problemIsActive =
			 * true; builderProblem.tag("description", "out of stddev-tube");
			 * builderProblem.field("problem", true); influx.write(builderProblem.build()); } } else
			 * { if (problemIsActive && dataPoint.getTime() - lastProblemTime > cooldownDuration) {
			 * problemIsActive = false; builderProblem.tag("description", "back to normal");
			 * builderProblem.field("problem", false); influx.write(builderProblem.build()); } } if
			 * (problemIsActive) { // double relativeDifferenceRatio = Math.abs((latestEwma - value)
			 * / (latestEwma + // value)); double relativeDifferenceRatio = Math.abs((value -
			 * latestEwma) / (latestEwma)); builder.field("rdRatio", relativeDifferenceRatio); }
			 * else { builder.field("rdRatio", 0); } influx.write(builder.build());
			 */
		}
	}
}
