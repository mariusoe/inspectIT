/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

/**
 * @author Marius Oehler
 *
 */
public class ConfidenceBand {

	final double mean;

	final double upperConfidenceLevel;

	final double lowerConfidenceLevel;

	/**
	 * @param mean
	 * @param upperConfidenceLevel
	 * @param lowerConfidenceLevel
	 */
	public ConfidenceBand(double mean, double upperConfidenceLevel, double lowerConfidenceLevel) {
		this.mean = mean;
		this.upperConfidenceLevel = upperConfidenceLevel;
		this.lowerConfidenceLevel = lowerConfidenceLevel;
	}

	/**
	 * Gets {@link #mean}.
	 *
	 * @return {@link #mean}
	 */
	public double getMean() {
		return mean;
	}

	/**
	 * Gets {@link #upperConfidenceLevel}.
	 *
	 * @return {@link #upperConfidenceLevel}
	 */
	public double getUpperConfidenceLevel() {
		return upperConfidenceLevel;
	}

	/**
	 * Gets {@link #lowerConfidenceLevel}.
	 *
	 * @return {@link #lowerConfidenceLevel}
	 */
	public double getLowerConfidenceLevel() {
		return lowerConfidenceLevel;
	}

	public boolean isInside(double value) {
		return value <= upperConfidenceLevel && value >= lowerConfidenceLevel;
	}

	public double getWidth() {
		return upperConfidenceLevel - lowerConfidenceLevel;
	}

	public double distanceToBand(double value) {
		return Math.abs(value - mean) - getWidth() / 2;
	}
}
