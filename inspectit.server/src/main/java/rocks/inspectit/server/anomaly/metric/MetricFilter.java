package rocks.inspectit.server.anomaly.metric;

/**
 * @author Marius Oehler
 *
 */
public class MetricFilter {

	private double upperLimit = Double.NaN;

	private double lowerLimit = Double.NaN;

	public MetricFilter() {
	}

	/**
	 * @param upperLimit
	 * @param lowerLimit
	 */
	public MetricFilter(double upperLimit, double lowerLimit) {
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
	}

	/**
	 * Gets {@link #upperLimit}.
	 *
	 * @return {@link #upperLimit}
	 */
	public double getUpperLimit() {
		return this.upperLimit;
	}

	/**
	 * Sets {@link #upperLimit}.
	 *
	 * @param upperLimit
	 *            New value for {@link #upperLimit}
	 */
	public void setUpperLimit(double upperLimit) {
		this.upperLimit = upperLimit;
	}

	/**
	 * Gets {@link #lowerLimit}.
	 *
	 * @return {@link #lowerLimit}
	 */
	public double getLowerLimit() {
		return this.lowerLimit;
	}

	/**
	 * Sets {@link #lowerLimit}.
	 *
	 * @param lowerLimit
	 *            New value for {@link #lowerLimit}
	 */
	public void setLowerLimit(double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}
}
