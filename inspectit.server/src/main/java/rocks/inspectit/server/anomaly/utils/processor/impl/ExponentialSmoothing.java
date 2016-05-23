/**
 *
 */
package rocks.inspectit.server.anomaly.utils.processor.impl;

import rocks.inspectit.server.anomaly.utils.AnomalyUtils;
import rocks.inspectit.server.anomaly.utils.processor.IStatisticProcessor;

/**
 * @author Marius Oehler
 *
 */
public class ExponentialSmoothing implements IStatisticProcessor {

	/**
	 * The time constant.
	 */
	private final double timeConstant;

	/**
	 * The current value.
	 */
	private double currentValue = Double.NaN;

	/**
	 * The time of the last push.
	 */
	private long lastTime;

	/**
	 * Constructor.
	 *
	 * @param timeConstant
	 *            the time constant used to calculate the smoothing factor
	 */
	public ExponentialSmoothing(double timeConstant) {
		this.timeConstant = timeConstant;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(long currentTime, double newData) {
		if (Double.isNaN(currentValue)) {
			currentValue = newData;

			lastTime = currentTime;
		} else {
			long deltaTime = currentTime - lastTime;
			currentValue = AnomalyUtils.calculateExponentialMovingAverage(timeConstant, deltaTime, currentValue, newData);

			lastTime = currentTime;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		return currentValue;
	}
}
