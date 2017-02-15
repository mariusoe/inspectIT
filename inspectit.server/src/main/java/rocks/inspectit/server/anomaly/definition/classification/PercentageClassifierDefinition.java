package rocks.inspectit.server.anomaly.definition.classification;

/**
 * @author Marius Oehler
 *
 */
public class PercentageClassifierDefinition extends ClassifierDefinition {

	private double percentageWarningLevel = Double.NaN;

	private double percentageCriticalLevel = Double.NaN;

	/**
	 * Gets {@link #percentageWarningLevel}.
	 *
	 * @return {@link #percentageWarningLevel}
	 */
	public double getPercentageWarningLevel() {
		return this.percentageWarningLevel;
	}

	/**
	 * Sets {@link #percentageWarningLevel}.
	 *
	 * @param percentageWarningLevel
	 *            New value for {@link #percentageWarningLevel}
	 */
	public void setPercentageWarningLevel(double percentageWarningLevel) {
		this.percentageWarningLevel = percentageWarningLevel;
	}

	/**
	 * Gets {@link #percentageCriticalLevel}.
	 *
	 * @return {@link #percentageCriticalLevel}
	 */
	public double getPercentageCriticalLevel() {
		return this.percentageCriticalLevel;
	}

	/**
	 * Sets {@link #percentageCriticalLevel}.
	 *
	 * @param percentageCriticalLevel
	 *            New value for {@link #percentageCriticalLevel}
	 */
	public void setPercentageCriticalLevel(double percentageCriticalLevel) {
		this.percentageCriticalLevel = percentageCriticalLevel;
	}

}
