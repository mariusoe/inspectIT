/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

/**
 * @author Marius Oehler
 *
 */
public class StreamStatistics {

	private ConfidenceBand confidenceBand;

	private double standardDeviation;

	/**
	 * Gets {@link #confidenceBand}.
	 *
	 * @return {@link #confidenceBand}
	 */
	public ConfidenceBand getConfidenceBand() {
		return confidenceBand;
	}

	/**
	 * Sets {@link #confidenceBand}.
	 *
	 * @param confidenceBand
	 *            New value for {@link #confidenceBand}
	 */
	public void setConfidenceBand(ConfidenceBand confidenceBand) {
		this.confidenceBand = confidenceBand;
	}

	/**
	 * Gets {@link #standardDeviation}.
	 *
	 * @return {@link #standardDeviation}
	 */
	public double getStandardDeviation() {
		return standardDeviation;
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

}
