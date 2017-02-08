package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.anomaly.classification.HealthState;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDetectionProcessor extends AbstractCmrDataProcessor {

	@Autowired
	private AnomalyDetectionSystem anomalyDetectionSystem;

	private boolean enabled = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		HealthState healthState = anomalyDetectionSystem.classify(defaultData);

		System.out.println(healthState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return enabled && anomalyDetectionSystem.canBeProcessed(defaultData);
	}

}
