/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.anomaly.utils.processor.IStatisticProcessor;
import info.novatec.inspectit.cmr.anomaly.utils.processor.impl.ExponentialSmoothing;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/**
 * @author Marius Oehler
 *
 */
public class ThreeSigmaBreakoutScoreStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * Smoothed raw data.
	 */
	private final IStatisticProcessor dataSmoothed = new ExponentialSmoothing(2D);

	/**
	 * Specifies whether the analysis is in the first run.
	 */
	private boolean first = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DetectionResult onAnalysis() {
		double currentData = queryHelper.queryDouble("MEAN(\"total_cpu_usage\")", "cpu_information", 5L, TimeUnit.SECONDS);
		double maxData = queryHelper.queryDouble("MAX(\"total_cpu_usage\")", "cpu_information", 5L, TimeUnit.SECONDS);

		if (Double.isNaN(currentData)) {
			return DetectionResult.make(Status.UNKNOWN);
		}

		// builder
		Builder builder = Point.measurement("anomaly_meta").time(getTime(), TimeUnit.MILLISECONDS);

		if (!first) {
			double stddev = queryHelper.queryDouble("STDDEV(\"total_cpu_usage\")", "cpu_information", 5L, TimeUnit.MINUTES);
			double threshold = 3 * stddev + dataSmoothed.getValue();

			builder.addField("stddev", stddev);

			if (maxData > threshold) {
				// SCORING
				double value = Math.abs(maxData);

				double relativeChange = (value - threshold) / threshold;

				// score (capped)
				double score = Math.min(100, relativeChange * relativeChange);
				builder.addField("anomalyScore", score);

				if (score >= 0.6D) {
					problemBegins(getTime(), "problem");
				} else if (score >= 0.3D) {
					insertWarning(getTime(), "warning");
				}
			}
		}

		// smoothing
		dataSmoothed.push(getTime(), currentData);
		builder.addField("dataSmoothed", dataSmoothed.getValue());

		// finalize
		timeSeriesDatabase.insert(builder.build());
		first = false;

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStrategyName() {
		return "StrategyTwo";
	}

}
