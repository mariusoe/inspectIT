/**
 *
 */
package rocks.inspectit.server.anomaly.utils;

import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

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
	 *            the decay factor per second of old data
	 * @param currentAverage
	 *            the current (old) exponential moving average
	 * @param newDataValue
	 *            the new data value which will influence the current average
	 * @return the new exponentially weighted moving average
	 */
	public static double calculateExponentialMovingAverage(double decayFactor, double currentAverage, double newDataValue) {
		return decayFactor * newDataValue + (1 - decayFactor) * currentAverage;
	}

	/**
	 * Calculates the an exponentially weighted moving average based on the given data in a certain
	 * time span. Basically, the {@link #calculateExponentialMovingAverage(double, double, double)}
	 * method is used. The decay factor is calculated with the following equation: 1 - e^( -(1000 /
	 * timeDelta) / timeConstant )
	 *
	 * @param timeConstant
	 *            the time constant to use
	 * @param deltaTime
	 *            the time delta to use
	 * @param currentAverage
	 *            the current (old) exponential moving average
	 * @param newDataValue
	 *            the new data value which will influence the current average
	 * @return the new exponentially weighted moving average
	 */
	public static double calculateExponentialMovingAverage(double timeConstant, double deltaTime, double currentAverage, double newDataValue) {
		double decayFactor = calculateSmoothingFactor(deltaTime, timeConstant);
		return calculateExponentialMovingAverage(decayFactor, currentAverage, newDataValue);
	}

	/**
	 * Calculates the smoothing factor used by exponential smoothing.
	 *
	 * @param timeConstant
	 *            the time constant to use
	 * @param deltaTime
	 *            the time delta to use
	 * @return the calculated smoothing factor
	 */
	public static double calculateSmoothingFactor(double timeConstant, double deltaTime) {
		return 1 - Math.exp(-(1000 / deltaTime) / timeConstant);
	}

	/**
	 * Converting the time string of an InfluxDB {@link Point} into a {@link Date} object.
	 *
	 * @param dateString
	 *            the time string of InfluxDB
	 * @return the {@link Date} representation
	 */
	public static Date parseInfluxTimeString(String dateString) {
		try {
			// TODO: Influx can provide nano seconds: YYYY-MM-DDTHH:MM:SS.nnnnnnnnnZ
			if (dateString.length() > 20) {
				// date string containing milliseconds

				String[] split = dateString.split("\\.");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date date = dateFormat.parse(split[0]);

				long millis = Long.parseLong(split[1].substring(0, split[1].length() - 1));
				millis = (long) (millis * Math.pow(10, 4 - split[1].length()));
				date.setTime(date.getTime() + millis);

				return date;
			} else {
				// date string without milliseconds

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				return dateFormat.parse(dateString);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static double[] toDoubleArray(Collection<Double> collection) {
		double[] returnArray = new double[collection.size()];
		int index = 0;
		Iterator<Double> iterator = collection.iterator();
		while (iterator.hasNext()) {
			returnArray[index++] = iterator.next().doubleValue();
		}
		return returnArray;
	}
}
