/**
 *
 */
package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.anomaly.stream.AnomalyStreamSystem;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.util.CacheIdGenerator;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyPipeProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link CacheIdGenerator}.
	 */
	@Autowired
	AnomalyStreamSystem streamSystem;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		streamSystem.getStream().process((InvocationSequenceData) defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
