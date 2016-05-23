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
import rocks.inspectit.server.anomaly.utils.processor.impl.DoubleExponentialSmoothing;
import rocks.inspectit.server.anomaly.utils.processor.impl.ExponentialSmoothing;
import rocks.inspectit.server.anomaly.utils.processor.impl.HoltWintersSmoothing;
import rocks.inspectit.server.anomaly.utils.processor.impl.MovingAverage;

/**
 * @author Marius Oehler
 *
 */
public class StatisticsTestStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * A moving average.
	 */
	private final IStatisticProcessor movingAverage = new MovingAverage(60, TimeUnit.SECONDS);

	/**
	 * An exponential weighted moving average (exponential smoothing).
	 */
	private final IStatisticProcessor exponentialSmoothing = new ExponentialSmoothing(5D);

	/**
	 * An exponential weighted moving average (double exponential smoothing).
	 */
	private final IStatisticProcessor doubleExponentialSmoothing = new DoubleExponentialSmoothing(5D, 0.2D);

	/**
	 * An exponential weighted moving average (double exponential smoothing).
	 */
	private final IStatisticProcessor holtWintersSmoothing = new HoltWintersSmoothing(5D, 0.2D, 0.2D, 120L, 5L, TimeUnit.SECONDS);

	/**
	 * The derivative (derivation).
	 */
	private final IStatisticProcessor derivative = new Derivative();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DetectionResult onAnalysis() {
		((HoltWintersSmoothing) holtWintersSmoothing).setQueryHelper(queryHelper);

		double currentData = queryHelper.queryDouble("MEAN(\"duration\")", "invocation_sequences", 5L, TimeUnit.SECONDS);

		if (Double.isNaN(currentData)) {
			return DetectionResult.make(Status.UNKNOWN);
		}

		// builder
		Builder builder = Point.measurement("anomaly_meta").time(getTime(), TimeUnit.MILLISECONDS);

		// moving average
		movingAverage.push(getTime(), currentData);
		builder.addField("movingAverage", movingAverage.getValue());

		// exponential smoothing
		exponentialSmoothing.push(getTime(), currentData);
		builder.addField("exponentialSmoothing", exponentialSmoothing.getValue());

		// double exponential smoothing
		doubleExponentialSmoothing.push(getTime(), currentData);
		builder.addField("doubleExponentialSmoothing", doubleExponentialSmoothing.getValue());

		// holt-weinters smoothing
		holtWintersSmoothing.push(getTime(), currentData);
		builder.addField("holtWintersSmoothing", holtWintersSmoothing.getValue());

		// holt-weinters smoothing
		builder.addField("season", ((HoltWintersSmoothing) holtWintersSmoothing).getCurrentSeason());

		// derivative
		derivative.push(getTime(), currentData);
		builder.addField("derivative", derivative.getValue());

		timeSeriesDatabase.insert(builder.build());

		return DetectionResult.make(Status.NORMAL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStrategyName() {
		return "StatisticsTestStrategy";
	}

}
