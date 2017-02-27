package rocks.inspectit.server.anomaly;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyRegistry {

	private Map<String, Anomaly> anomalies = new HashMap<>();

	public Anomaly startAnomaly(long time) {
		Anomaly anomaly = new Anomaly(time);

		anomalies.put(anomaly.getId(), anomaly);

		return anomaly;
	}

	public Anomaly getAnomaly(String id) {
		return anomalies.get(id);
	}

	/**
	 * @param time
	 * @param currentAnomaly
	 */
	public void endAnomaly(long time, Anomaly currentAnomaly) {
		currentAnomaly.setEndTime(time);
	}
}
