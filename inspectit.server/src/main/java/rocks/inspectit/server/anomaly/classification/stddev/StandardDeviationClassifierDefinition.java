package rocks.inspectit.server.anomaly.classification.stddev;

import rocks.inspectit.server.anomaly.classification.IClassifierDefinition;

/**
 * @author Marius Oehler
 *
 */
public class StandardDeviationClassifierDefinition implements IClassifierDefinition {

	/**
	 * The window size (number of elements) used to calculate the standard deviation.
	 */
	private int windowSize;

	private int sigmaAmount;

	/**
	 * Gets {@link #sigmaAmount}.
	 *
	 * @return {@link #sigmaAmount}
	 */
	public int getSigmaAmount() {
		return this.sigmaAmount;
	}

	/**
	 * Sets {@link #sigmaAmount}.
	 *
	 * @param sigmaAmount
	 *            New value for {@link #sigmaAmount}
	 */
	public void setSigmaAmount(int sigmaAmount) {
		this.sigmaAmount = sigmaAmount;
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
