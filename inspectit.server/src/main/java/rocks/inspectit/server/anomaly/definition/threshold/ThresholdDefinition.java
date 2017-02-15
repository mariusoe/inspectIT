package rocks.inspectit.server.anomaly.definition.threshold;

import rocks.inspectit.server.anomaly.definition.AbstractDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class ThresholdDefinition extends AbstractDefinition {

	/**
	 * The window size (number of elements) used to calculate the standard deviation.
	 */
	private int windowSize;

	private boolean exponentialSmoothed;

	private double smoothingFactor;

	/**
	 * Gets {@link #smoothingFactor}.
	 *
	 * @return {@link #smoothingFactor}
	 */
	public double getSmoothingFactor() {
		return this.smoothingFactor;
	}

	/**
	 * Sets {@link #smoothingFactor}.
	 *
	 * @param smoothingFactor
	 *            New value for {@link #smoothingFactor}
	 */
	public void setSmoothingFactor(double smoothingFactor) {
		this.smoothingFactor = smoothingFactor;
	}

	/**
	 * Gets {@link #exponentialSmoothed}.
	 *
	 * @return {@link #exponentialSmoothed}
	 */
	public boolean isExponentialSmoothed() {
		return this.exponentialSmoothed;
	}

	/**
	 * Sets {@link #exponentialSmoothed}.
	 *
	 * @param exponentialSmoothed
	 *            New value for {@link #exponentialSmoothed}
	 */
	public void setExponentialSmoothed(boolean exponentialSmoothed) {
		this.exponentialSmoothed = exponentialSmoothed;
	}

	/**
	 * Gets {@link #windowSize}.
	 *
	 * @return {@link #windowSize}
	 */
	public int getWindowSize() {
		return this.windowSize;
	}

	/**
	 * Sets {@link #windowSize}.
	 *
	 * @param windowSize
	 *            New value for {@link #windowSize}
	 */
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
}
