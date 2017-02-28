package rocks.inspectit.server.anomaly;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroupContext;
import rocks.inspectit.server.anomaly.state.AnomalyStateContext;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.definition.anomaly.AnomalyDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
public class ContextFactory {

	/**
	 * @param groupConfiguration
	 * @return
	 */
	public ProcessingUnitGroupContext createProcessingGroupContext(AnomalyDetectionGroupConfiguration groupConfiguration) {
		ProcessingUnitGroupContext groupContext = new ProcessingUnitGroupContext();

		groupContext.setGroupId(groupConfiguration.getGroupId());
		groupContext.setGroupConfiguration(groupConfiguration);

		AnomalyStateContext anomalyStateContext = createAnomalyStateContext(groupConfiguration.getAnomalyDefinition());
		groupContext.setAnomalyStateContext(anomalyStateContext);

		for (AnomalyDetectionConfiguration configuration : groupConfiguration.getConfigurations()) {
			ProcessingUnitContext unitContext = createProcessingContext(groupContext, configuration);
			groupContext.getProcessingUnitContexts().add(unitContext);
		}

		return groupContext;
	}

	private ProcessingUnitContext createProcessingContext(ProcessingUnitGroupContext groupContext, AnomalyDetectionConfiguration configuration) {
		ProcessingUnitContext unitContext = new ProcessingUnitContext();

		unitContext.setGroupContext(groupContext);
		unitContext.setConfiguration(configuration);

		return unitContext;
	}

	private AnomalyStateContext createAnomalyStateContext(AnomalyDefinition anomalyDefinition) {
		AnomalyStateContext stateContext = new AnomalyStateContext();

		int contextHealthListSize = Math.max(anomalyDefinition.getStartCount(), anomalyDefinition.getEndCount());

		stateContext.setMaxHealthListSize(contextHealthListSize);

		return stateContext;
	}
}
