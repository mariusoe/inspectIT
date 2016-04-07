/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.anomaly.utils.AnomalyUtils;
import info.novatec.inspectit.cmr.tsdb.DataPoint;
import info.novatec.inspectit.cmr.tsdb.TimeSeries;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

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
					Builder problemBuilder = Point.measurement("anomaly_problems").time(date.getTime(), TimeUnit.MILLISECONDS);

					problemBuilder.tag("type", "fixedThreshold").addField("problem", true);

					timeSeriesDatabase.insert(problemBuilder.build());
				}

				problemIsActive = true;
			} else {
				if (problemIsActive) {
					Builder problemBuilder = Point.measurement("anomaly_problems").time(date.getTime(), TimeUnit.MILLISECONDS);

					problemBuilder.tag("type", "fixedThreshold").addField("problem", false);

					timeSeriesDatabase.insert(problemBuilder.build());
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
