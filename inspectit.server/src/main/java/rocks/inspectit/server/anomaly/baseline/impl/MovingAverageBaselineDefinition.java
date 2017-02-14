package rocks.inspectit.server.anomaly.baseline.impl;

import rocks.inspectit.server.anomaly.baseline.BaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
public class MovingAverageBaselineDefinition implements BaselineDefinition {

	/**
	 * The window size (number of elements) of this moving average.
	 */
	private int windowSize;

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
