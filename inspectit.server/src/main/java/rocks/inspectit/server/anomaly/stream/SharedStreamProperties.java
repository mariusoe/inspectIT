/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import rocks.inspectit.server.tsdb.InfluxDBService;

/**
 * @author Marius Oehler
 *
 */
public class SharedStreamProperties {

	private static double confidenceBandUpper = Double.NaN;

	private static double confidenceBandLower = Double.NaN;

	private static double upperThreeSigmaThreshold = 0;

	private static double lowerThreeSigmaThreshold = 0;

	private static double stddev = 0;

	private static boolean baselineAvailable = false;

	private static InfluxDBService influxService;

	private SharedStreamProperties() {
	}

	/**
	 * Gets {@link #baselineAvailable}.
	 *
	 * @return {@link #baselineAvailable}
	 */
	public static boolean isBaselineAvailable() {
		return baselineAvailable;
	}

	/**
	 * Sets {@link #baselineAvailable}.
	 *
	 * @param baselineAvailable
	 *            New value for {@link #baselineAvailable}
	 */
	public static void setBaselineAvailable(boolean baselineAvailable) {
		SharedStreamProperties.baselineAvailable = baselineAvailable;
	}

	/**
	 * Gets {@link #upperThreeSigmaThreshold}.
	 *
	 * @return {@link #upperThreeSigmaThreshold}
	 */
	public static double getUpperThreeSigmaThreshold() {
		return upperThreeSigmaThreshold;
	}

	/**
	 * Gets {@link #confidenceBandUpper}.
	 * 
	 * @return {@link #confidenceBandUpper}
	 */
	public static double getConfidenceBandUpper() {
		return confidenceBandUpper;
	}

	/**
	 * Sets {@link #confidenceBandUpper}.
	 * 
	 * @param confidenceBandUpper
	 *            New value for {@link #confidenceBandUpper}
	 */
	public static void setConfidenceBandUpper(double confidenceBandUpper) {
		SharedStreamProperties.confidenceBandUpper = confidenceBandUpper;
	}

	/**
	 * Gets {@link #confidenceBandLower}.
	 * 
	 * @return {@link #confidenceBandLower}
	 */
	public static double getConfidenceBandLower() {
		return confidenceBandLower;
	}

	/**
	 * Sets {@link #confidenceBandLower}.
	 * 
	 * @param confidenceBandLower
	 *            New value for {@link #confidenceBandLower}
	 */
	public static void setConfidenceBandLower(double confidenceBandLower) {
		SharedStreamProperties.confidenceBandLower = confidenceBandLower;
	}

	/**
	 * Sets {@link #upperThreeSigmaThreshold}.
	 *
	 * @param upperThreeSigmaThreshold
	 *            New value for {@link #upperThreeSigmaThreshold}
	 */
	public static void setUpperThreeSigmaThreshold(double upperThreeSigmaThreshold) {
		SharedStreamProperties.upperThreeSigmaThreshold = upperThreeSigmaThreshold;
	}

	/**
	 * Gets {@link #stddev}.
	 *
	 * @return {@link #stddev}
	 */
	public static double getStddev() {
		return stddev;
	}

	/**
	 * Sets {@link #stddev}.
	 *
	 * @param stddev
	 *            New value for {@link #stddev}
	 */
	public static void setStddev(double stddev) {
		SharedStreamProperties.stddev = stddev;
	}

	/**
	 * Gets {@link #lowerThreeSigmaThreshold}.
	 *
	 * @return {@link #lowerThreeSigmaThreshold}
	 */
	public static double getLowerThreeSigmaThreshold() {
		return lowerThreeSigmaThreshold;
	}

	/**
	 * Sets {@link #lowerThreeSigmaThreshold}.
	 *
	 * @param lowerThreeSigmaThreshold
	 *            New value for {@link #lowerThreeSigmaThreshold}
	 */
	public static void setLowerThreeSigmaThreshold(double lowerThreeSigmaThreshold) {
		SharedStreamProperties.lowerThreeSigmaThreshold = lowerThreeSigmaThreshold;
	}

	/**
	 * Gets {@link #influxService}.
	 *
	 * @return {@link #influxService}
	 */
	public static InfluxDBService getInfluxService() {
		return influxService;
	}

	/**
	 * Sets {@link #influxService}.
	 *
	 * @param influxService
	 *            New value for {@link #influxService}
	 */
	public static void setInfluxService(InfluxDBService influxService) {
		SharedStreamProperties.influxService = influxService;
	}

}
