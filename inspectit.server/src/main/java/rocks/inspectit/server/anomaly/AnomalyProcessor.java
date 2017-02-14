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
import rocks.inspectit.server.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingUnit;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyProcessor implements Runnable {

	@Log
	private Logger log;

	@Autowired
	BeanFactory beanFactory;

	@Autowired
	DefinitionAwareFactory definitionAwareFactory;

	private List<AnomalyProcessingUnit> processingUnits = new ArrayList<>();

	@PostConstruct
	public void PostConstruct() {
		createProcessingUnits();
	}

	private void createProcessingUnits() {
		// load configurations
		List<AnomalyDetectionConfiguration> configurations = Collections.singletonList(AnomalyDetectionConfiguration.getTestDefinition());

		for (AnomalyDetectionConfiguration configuration : configurations) {
			processingUnits.add(createProcessingUnit(configuration));
		}
	}

	private AnomalyProcessingUnit createProcessingUnit(AnomalyDetectionConfiguration configuration) {
		AnomalyProcessingUnit processingUnit = beanFactory.getBean(AnomalyProcessingUnit.class);
		processingUnit.setConfiguration(configuration);

		AbstractMetricProvider<?> metricProvider = definitionAwareFactory.createMetricProvider(configuration.getMetricDefinition());
		processingUnit.setMetricProvider(metricProvider);

		AbstractBaseline<?> baseline = definitionAwareFactory.createBaseline(configuration.getBaselineDefinition());
		processingUnit.setBaseline(baseline);

		AbstractThreshold<?> classifier = definitionAwareFactory.createClassifier(configuration.getClassifierDefinition());
		processingUnit.setClassifier(classifier);

		return processingUnit;
	}

	/**
	 *
	 */
	public void initialize() {
		for (AnomalyProcessingUnit processingUnit : processingUnits) {
			processingUnit.initialize();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			for (AnomalyProcessingUnit processingUnit : processingUnits) {
				processingUnit.process();
			}
		} catch (Exception e) {
			log.error("Unexpected exception.", e);
		}
	}
}
