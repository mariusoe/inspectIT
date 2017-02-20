package rocks.inspectit.server.anomaly.processing.health;

import java.util.List;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.IAnomalyProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IHealthDeclaration {

	HealthStatus declareHelthStatus(List<IAnomalyProcessor> processors);
}
