package rocks.inspectit.shared.cs.ci.anomaly.definition.baseline;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "holt-winters-baseline")
public class HoltWintersBaselineDefinition extends BaselineDefinition {

	/**
	 * The trend smoothing factor.
	 */
	private double smoothingFactor;

	/**
	 * The trend smoothing factor.
	 */
	private double trendSmoothingFactor;

	/**
	 * The seasonal smoothing factor.
	 */
	private double seasonalSmoothingFactor;
	/**
	 * The season length (number of elements).
	 */
	private int seasonalLength;

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
	 * Gets {@link #seasonalSmoothingFactor}.
	 *
	 * @return {@link #seasonalSmoothingFactor}
	 */
	public double getSeasonalSmoothingFactor() {
		return this.seasonalSmoothingFactor;
	}

	/**
	 * Sets {@link #seasonalSmoothingFactor}.
	 *
	 * @param seasonalSmoothingFactor
	 *            New value for {@link #seasonalSmoothingFactor}
	 */
	public void setSeasonalSmoothingFactor(double seasonalSmoothingFactor) {
		this.seasonalSmoothingFactor = seasonalSmoothingFactor;
	}

	/**
	 * Gets {@link #seasonalLength}.
	 *
	 * @return {@link #seasonalLength}
	 */
	public int getSeasonalLength() {
		return this.seasonalLength;
	}

	/**
	 * Sets {@link #seasonalLength}.
	 *
	 * @param seasonalLength
	 *            New value for {@link #seasonalLength}
	 */
	public void setSeasonalLength(int seasonalLength) {
		this.seasonalLength = seasonalLength;
	}
}
