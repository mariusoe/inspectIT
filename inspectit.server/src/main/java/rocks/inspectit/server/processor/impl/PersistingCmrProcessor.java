package rocks.inspectit.server.processor.impl;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Processor that saves objects to database via {@link EntityManager}.
 *
 * @author Ivan Senic
 *
 */
public class PersistingCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * If this value is true, we will not persist anything to the relational database but writing to
	 * the InfluxDB.
	 */
	@Value("${influxdb.writeData}")
	boolean writeInInflux;

	/**
	 * List of classes that should be saved by this simple saver.
	 */
	private List<Class<? extends DefaultData>> classes;

	/**
	 * Default constructor.
	 *
	 * @param classes
	 *            List of classes that should be saved by this simple saver.
	 */
	public PersistingCmrProcessor(List<Class<? extends DefaultData>> classes) {
		this.classes = classes;
		if (null == this.classes) {
			this.classes = Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		defaultData.setId(0);
		entityManager.persist(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		if (!writeInInflux && (null != defaultData)) {
			return classes.contains(defaultData.getClass());
		}
		return false;
	}

}