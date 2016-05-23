/**
 *
 */
package rocks.inspectit.server.anomaly.strategy.impl;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import rocks.inspectit.server.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import rocks.inspectit.server.anomaly.strategy.DetectionResult;
import rocks.inspectit.server.anomaly.strategy.DetectionResult.Status;
import rocks.inspectit.server.anomaly.utils.processor.IStatisticProcessor;
import rocks.inspectit.server.anomaly.utils.processor.impl.ExponentialSmoothing;

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
		double currentData = queryHelper.queryDouble("MEAN(\"duration\")", "invocation_sequences", 5L, TimeUnit.SECONDS);
		double maxData = queryHelper.queryDouble("MAX(\"duration\")", "invocation_sequences", 5L, TimeUnit.SECONDS);

		if (Double.isNaN(currentData)) {
			return DetectionResult.make(Status.UNKNOWN);
		}

		// builder
		Builder builder = Point.measurement("anomaly_meta").time(getTime(), TimeUnit.MILLISECONDS);

		if (!first) {
			double stddev = queryHelper.queryDouble("STDDEV(\"duration\")", "invocation_sequences", 5L, TimeUnit.MINUTES);
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
