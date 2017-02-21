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
