package rocks.inspectit.server.anomaly.processing.health;

import java.util.List;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.IAnomalyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class BestHealthDeclaration implements IHealthDeclaration {

	public static final BestHealthDeclaration INSTANCE = new BestHealthDeclaration();

	private BestHealthDeclaration() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthStatus declareHelthStatus(List<IAnomalyProcessor> processors) {
		HealthStatus status = HealthStatus.UNKNOWN;

		for (IAnomalyProcessor processor : processors) {
			if ((status == HealthStatus.UNKNOWN) || (processor.getHealthStatus().ordinal() < status.ordinal())) {
				status = processor.getHealthStatus();
			}
		}

		return status;
	}

}
