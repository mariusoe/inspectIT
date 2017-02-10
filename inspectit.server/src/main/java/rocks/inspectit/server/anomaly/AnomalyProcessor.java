package rocks.inspectit.server.anomaly;

import java.util.ArrayList;
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


	private List<AnomalyProcessingUnit> processingUnits;

	@PostConstruct
	public void PostConstruct() {
		processingUnits = new ArrayList<>();

		processingUnits.add(createProcessingUnit());
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

	private AnomalyProcessingUnit createProcessingUnit() {
		return beanFactory.getBean(AnomalyProcessingUnit.class);
	}
}
