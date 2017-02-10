package rocks.inspectit.server.anomaly.processing;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyProcessingContext {

	private double mean;

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

}
