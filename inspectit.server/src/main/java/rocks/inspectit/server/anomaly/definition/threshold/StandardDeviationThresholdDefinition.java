package rocks.inspectit.server.anomaly.definition.threshold;

/**
 * @author Marius Oehler
 *
 */
public class StandardDeviationThresholdDefinition extends ThresholdDefinition {

	/**
	 * The window size (number of elements) used to calculate the standard deviation.
	 */
	private int windowSize;

	private int sigmaAmountCritical;

	private int sigmaAmountWarning;

	private boolean useResiduals;

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
	 * Gets {@link #useResiduals}.
	 *
	 * @return {@link #useResiduals}
	 */
	public boolean isUseResiduals() {
		return this.useResiduals;
	}

	/**
	 * Sets {@link #useResiduals}.
	 *
	 * @param useResiduals
	 *            New value for {@link #useResiduals}
	 */
	public void setUseResiduals(boolean useResiduals) {
		this.useResiduals = useResiduals;
	}

	/**
	 * Gets {@link #sigmaAmountCritical}.
	 *
	 * @return {@link #sigmaAmountCritical}
	 */
	public int getSigmaAmountCritical() {
		return this.sigmaAmountCritical;
	}

	/**
	 * Sets {@link #sigmaAmountCritical}.
	 *
	 * @param sigmaAmountCritical
	 *            New value for {@link #sigmaAmountCritical}
	 */
	public void setSigmaAmountCritical(int sigmaAmountCritical) {
		this.sigmaAmountCritical = sigmaAmountCritical;
	}

	/**
	 * Gets {@link #sigmaAmountWarning}.
	 *
	 * @return {@link #sigmaAmountWarning}
	 */
	public int getSigmaAmountWarning() {
		return this.sigmaAmountWarning;
	}

	/**
	 * Sets {@link #sigmaAmountWarning}.
	 *
	 * @param sigmaAmountWarning
	 *            New value for {@link #sigmaAmountWarning}
	 */
	public void setSigmaAmountWarning(int sigmaAmountWarning) {
		this.sigmaAmountWarning = sigmaAmountWarning;
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
