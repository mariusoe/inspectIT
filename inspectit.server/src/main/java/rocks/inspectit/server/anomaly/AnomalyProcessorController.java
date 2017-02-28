package rocks.inspectit.server.anomaly;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.processing.ProcessingUnit;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroup;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroupContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.ci.event.AbstractAnomalyConfigurationEvent;
import rocks.inspectit.server.ci.event.AbstractAnomalyConfigurationEvent.AnomalyDetectionGroupConfigurationCreatedEvent;
import rocks.inspectit.server.ci.event.AbstractAnomalyConfigurationEvent.AnomalyDetectionGroupConfigurationsLoadedEvent;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyProcessorController implements Runnable, ApplicationListener<AbstractAnomalyConfigurationEvent> {

	@Log
	private Logger log;

	@Autowired
	BeanFactory beanFactory;

	@Autowired
	DefinitionAwareFactory definitionAwareFactory;

	@Autowired
	InfluxDBDao influx;

	@Autowired
	ContextFactory contextFactory;

	private List<ProcessingUnitGroup> processingUnitGroups = new ArrayList<>();

	// @PostConstruct
	// public void test() {
	// try {
	// ciAnomalyManager.createAnomalyDetectionConfigurationGroup(AnomalyDetectionGroupConfiguration.getTestConfiguration());
	// } catch (JAXBException | IOException e) {
	// e.printStackTrace();
	// }
	// }

	private void createProcessingUnitGroups(List<AnomalyDetectionGroupConfiguration> groupConfigurations) {
		for (AnomalyDetectionGroupConfiguration groupConfiguration : groupConfigurations) {
			ProcessingUnitGroup unitGroup = createProcessingUnitGroup(groupConfiguration);
			processingUnitGroups.add(unitGroup);
		}
	}

	private ProcessingUnitGroup createProcessingUnitGroup(AnomalyDetectionGroupConfiguration groupConfiguration) {
		ProcessingUnitGroupContext groupContext = contextFactory.createProcessingGroupContext(groupConfiguration);

		ProcessingUnitGroup processingUnitGroup = (ProcessingUnitGroup) beanFactory.getBean("processingUnitGroup", groupContext);

		for (ProcessingUnitContext unitContext : groupContext.getProcessingUnitContexts()) {
			ProcessingUnit processingUnit = createProcessingUnit(unitContext);
			processingUnitGroup.getProcessingUnits().add(processingUnit);
		}

		return processingUnitGroup;
	}

	private ProcessingUnit createProcessingUnit(ProcessingUnitContext unitContext) {
		ProcessingUnit processingUnit = (ProcessingUnit) beanFactory.getBean("processingUnit", unitContext);

		AbstractMetricProvider<?> metricProvider = definitionAwareFactory.createMetricProvider(unitContext.getConfiguration().getMetricDefinition());
		processingUnit.setMetricProvider(metricProvider);

		AbstractBaseline<?> baseline = definitionAwareFactory.createBaseline(unitContext.getConfiguration().getBaselineDefinition());
		processingUnit.setBaseline(baseline);

		AbstractThreshold<?> threshold = definitionAwareFactory.createThreshold(unitContext.getConfiguration().getThresholdDefinition());
		processingUnit.setThreshold(threshold);

		AbstractClassifier<?> classifier = definitionAwareFactory.createClassifier(unitContext.getConfiguration().getClassifierDefinition());
		processingUnit.setClassifier(classifier);

		return processingUnit;
	}

	public void initialize() {
		for (ProcessingUnitGroup unitGroup : processingUnitGroups) {
			unitGroup.initialize();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (ProcessingUnitGroup unitGroup : processingUnitGroups) {
			try {
				unitGroup.process();
			} catch (Exception e) {
				log.error("Unexpected exception.", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(AbstractAnomalyConfigurationEvent event) {
		if (event instanceof AnomalyDetectionGroupConfigurationCreatedEvent) {
			AnomalyDetectionGroupConfigurationCreatedEvent createdEvent = (AnomalyDetectionGroupConfigurationCreatedEvent) event;

			// create and init
			createProcessingUnitGroups(ImmutableList.of(createdEvent.getGroupConfiguration()));

		} else if (event instanceof AnomalyDetectionGroupConfigurationsLoadedEvent) {
			AnomalyDetectionGroupConfigurationsLoadedEvent loadedEvent = (AnomalyDetectionGroupConfigurationsLoadedEvent) event;

			// create and init
			createProcessingUnitGroups(loadedEvent.getGroupConfigurations());
		}
	}
}
