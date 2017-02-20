package rocks.inspectit.server.anomaly.processing.health;

import java.util.List;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.IAnomalyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class WorstHealthDeclaration implements IHealthDeclaration {

	public static final WorstHealthDeclaration INSTANCE = new WorstHealthDeclaration();

	private WorstHealthDeclaration() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthStatus declareHelthStatus(List<IAnomalyProcessor> processors) {
		HealthStatus status = HealthStatus.UNKNOWN;

		for (IAnomalyProcessor processor : processors) {
			if (processor.getHealthStatus().ordinal() > status.ordinal()) {
				status = processor.getHealthStatus();
			}
		}

		return status;
	}

}
