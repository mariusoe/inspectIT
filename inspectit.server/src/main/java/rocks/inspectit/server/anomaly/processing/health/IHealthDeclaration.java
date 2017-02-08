package rocks.inspectit.server.anomaly.processing.health;

import java.util.List;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.ProcessingUnit;

/**
 * @author Marius Oehler
 *
 */
public interface IHealthDeclaration {

	HealthStatus declareHelthStatus(List<ProcessingUnit> processors);
}
