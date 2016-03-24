/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.influxdb.dto.Point;

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

	public static double calculateExponentialMovingAverage(double timeConstant, double deltaTime, double currentAverage, double newDataValue) {
		double decayFactor = 1 - Math.exp(-(1000 / deltaTime) / timeConstant);
		return calculateExponentialMovingAverage(decayFactor, currentAverage, newDataValue);
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
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
				Date date = dateFormat.parse(split[0]);

				long millis = Long.parseLong(split[1].substring(0, split[1].length() - 1));
				millis = (long) (millis * Math.pow(10, 4 - split[1].length()));
				date.setTime(date.getTime() + millis);

				return date;
			} else {
				// date string without milliseconds

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
				return dateFormat.parse(dateString);
			}
		} catch (Exception e) {
			return null;
		}
	}

}
