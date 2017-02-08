package rocks.inspectit.shared.cs.anomaly.classification.context;

/**
 * @author Marius Oehler
 *
 */
public class ThresholdContext extends AbstractClassificationContext {

	private double criticalLevel;

	private double warningLevel;

	private boolean isUpperThreshold;

	/**
	 * Gets {@link #criticalLevel}.
	 *
	 * @return {@link #criticalLevel}
	 */
	public double getCriticalLevel() {
		return this.criticalLevel;
	}

	/**
	 * Sets {@link #criticalLevel}.
	 *
	 * @param criticalLevel
	 *            New value for {@link #criticalLevel}
	 */
	public void setCriticalLevel(double criticalLevel) {
		this.criticalLevel = criticalLevel;
	}

	/**
	 * Gets {@link #warningLevel}.
	 *
	 * @return {@link #warningLevel}
	 */
	public double getWarningLevel() {
		return this.warningLevel;
	}

	/**
	 * Sets {@link #warningLevel}.
	 *
	 * @param warningLevel
	 *            New value for {@link #warningLevel}
	 */
	public void setWarningLevel(double warningLevel) {
		this.warningLevel = warningLevel;
	}

	/**
	 * Gets {@link #isUpperThreshold}.
	 *
	 * @return {@link #isUpperThreshold}
	 */
	public boolean isUpperThreshold() {
		return this.isUpperThreshold;
	}

	/**
	 * Sets {@link #isUpperThreshold}.
	 *
	 * @param isUpperThreshold
	 *            New value for {@link #isUpperThreshold}
	 */
	public void setUpperThreshold(boolean isUpperThreshold) {
		this.isUpperThreshold = isUpperThreshold;
	}

}
