package rocks.inspectit.shared.cs.ci.anomaly.definition.baseline;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "historical-baseline")
public class HistoricalBaselineDefinition extends BaselineDefinition {

	private int seasonLength;

	private double smoothingFactor;

	private boolean smoothValue;

	private double valueSoothingFactor;

	private double trendSmoothingFactor;

	/**
	 * Gets {@link #smoothValue}.
	 *
	 * @return {@link #smoothValue}
	 */
	public boolean isSmoothValue() {
		return this.smoothValue;
	}

	/**
	 * Sets {@link #smoothValue}.
	 *
	 * @param smoothValue
	 *            New value for {@link #smoothValue}
	 */
	public void setSmoothValue(boolean smoothValue) {
		this.smoothValue = smoothValue;
	}

	/**
	 * Gets {@link #valueSoothingFactor}.
	 *
	 * @return {@link #valueSoothingFactor}
	 */
	public double getValueSmoothingFactor() {
		return this.valueSoothingFactor;
	}

	/**
	 * Sets {@link #valueSoothingFactor}.
	 *
	 * @param valueSoothingFactor
	 *            New value for {@link #valueSoothingFactor}
	 */
	public void setValueSoothingFactor(double valueSoothingFactor) {
		this.valueSoothingFactor = valueSoothingFactor;
	}

	/**
	 * Gets {@link #trendSmoothingFactor}.
	 *
	 * @return {@link #trendSmoothingFactor}
	 */
	public double getTrendSmoothingFactor() {
		return this.trendSmoothingFactor;
	}

	/**
	 * Sets {@link #trendSmoothingFactor}.
	 *
	 * @param trendSmoothingFactor
	 *            New value for {@link #trendSmoothingFactor}
	 */
	public void setTrendSmoothingFactor(double trendSmoothingFactor) {
		this.trendSmoothingFactor = trendSmoothingFactor;
	}

	/**
	 * Gets {@link #seasonLength}.
	 *
	 * @return {@link #seasonLength}
	 */
	public int getSeasonLength() {
		return this.seasonLength;
	}

	/**
	 * Sets {@link #seasonLength}.
	 *
	 * @param seasonLength
	 *            New value for {@link #seasonLength}
	 */
	public void setSeasonLength(int seasonLength) {
		this.seasonLength = seasonLength;
	}

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

}
