package rocks.inspectit.server.anomaly.definition.threshold;

/**
 * @author Marius Oehler
 *
 */
public class PercentageDerivationThresholdDefinition extends ThresholdDefinition {

	private double percentageDerivationCritical;

	private double percentageDerivationWarning;

	private boolean excludeCriticalData;

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
	 * Gets {@link #percentageDerivationCritical}.
	 *
	 * @return {@link #percentageDerivationCritical}
	 */
	public double getPercentageDerivationCritical() {
		return this.percentageDerivationCritical;
	}

	/**
	 * Sets {@link #percentageDerivationCritical}.
	 *
	 * @param percentageDerivationCritical
	 *            New value for {@link #percentageDerivationCritical}
	 */
	public void setPercentageDerivationCritical(double percentageDerivationCritical) {
		this.percentageDerivationCritical = percentageDerivationCritical;
	}

	/**
	 * Gets {@link #percentageDerivationWarning}.
	 *
	 * @return {@link #percentageDerivationWarning}
	 */
	public double getPercentageDerivationWarning() {
		return this.percentageDerivationWarning;
	}

	/**
	 * Sets {@link #percentageDerivationWarning}.
	 *
	 * @param percentageDerivationWarning
	 *            New value for {@link #percentageDerivationWarning}
	 */
	public void setPercentageDerivationWarning(double percentageDerivationWarning) {
		this.percentageDerivationWarning = percentageDerivationWarning;
	}

}
