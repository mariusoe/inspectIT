package rocks.inspectit.server.anomaly.definition.threshold;

/**
 * @author Marius Oehler
 *
 */
public class StandardDeviationThresholdDefinition extends ThresholdDefinition {

	private boolean excludeCriticalData;

	private boolean excludeWarningData;

	private int sigmaAmountCritical;

	private int sigmaAmountWarning;

	/**
	 * Gets {@link #excludeWarningData}.
	 *
	 * @return {@link #excludeWarningData}
	 */
	public boolean isExcludeWarningData() {
		return this.excludeWarningData;
	}

	/**
	 * Sets {@link #excludeWarningData}.
	 *
	 * @param excludeWarningData
	 *            New value for {@link #excludeWarningData}
	 */
	public void setExcludeWarningData(boolean excludeWarningData) {
		this.excludeWarningData = excludeWarningData;
	}

	/**
	 * Gets {@link #excludeCriticalData}.
	 *
	 * @return {@link #excludeCriticalData}
	 */
	public boolean isExcludeCriticalData() {
		return this.excludeCriticalData;
	}

	/**
	 * Sets {@link #excludeCriticalData}.
	 *
	 * @param excludeCriticalData
	 *            New value for {@link #excludeCriticalData}
	 */
	public void setExcludeCriticalData(boolean excludeCriticalData) {
		this.excludeCriticalData = excludeCriticalData;
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
}
