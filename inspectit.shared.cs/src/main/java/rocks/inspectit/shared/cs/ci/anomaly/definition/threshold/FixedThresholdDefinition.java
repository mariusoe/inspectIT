package rocks.inspectit.shared.cs.ci.anomaly.definition.threshold;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-fixed-threshold")
public class FixedThresholdDefinition extends ThresholdDefinition {

	private double upperCriticalThreshold = Double.NaN;

	private double upperWarningThreshold = Double.NaN;

	private double lowerWarningThreshold = Double.NaN;

	private double lowerCriticalThreshold = Double.NaN;

	/**
	 * Gets {@link #upperCriticalThreshold}.
	 *
	 * @return {@link #upperCriticalThreshold}
	 */
	public double getUpperCriticalThreshold() {
		return this.upperCriticalThreshold;
	}

	/**
	 * Sets {@link #upperCriticalThreshold}.
	 *
	 * @param upperCriticalThreshold
	 *            New value for {@link #upperCriticalThreshold}
	 */
	public void setUpperCriticalThreshold(double upperCriticalThreshold) {
		this.upperCriticalThreshold = upperCriticalThreshold;
	}

	/**
	 * Gets {@link #upperWarningThreshold}.
	 *
	 * @return {@link #upperWarningThreshold}
	 */
	public double getUpperWarningThreshold() {
		return this.upperWarningThreshold;
	}

	/**
	 * Sets {@link #upperWarningThreshold}.
	 *
	 * @param upperWarningThreshold
	 *            New value for {@link #upperWarningThreshold}
	 */
	public void setUpperWarningThreshold(double upperWarningThreshold) {
		this.upperWarningThreshold = upperWarningThreshold;
	}

	/**
	 * Gets {@link #lowerWarningThreshold}.
	 *
	 * @return {@link #lowerWarningThreshold}
	 */
	public double getLowerWarningThreshold() {
		return this.lowerWarningThreshold;
	}

	/**
	 * Sets {@link #lowerWarningThreshold}.
	 *
	 * @param lowerWarningThreshold
	 *            New value for {@link #lowerWarningThreshold}
	 */
	public void setLowerWarningThreshold(double lowerWarningThreshold) {
		this.lowerWarningThreshold = lowerWarningThreshold;
	}

	/**
	 * Gets {@link #lowerCriticalThreshold}.
	 *
	 * @return {@link #lowerCriticalThreshold}
	 */
	public double getLowerCriticalThreshold() {
		return this.lowerCriticalThreshold;
	}

	/**
	 * Sets {@link #lowerCriticalThreshold}.
	 *
	 * @param lowerCriticalThreshold
	 *            New value for {@link #lowerCriticalThreshold}
	 */
	public void setLowerCriticalThreshold(double lowerCriticalThreshold) {
		this.lowerCriticalThreshold = lowerCriticalThreshold;
	}
}
