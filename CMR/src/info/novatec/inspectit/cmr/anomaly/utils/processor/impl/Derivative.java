/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.utils.processor.impl;

import info.novatec.inspectit.cmr.anomaly.utils.processor.IStatisticProcessor;

/**
 * @author Marius Oehler
 *
 */
public class Derivative implements IStatisticProcessor {

	/**
	 * The time of the last push.
	 */
	private long lastTime;

	/**
	 * The current derivation.
	 */
	private double currentDerivative = Double.NaN;

	/**
	 * The last value.
	 */
	private double lastValue = Double.NaN;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(long time, double value) {
		double deltaTime = (time - lastTime) / 1000D;

		if (Double.isNaN(lastValue)) {
			currentDerivative = 0D;
		} else {
			currentDerivative = (value - lastValue) / deltaTime;
		}

		lastValue = value;
		lastTime = time;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		return currentDerivative;
	}

}
