package rocks.inspectit.server.anomaly.processing;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.health.BestHealthDeclaration;
import rocks.inspectit.server.anomaly.processing.health.WorstHealthDeclaration;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfigurationGroup;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class ProcessingUnitGroup implements IAnomalyProcessor {

	@Log
	private Logger log;

	private List<IAnomalyProcessor> processors = new ArrayList<>();

	private final AnomalyDetectionConfigurationGroup configurationGroup;

	private HealthStatus currentHealthStatus = HealthStatus.UNKNOWN;

	@Autowired
	public ProcessingUnitGroup(AnomalyDetectionConfigurationGroup configurationGroup) {
		this.configurationGroup = configurationGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthStatus getHealthStatus() {
		return currentHealthStatus;
	}

	private void updateHealthStatus() {
		switch (configurationGroup.getMode()) {
		case WORST:
			currentHealthStatus = WorstHealthDeclaration.INSTANCE.declareHelthStatus(processors);
			break;
		case BEST:
			currentHealthStatus = BestHealthDeclaration.INSTANCE.declareHelthStatus(processors);
			break;
		default:
			throw new RuntimeException("Unknown mode for health");
		}
	}

	/**
	 * Gets {@link #configurationGroup}.
	 *
	 * @return {@link #configurationGroup}
	 */
	public AnomalyDetectionConfigurationGroup getConfigurationGroup() {
		return this.configurationGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(long time) {
		for (IAnomalyProcessor processor : processors) {
			processor.process(time);
		}

		updateHealthStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(long time) {
		for (IAnomalyProcessor processor : processors) {
			processor.initialize(time);
		}

		updateHealthStatus();
	}

	/**
	 * Gets {@link #processors}.
	 *
	 * @return {@link #processors}
	 */
	public List<IAnomalyProcessor> getProcessors() {
		return this.processors;
	}
}
