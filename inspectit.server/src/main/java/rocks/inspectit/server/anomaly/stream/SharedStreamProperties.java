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

	private static InfluxDBService influxService;

	private static ConfidenceBand confidenceBand;

	private static double standardDeviation;

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

	/**
	 * Gets {@link #confidenceBand}.
	 *
	 * @return {@link #confidenceBand}
	 */
	public static ConfidenceBand getConfidenceBand() {
		return confidenceBand;
	}

	/**
	 * Sets {@link #confidenceBand}.
	 *
	 * @param confidenceBand
	 *            New value for {@link #confidenceBand}
	 */
	public static void setConfidenceBand(ConfidenceBand confidenceBand) {
		SharedStreamProperties.confidenceBand = confidenceBand;
	}

	private SharedStreamProperties() {
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
