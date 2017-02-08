package rocks.inspectit.shared.cs.ci.anomaly.definition.classification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-percentage-classifier")
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
