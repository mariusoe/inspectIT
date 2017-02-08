package rocks.inspectit.shared.cs.anomaly.classification.context;

/**
 * @author Marius Oehler
 *
 */
public class TubeContext extends AbstractClassificationContext {

	private double upperCriticalLevel;

	private double upperWarningLevel;

	private double lowerCriticalLevel;

	private double lowerWarningLevel;

	/**
	 * Gets {@link #upperCriticalLevel}.
	 *
	 * @return {@link #upperCriticalLevel}
	 */
	public double getUpperCriticalLevel() {
		return this.upperCriticalLevel;
	}

	/**
	 * Sets {@link #upperCriticalLevel}.
	 *
	 * @param upperCriticalLevel
	 *            New value for {@link #upperCriticalLevel}
	 */
	public void setUpperCriticalLevel(double upperCriticalLevel) {
		this.upperCriticalLevel = upperCriticalLevel;
	}

	/**
	 * Gets {@link #upperWarningLevel}.
	 *
	 * @return {@link #upperWarningLevel}
	 */
	public double getUpperWarningLevel() {
		return this.upperWarningLevel;
	}

	/**
	 * Sets {@link #upperWarningLevel}.
	 *
	 * @param upperWarningLevel
	 *            New value for {@link #upperWarningLevel}
	 */
	public void setUpperWarningLevel(double upperWarningLevel) {
		this.upperWarningLevel = upperWarningLevel;
	}

	/**
	 * Gets {@link #lowerCriticalLevel}.
	 *
	 * @return {@link #lowerCriticalLevel}
	 */
	public double getLowerCriticalLevel() {
		return this.lowerCriticalLevel;
	}

	/**
	 * Sets {@link #lowerCriticalLevel}.
	 *
	 * @param lowerCriticalLevel
	 *            New value for {@link #lowerCriticalLevel}
	 */
	public void setLowerCriticalLevel(double lowerCriticalLevel) {
		this.lowerCriticalLevel = lowerCriticalLevel;
	}

	/**
	 * Gets {@link #lowerWarningLevel}.
	 *
	 * @return {@link #lowerWarningLevel}
	 */
	public double getLowerWarningLevel() {
		return this.lowerWarningLevel;
	}

	/**
	 * Sets {@link #lowerWarningLevel}.
	 *
	 * @param lowerWarningLevel
	 *            New value for {@link #lowerWarningLevel}
	 */
	public void setLowerWarningLevel(double lowerWarningLevel) {
		this.lowerWarningLevel = lowerWarningLevel;
	}

}
