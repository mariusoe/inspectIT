package rocks.inspectit.server.anomaly.processing;

import rocks.inspectit.server.anomaly.HealthStatus;

/**
 * @author Marius Oehler
 *
 */
public interface IAnomalyProcessor {

	HealthStatus getHealthStatus();

	void process(long time);

	void initialize(long time);
}
