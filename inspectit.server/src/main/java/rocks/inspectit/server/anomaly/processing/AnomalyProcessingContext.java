package rocks.inspectit.server.anomaly.processing;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyProcessingContext {

	private double mean;

	private DescriptiveStatistics stats = new DescriptiveStatistics();

	private double standardDeviation;

	/**
	 * Gets {@link #standardDeviation}.
	 *
	 * @return {@link #standardDeviation}
	 */
	public double getStandardDeviation() {
		return this.standardDeviation;
	}

	/**
	 * Sets {@link #standardDeviation}.
	 *
	 * @param standardDeviation
	 *            New value for {@link #standardDeviation}
	 */
	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	/**
	 * Gets {@link #mean}.
	 *
	 * @return {@link #mean}
	 */
	public double getMean() {
		return this.mean;
	}

	/**
	 * Sets {@link #mean}.
	 *
	 * @param mean
	 *            New value for {@link #mean}
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}

	/**
	 * Gets {@link #stats}.
	 *
	 * @return {@link #stats}
	 */
	public DescriptiveStatistics getStats() {
		return this.stats;
	}

	/**
	 * Sets {@link #stats}.
	 *
	 * @param stats
	 *            New value for {@link #stats}
	 */
	public void setStats(DescriptiveStatistics stats) {
		this.stats = stats;
	}

}
