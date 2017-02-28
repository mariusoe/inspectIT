package rocks.inspectit.server.anomaly.processing.health;

import java.util.List;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.ProcessingUnit;

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
	public HealthStatus declareHelthStatus(List<ProcessingUnit> processors) {
		HealthStatus status = HealthStatus.UNKNOWN;

		for (ProcessingUnit processor : processors) {
			if (processor.getHealthStatus().ordinal() > status.ordinal()) {
				status = processor.getHealthStatus();
			}
		}

		return status;
	}

}
