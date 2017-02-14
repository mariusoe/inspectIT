package rocks.inspectit.server.anomaly.definition.baseline;

/**
 * @author Marius Oehler
 *
 */
public class ExponentialMovingAverageBaselineDefinition extends BaselineDefinition {

	private double smoothingFactor;

	private double trendSmoothingFactor;

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
