package info.novatec.inspectit.cmr.processor;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;

/**
 * Abstract processor class for CMR data.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractCmrDataProcessor {

	/**
	 * Specifies whether only the monitoring processor should be active.
	 */
	@Value("${monitoring.dataOnly}")
	private boolean onlyMonitoringProcessor;

	/**
	 * Processes many {@link DefaultData} objects.
	 *
	 * @param defaultDatas
	 *            Default data objects.
	 * @param entityManager
	 *            {@link EntityManager} to save data in DB if needed.
	 */
	public void process(Collection<? extends DefaultData> defaultDatas, EntityManager entityManager) {
		for (DefaultData defaultData : defaultDatas) {
			process(defaultData, entityManager);
		}
	}

	/**
	 * Processes one {@link DefaultData} object. This method will check is
	 * {@link #canBeProcessed(DefaultData)} is true, and then delegate the processing to the
	 * {@link #processData(DefaultData)} method.
	 *
	 * @param defaultData
	 *            Default data object.
	 * @param entityManager
	 *            {@link EntityManager} to save data in DB if needed.
	 */
	public void process(DefaultData defaultData, EntityManager entityManager) {
		if (canBeProcessed(defaultData) && isActive()) {
			processData(defaultData, entityManager);
		}
	}

	/**
	 * Returns whether this processor is active. Basically, this method is returning false, if only
	 * monitoring processor should be active but the processor is not a monitoring processor.
	 *
	 * @return True if the processor is active
	 */
	private boolean isActive() {
		return !onlyMonitoringProcessor || isMonitoringProcessor();
	}

	/**
	 * Returns whether this is a data processor for monitoring purpose.
	 *
	 * @return True if it a monitoring processor, false otherwise.
	 */
	protected boolean isMonitoringProcessor() {
		return false;
	}

	/**
	 * Concrete method for processing. Implemented by sub-classes.
	 *
	 * @param defaultData
	 *            Default data object.
	 * @param entityManager
	 *            {@link EntityManager} to save data in DB if needed.
	 */
	protected abstract void processData(DefaultData defaultData, EntityManager entityManager);

	/**
	 * Returns if the {@link DefaultData} object can be processed by this
	 * {@link AbstractDataProcessor}.
	 *
	 * @param defaultData
	 *            Default data object.
	 * @return True if data can be processed, false otherwise.
	 */
	public abstract boolean canBeProcessed(DefaultData defaultData);
}
