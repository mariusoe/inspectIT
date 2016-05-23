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
import rocks.inspectit.server.anomaly.utils.processor.impl.Derivative;
import rocks.inspectit.server.anomaly.utils.processor.impl.ExponentialSmoothing;

/**
 * @author Marius Oehler
 *
 */
public class DerivationScoreStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * Smoothed raw data.
	 */
	private final IStatisticProcessor dataSmoothed = new ExponentialSmoothing(1D);

	/**
	 * The derivation of the smoothed data.
	 */
	private final IStatisticProcessor dataDerivation = new Derivative();

	/**
	 * Smoothed derivation.
	 */
	private final IStatisticProcessor derivativeSmoothed = new ExponentialSmoothing(10D);

	/**
	 * Smoothed squared derivation.
	 */
	private final IStatisticProcessor derivativeSquaredSmoothed = new ExponentialSmoothing(10D);

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

		if (Double.isNaN(currentData)) {
			return DetectionResult.make(Status.UNKNOWN);
		}

		// builder
		Builder builder = Point.measurement("anomaly_meta").time(getTime(), TimeUnit.MILLISECONDS);

		// smoothing
		dataSmoothed.push(getTime(), currentData);
		builder.addField("dataSmoothed", dataSmoothed.getValue());

		// derivation
		dataDerivation.push(getTime(), dataSmoothed.getValue());
		builder.addField("dataDerivation", dataDerivation.getValue());

		if (!first) {
			double derivativeSmoothedStddev = Math.sqrt(derivativeSquaredSmoothed.getValue() - derivativeSmoothed.getValue() * derivativeSmoothed.getValue());

			builder.addField("derivativeSmoothed", derivativeSmoothed.getValue());
			builder.addField("derivativeSmoothedStddev", derivativeSmoothedStddev);

			double threshold = 3 * derivativeSmoothedStddev;

			if (dataDerivation.getValue() > threshold) {
				// SCORING
				double value = Math.abs(dataDerivation.getValue());

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

		// smoothed derivative
		derivativeSmoothed.push(getTime(), dataDerivation.getValue());
		derivativeSquaredSmoothed.push(getTime(), dataDerivation.getValue() * dataDerivation.getValue());

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
		return "DerivationScoreStrategy";
	}

}
