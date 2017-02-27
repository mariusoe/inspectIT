package rocks.inspectit.server.anomaly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.processing.ProcessingGroupContext;
import rocks.inspectit.server.anomaly.processing.ProcessingUnit;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroup;
import rocks.inspectit.server.anomaly.processing.RootProcessingUnitGroup;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.ci.ConfigurationInterfaceManager;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyProcessorController implements Runnable {

	@Log
	private Logger log;

	@Autowired
	BeanFactory beanFactory;

	@Autowired
	DefinitionAwareFactory definitionAwareFactory;

	@Autowired
	InfluxDBDao influx;

	@Autowired
	ConfigurationInterfaceManager ciManager;

	private List<RootProcessingUnitGroup> processingUnitGroups = new ArrayList<>();

	@PostConstruct
	public void PostConstruct() {
		createProcessingUnitGroups();
	}

	private void createProcessingUnitGroups() {
		// load configurations
		List<AnomalyDetectionGroupConfiguration> configurationGroups = Collections.singletonList(AnomalyDetectionGroupConfiguration.getTestConfiguration());

		// try {
		// ciManager.createAnomalyDetectionConfigurationGroup(configurationGroups.get(0));
		// } catch (JAXBException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		for (AnomalyDetectionGroupConfiguration groupConfiguration : configurationGroups) {
			RootProcessingUnitGroup unitGroup = (RootProcessingUnitGroup) createProcessingUnitGroup(groupConfiguration, true);
			processingUnitGroups.add(unitGroup);
		}
	}

	private ProcessingUnitGroup createProcessingUnitGroup(AnomalyDetectionGroupConfiguration groupConfiguration, boolean isRoot) {
		ProcessingUnitGroup processingUnitGroup = null;
		if (isRoot) {
			processingUnitGroup = (ProcessingUnitGroup) beanFactory.getBean("rootProcessingUnitGroup", groupConfiguration);
		} else {
			processingUnitGroup = (ProcessingUnitGroup) beanFactory.getBean("processingUnitGroup", groupConfiguration);
		}

		for (AnomalyDetectionConfiguration configuration : groupConfiguration.getConfigurations()) {
			ProcessingUnit processingUnit = createProcessingUnit(processingUnitGroup.getGroupContext(), configuration);
			processingUnitGroup.getProcessors().add(processingUnit);
		}

		for (AnomalyDetectionGroupConfiguration innerGroupConfiguration : groupConfiguration.getConfigurationGroups()) {
			ProcessingUnitGroup innerProcessingUnitGroup = createProcessingUnitGroup(innerGroupConfiguration, false);
			processingUnitGroup.getProcessors().add(innerProcessingUnitGroup);
		}

		return processingUnitGroup;
	}

	private ProcessingUnit createProcessingUnit(ProcessingGroupContext groupContext, AnomalyDetectionConfiguration configuration) {
		ProcessingUnit processingUnit = (ProcessingUnit) beanFactory.getBean("processingUnit", groupContext, configuration);

		AbstractMetricProvider<?> metricProvider = definitionAwareFactory.createMetricProvider(configuration.getMetricDefinition());
		processingUnit.setMetricProvider(metricProvider);

		AbstractBaseline<?> baseline = definitionAwareFactory.createBaseline(configuration.getBaselineDefinition());
		processingUnit.setBaseline(baseline);

		AbstractThreshold<?> threshold = definitionAwareFactory.createThreshold(configuration.getThresholdDefinition());
		processingUnit.setThreshold(threshold);

		AbstractClassifier<?> classifier = definitionAwareFactory.createClassifier(configuration.getClassifierDefinition());
		processingUnit.setClassifier(classifier);

		return processingUnit;
	}

	/**
	 *
	 */
	public void initialize() {
		for (RootProcessingUnitGroup unitGroup : processingUnitGroups) {
			unitGroup.initialize();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (RootProcessingUnitGroup unitGroup : processingUnitGroups) {
			try {
				unitGroup.process();
			} catch (Exception e) {
				log.error("Unexpected exception.", e);
			}
		}
	}
}
