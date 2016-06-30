/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.server.alearting.adapter.IAlertAdapter;
import rocks.inspectit.server.tsdb.InfluxDBService;

/**
 * @author Marius Oehler
 *
 */
public class SharedStreamProperties {

	private static InfluxDBService influxService;

	private static IAlertAdapter alertingComponent;

	/**
	 * Gets {@link #alertingComponent}.
	 *
	 * @return {@link #alertingComponent}
	 */
	public static IAlertAdapter getAlertingComponent() {
		return alertingComponent;
	}

	/**
	 * Sets {@link #alertingComponent}.
	 *
	 * @param alertingComponent
	 *            New value for {@link #alertingComponent}
	 */
	public static void setAlertingComponent(IAlertAdapter alertingComponent) {
		SharedStreamProperties.alertingComponent = alertingComponent;
	}

	private static Map<String, StreamStatistics> streamStatisticMap = new HashMap<>();

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

	public static StreamStatistics getStreamStatistic(String businessTransaction) {
		if (!streamStatisticMap.containsKey(businessTransaction)) {
			synchronized (streamStatisticMap) {
				if (!streamStatisticMap.containsKey(businessTransaction)) {
					streamStatisticMap.put(businessTransaction, new StreamStatistics());
				}
			}
		}
		return streamStatisticMap.get(businessTransaction);
	}

}
