/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult;
import info.novatec.inspectit.cmr.anomaly.strategy.DetectionResult.Status;
import info.novatec.inspectit.cmr.anomaly.utils.processor.IStatisticProcessor;
import info.novatec.inspectit.cmr.anomaly.utils.processor.impl.Derivative;
import info.novatec.inspectit.cmr.anomaly.utils.processor.impl.ExponentialSmoothing;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/**
 * @author Marius Oehler
 *
 */
public class DerivationScoreStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * Smoothed raw data.
	 */
	private final IStatisticProcessor dataSmoothed = new ExponentialSmoothing(10D);

	/**
	 * The derivation of the smoothed data.
	 */
	private final IStatisticProcessor dataDerivation = new Derivative();

	/**
	 * Smoothed derivation.
	 */
	private final IStatisticProcessor derivativeSmoothed = new ExponentialSmoothing(15D);

	/**
	 * Smoothed squared derivation.
	 */
	private final IStatisticProcessor derivativeSquaredSmoothed = new ExponentialSmoothing(30D);

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

			if (dataDerivation.getValue() > 3 * derivativeSmoothedStddev) {
				// SCORING
				double value = Math.abs(dataDerivation.getValue());
				double threshold = 3 * derivativeSmoothedStddev;

				// normalize
				value = value / threshold;
				threshold = 1D;

				double breakoutDistance = Math.max(0, value - threshold);

				// score
				double score = Math.min(100, breakoutDistance * breakoutDistance);
				builder.addField("anomalyScore", score);

				if (score >= 0.66D) {
					problemBegins(getTime(), "problem");
				} else if (score >= 0.33D) {
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
		return "StrategyTwo";
	}

}
