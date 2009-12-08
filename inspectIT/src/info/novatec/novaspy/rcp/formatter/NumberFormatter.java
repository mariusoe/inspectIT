package info.novatec.novaspy.rcp.formatter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is for formatting some output.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public final class NumberFormatter {

	/**
	 * Formats a decimal number with the specific pattern.
	 */
	private static DecimalFormat decFormat = new DecimalFormat("#,##0.##");

	/**
	 * Formats a decimal number and returns it in milliseconds format.
	 */
	private static DecimalFormat millisFormat = new DecimalFormat("0.00");

	/**
	 * Formats a decimal number and returns it in nanoseconds format.
	 */
	private static DecimalFormat nanosFormat = new DecimalFormat("0.00");

	/**
	 * Formats a decimal number with the specified pattern.
	 */
	private static DecimalFormat cpuFormat = new DecimalFormat("#.##");

	/**
	 * Formats a decimal number with the specified pattern.
	 */
	private static DecimalFormat intFormat = new DecimalFormat(",###");

	/**
	 * Formats a decimal number with the specified pattern.
	 */
	private static DecimalFormat doubleFormat = new DecimalFormat("#.###");

	/**
	 * Formats a date/time value with the specified pattern.
	 */
	private static DateFormat dateMillisFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

	/**
	 * Formats a date/time value with the specified pattern.
	 */
	private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	/**
	 * The default private constructor.
	 */
	private NumberFormatter() {
	}

	/**
	 * Converts time in milliseconds to a <code>String</code> in the format
	 * HH:mm:ss.
	 * 
	 * @param time
	 *            the time in milliseconds.
	 * @return a <code>String</code> representing the time in the format
	 *         HH:mm:ss.
	 */
	public static String millisecondsToString(long time) {
		int seconds = (int) (time / 1000 % 60);
		int minutes = (int) ((time / 60000) % 60);
		int hours = (int) ((time / 3600000) % 24);
		int days = (int) ((time / 3600000) / 24);
		return new String(days + "d " + hours + "h " + minutes + "m " + seconds + "s");
	}

	/**
	 * Formats the time to a String value with milliseconds.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return The formatted string.
	 */
	public static String formatTimeWithMillis(long time) {
		return formatTimeWithMillis(new Date(time));
	}

	/**
	 * Formats the time to a String value with milliseconds.
	 * 
	 * @param date
	 *            The date to format.
	 * @return The formatted string.
	 */
	public static String formatTimeWithMillis(Date date) {
		return dateMillisFormat.format(date);
	}

	/**
	 * Formats the time to a String value.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return The formatted string.
	 */
	public static String formatTime(long time) {
		return formatTime(new Date(time));
	}

	/**
	 * Formats the time to a String value.
	 * 
	 * @param date
	 *            The date to format.
	 * @return The formatted string.
	 */
	public static String formatTime(Date date) {
		return dateFormat.format(date);
	}

	/**
	 * Formats nanoseconds to seconds.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return A formatted string.
	 */
	public static String formatNanosToSeconds(long time) {
		double sec = time / 1000000000d;
		return nanosFormat.format(sec) + " s";
	}

	/**
	 * Formats milliseconds to seconds.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return A formatted string.
	 */
	public static String formatMillisToSeconds(long time) {
		double sec = time / 1000d;
		return millisFormat.format(sec) + " s";
	}

	/**
	 * Formats bytes to kiloBytes.
	 * 
	 * @param bytes
	 *            The bytes to format.
	 * @return A formatted string.
	 */
	public static String formatBytesToKBytes(long bytes) {
		return decFormat.format(bytes / 1024) + " kB";
	}

	/**
	 * Adds a %-sign to a floating number. For example: input = 12 / output = 12
	 * %.
	 * 
	 * @param percent
	 *            The value to format.
	 * @return The formatted string.
	 */
	public static String formatCpuPercent(float percent) {
		return cpuFormat.format(percent) + " %";
	}

	/**
	 * Formats an integer value. For example: input = 1234567 / output =
	 * 1,234,567.
	 * 
	 * @param number
	 *            The value to format.
	 * @return The formatted String.
	 */
	public static String formatInteger(int number) {
		return intFormat.format(number);
	}

	/**
	 * Formats a long value. For example: input = 1234567 / output = 1,234,567.
	 * 
	 * @param number
	 *            The value to format.
	 * @return The formatted string.
	 */
	public static String formatLong(long number) {
		return intFormat.format(number);
	}

	/**
	 * Formats a double value. For example: input = 123545.9876543 / output =
	 * 123545.987.
	 * 
	 * @param number
	 *            The value to format.
	 * @return The formatted string.
	 */
	public static String formatDouble(double number) {
		return doubleFormat.format(number);
	}

}
