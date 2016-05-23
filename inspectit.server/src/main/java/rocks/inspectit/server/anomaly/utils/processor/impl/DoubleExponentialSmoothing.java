/**
 *
 */
package rocks.inspectit.server.anomaly.utils.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.utils.AnomalyUtils;
import rocks.inspectit.server.anomaly.utils.processor.IStatisticProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DoubleExponentialSmoothing implements IStatisticProcessor {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(DoubleExponentialSmoothing.class);

	/**
	 * The time constant.
	 */
	private final double timeConstant;

	/**
	 * The trend smoothing factor.
	 */
	private final double trendSmoothingFactor;

	/**
	 * The current value.
	 */
	private double currentValue;

	/**
	 * The current trend value.
	 */
	private double currentTrend;

	/**
	 * The time of the last push.
	 */
	private long lastTime;

	/**
	 * The count of pushes.
	 */
	private long pushCount = 0;

	/**
	 * Constructor.
	 *
	 * @param timeConstant
	 *            the time constant used to calculate the smoothing factor
	 * @param trendSmoothingFactor
	 *            the trend smoothing factor
	 */
	public DoubleExponentialSmoothing(double timeConstant, double trendSmoothingFactor) {
		this.timeConstant = timeConstant;
		this.trendSmoothingFactor = trendSmoothingFactor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(long currentTime, double value) {
		if (pushCount <= 0L) {
			currentValue = value;
			lastTime = currentTime;
		} else if (pushCount == 1L) {
			currentTrend = value - currentValue;
			currentValue = value;
			lastTime = currentTime;
		} else {
			long deltaTime = currentTime - lastTime;
			double smoothingFactor = AnomalyUtils.calculateSmoothingFactor(timeConstant, deltaTime);

			double nextValue = smoothingFactor * value + (1 - smoothingFactor) * (currentValue + currentTrend);
			currentTrend = trendSmoothingFactor * (nextValue - currentValue) + (1 - trendSmoothingFactor) * currentTrend;
			currentValue = nextValue;

			lastTime = currentTime;
		}

		// log.info("st=" + currentValue + "; bt=" + currentTrend);

		pushCount++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		return currentValue;
	}

}
