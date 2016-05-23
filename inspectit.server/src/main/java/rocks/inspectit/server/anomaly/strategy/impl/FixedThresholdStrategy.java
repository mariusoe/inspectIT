/**
 *
 */
package rocks.inspectit.server.anomaly.strategy.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import rocks.inspectit.server.anomaly.strategy.DetectionResult;
import rocks.inspectit.server.anomaly.strategy.DetectionResult.Status;
import rocks.inspectit.server.anomaly.utils.AnomalyUtils;
import rocks.inspectit.server.tsdb.DataPoint;
import rocks.inspectit.server.tsdb.TimeSeries;

/**
 * Strategy to detect values above a certain threshold.
 *
 * @author Marius Oehler
 *
 */
public class FixedThresholdStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * The threshold in milliseconds.
	 */
	private static final double THRESHOLD = 2000;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DetectionResult onAnalysis() {
		TimeSeries result;
		if (getLastTime() < 0) {
			// first call
			result = queryHelper.query("total_cpu_usage", "cpu_information");
		} else {
			result = queryHelper.query("total_cpu_usage", "cpu_information", getDeltaTime(), TimeUnit.MILLISECONDS);
		}

		boolean problemIsActive = timeSeriesDatabase.queryBoolean("SELECT LAST(problem) FROM anomaly_problems WHERE type = 'fixedThreshold'");

		DetectionResult detectionResult = null;

		for (DataPoint data : result.getData()) {
			double value = (double) data.get(1);
			Date date = AnomalyUtils.parseInfluxTimeString(data.get(0).toString());

			if (THRESHOLD < value) {
				if (detectionResult == null) {
					detectionResult = DetectionResult.make(Status.CRITICAL);
				}

				if (!problemIsActive) {
					problemBegins(date.getTime(), "fixedThreshold");
				}

				problemIsActive = true;
			} else {
				if (problemIsActive) {
					problemEnds(date.getTime(), "fixedThreshold");
				}

				problemIsActive = false;
			}
		}

		if (detectionResult == null) {
			detectionResult = DetectionResult.make(Status.NORMAL);
		}

		return detectionResult;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStrategyName() {
		return "FixedThresholdStrategy";
	}

}
