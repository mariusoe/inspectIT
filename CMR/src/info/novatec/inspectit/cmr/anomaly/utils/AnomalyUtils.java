/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.utils;

/**
 * Utility class which provides useful methods for the anomaly detection.
 *
 * @author Marius Oehler
 *
 */
public final class AnomalyUtils {

	/**
	 * Hidden constructor.
	 */
	private AnomalyUtils() {
	}

	/**
	 * Calculates the an exponentially weighted moving average based on the given data.
	 *
	 * @param decayFactor
	 *            the decay factor of old data
	 * @param currentAverage
	 *            the current (old) exponential moving average
	 * @param newDataValue
	 *            the new data value which will influence the current average
	 * @return the new exponentially weighted moving average
	 */
	public static double calculateExponentialMovingAverage(double decayFactor, double currentAverage, double newDataValue) {
		return decayFactor * newDataValue + (1 - decayFactor) * currentAverage;
	}

}
