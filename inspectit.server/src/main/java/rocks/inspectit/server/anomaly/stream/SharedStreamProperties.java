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

	private static double standardDeviation = Double.NaN;

	private static InfluxDBService influxService;

	private SharedStreamProperties() {
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

	/**
	 * Gets {@link #standardDeviation}.
	 * 
	 * @return {@link #standardDeviation}
	 */
	public static double getStandardDeviation() {
		return standardDeviation;
	}

	/**
	 * Sets {@link #standardDeviation}.
	 * 
	 * @param standardDeviation
	 *            New value for {@link #standardDeviation}
	 */
	public static void setStandardDeviation(double standardDeviation) {
		SharedStreamProperties.standardDeviation = standardDeviation;
	}

}
