/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.anomaly.utils.AnomalyUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(FixedThresholdStrategy.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DetectionResult onAnalysis() {
		QueryResult result;
		if (getLastTime() < 0) {
			// first call
			result = queryHelper.query("total_cpu_usage", "cpu_information");
		} else {
			result = queryHelper.query("total_cpu_usage", "cpu_information", getDeltaTime(), TimeUnit.MILLISECONDS);
		}

		boolean problemIsActive = timeSeriesDatabase.queryBoolean("SELECT LAST(problem) FROM anomaly_problems WHERE type = 'fixedThreshold'");

		DetectionResult detectionResult = null;

		List<List<Object>> dataList;
		try {
			dataList = result.getResults().get(0).getSeries().get(0).getValues();
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("No data found.");
			}

			return DetectionResult.make(Status.UNKNOWN);
		}

		for (List<Object> data : dataList) {
			double value = (double) data.get(1);
			Date date = AnomalyUtils.parseInfluxTimeString(data.get(0).toString());
			date.setTime(date.getTime() + 3600000); // fix timezone

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
