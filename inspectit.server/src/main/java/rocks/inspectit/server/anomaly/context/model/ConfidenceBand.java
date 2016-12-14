package rocks.inspectit.server.anomaly.context.model;

import org.apache.commons.lang.NotImplementedException;

/**
 * @author Marius Oehler
 *
 */
public class ConfidenceBand {

	private final double upperBound;

	private final double lowerBound;

	/**
	 * @param upperBound
	 * @param lowerBound
	 */
	public ConfidenceBand(double upperBound, double lowerBound) {
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
	}

	public boolean isInside(double value) {
		return (lowerBound < value) && (value <= upperBound);
	}

	public double distanceToBoundary(double value) {
		// TODO
		throw new NotImplementedException("TODO");
	}

	/**
	 * Gets {@link #upperBound}.
	 *
	 * @return {@link #upperBound}
	 */
	public double getUpperBound() {
		return this.upperBound;
	}

	/**
	 * Gets {@link #lowerBound}.
	 *
	 * @return {@link #lowerBound}
	 */
	public double getLowerBound() {
		return this.lowerBound;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Confidence-Band[lower=" + lowerBound + " upper=" + upperBound + "]";
	}
}
