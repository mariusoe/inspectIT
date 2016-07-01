/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rocks.inspectit.server.alearting.adapter.IAlertAdapter;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;

/**
 * @author Marius Oehler
 *
 */
public class SharedStreamProperties {

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

	private final Map<String, StreamContext> streamContextMap = new HashMap<>();

	private SharedStreamProperties() {
	}

	/**
	 * Gets {@link #streamContextMap}.
	 *
	 * @return {@link #streamContextMap}
	 */
	public Map<String, StreamContext> getStreamContextMap() {
		return streamContextMap;
	}

	public List<String> getBusinessTransactions() {
		return new ArrayList<String>(streamContextMap.keySet());
	}

	public StreamContext getStreamContext(String businessTransaction) {
		return streamContextMap.get(businessTransaction);
	}
}
