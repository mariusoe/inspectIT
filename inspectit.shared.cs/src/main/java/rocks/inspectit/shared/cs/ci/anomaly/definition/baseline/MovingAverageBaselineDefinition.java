package rocks.inspectit.shared.cs.ci.anomaly.definition.baseline;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-moving-average-baseline")
public class MovingAverageBaselineDefinition extends BaselineDefinition {

	/**
	 * The window size (number of elements) of this moving average.
	 */
	private int windowSize;

	/**
	 * Gets {@link #windowSize}.
	 *
	 * @return {@link #windowSize}
	 */
	public int getWindowSize() {
		return this.windowSize;
	}

	/**
	 * Sets {@link #windowSize}.
	 *
	 * @param windowSize
	 *            New value for {@link #windowSize}
	 */
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

}
