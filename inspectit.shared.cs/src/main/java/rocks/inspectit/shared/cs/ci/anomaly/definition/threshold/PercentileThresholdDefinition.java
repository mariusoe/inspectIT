package rocks.inspectit.shared.cs.ci.anomaly.definition.threshold;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-percentile-threshold")
public class PercentileThresholdDefinition extends ThresholdDefinition {

	private double upperCriticalPercentile;

	private double upperWarningPercentile;

	private double lowerWarningPercentile;

	private double lowerCriticalPercentile;

	/**
	 * Gets {@link #upperCriticalPercentile}.
	 *
	 * @return {@link #upperCriticalPercentile}
	 */
	public double getUpperCriticalPercentile() {
		return this.upperCriticalPercentile;
	}

	/**
	 * Sets {@link #upperCriticalPercentile}.
	 *
	 * @param upperCriticalPercentile
	 *            New value for {@link #upperCriticalPercentile}
	 */
	public void setUpperCriticalPercentile(double upperCriticalPercentile) {
		this.upperCriticalPercentile = upperCriticalPercentile;
	}

	/**
	 * Gets {@link #upperWarningPercentile}.
	 *
	 * @return {@link #upperWarningPercentile}
	 */
	public double getUpperWarningPercentile() {
		return this.upperWarningPercentile;
	}

	/**
	 * Sets {@link #upperWarningPercentile}.
	 *
	 * @param upperWarningPercentile
	 *            New value for {@link #upperWarningPercentile}
	 */
	public void setUpperWarningPercentile(double upperWarningPercentile) {
		this.upperWarningPercentile = upperWarningPercentile;
	}

	/**
	 * Gets {@link #lowerWarningPercentile}.
	 *
	 * @return {@link #lowerWarningPercentile}
	 */
	public double getLowerWarningPercentile() {
		return this.lowerWarningPercentile;
	}

	/**
	 * Sets {@link #lowerWarningPercentile}.
	 *
	 * @param lowerWarningPercentile
	 *            New value for {@link #lowerWarningPercentile}
	 */
	public void setLowerWarningPercentile(double lowerWarningPercentile) {
		this.lowerWarningPercentile = lowerWarningPercentile;
	}

	/**
	 * Gets {@link #lowerCriticalPercentile}.
	 *
	 * @return {@link #lowerCriticalPercentile}
	 */
	public double getLowerCriticalPercentile() {
		return this.lowerCriticalPercentile;
	}

	/**
	 * Sets {@link #lowerCriticalPercentile}.
	 *
	 * @param lowerCriticalPercentile
	 *            New value for {@link #lowerCriticalPercentile}
	 */
	public void setLowerCriticalPercentile(double lowerCriticalPercentile) {
		this.lowerCriticalPercentile = lowerCriticalPercentile;
	}

}
