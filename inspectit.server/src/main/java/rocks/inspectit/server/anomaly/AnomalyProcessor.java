package rocks.inspectit.server.anomaly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.processing.AnomalyProcessingUnit;
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

		return processingUnit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (AnomalyProcessingUnit processingUnit : processingUnits) {
			processingUnit.process();
		}
	}
}
