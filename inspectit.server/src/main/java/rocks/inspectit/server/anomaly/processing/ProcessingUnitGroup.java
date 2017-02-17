package rocks.inspectit.server.anomaly.processing;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.HealthStatus;
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

	private AnomalyDetectionConfigurationGroup configurationGroup;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthStatus getHealthStatus() {
		switch (configurationGroup.getMode()) {
		case HIGHEST:
			return healthOnce();
		case LOWEST:
			return healthAll();
		default:
			throw new RuntimeException("Unknown mode for health");
		}
	}

	private HealthStatus healthOnce() {
		HealthStatus status = HealthStatus.UNKNOWN;

		for (IAnomalyProcessor processor : processors) {
			if (processor.getHealthStatus().ordinal() > status.ordinal()) {
				status = processor.getHealthStatus();
			}
		}

		return status;
	}

	private HealthStatus healthAll() {
		HealthStatus status = HealthStatus.UNKNOWN;

		for (IAnomalyProcessor processor : processors) {
			if ((status == HealthStatus.UNKNOWN) || (processor.getHealthStatus().ordinal() < status.ordinal())) {
				status = processor.getHealthStatus();
			}
		}

		return status;
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
	 * Sets {@link #configurationGroup}.
	 *
	 * @param configurationGroup
	 *            New value for {@link #configurationGroup}
	 */
	public void setConfigurationGroup(AnomalyDetectionConfigurationGroup configurationGroup) {
		this.configurationGroup = configurationGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(long time) {
		for (IAnomalyProcessor processor : processors) {
			processor.process(time);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(long time) {
		for (IAnomalyProcessor processor : processors) {
			processor.initialize(time);
		}
	}

	/**
	 * Gets {@link #processors}.
	 *
	 * @return {@link #processors}
	 */
	public List<IAnomalyProcessor> getProcessors() {
		return this.processors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		for (int i = 0; i < processors.size(); i++) {
			b.append("\t processor ").append(i).append(": ").append(processors.get(i).getHealthStatus());
		}

		b.append("\t total: ").append(getHealthStatus());

		return b.toString();
	}
}
