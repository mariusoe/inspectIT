package info.novatec.inspectit.storage.processor.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DataSaverProcessor} enables definition of classes which objects need to be saved to the
 * storage.
 * 
 * @author Ivan Senic
 * 
 */
public class DataSaverProcessor extends AbstractDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5795459378970312428L;

	/**
	 * List of classes that should be saved by this simple saver.
	 */
	private List<Class<? extends DefaultData>> classes;

	/**
	 * No-arg constructor.
	 */
	public DataSaverProcessor() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param classes
	 *            List of classes to be saved to storage by this {@link AbstractDataProcessor}.
	 */
	public DataSaverProcessor(List<Class<? extends DefaultData>> classes) {
		this.classes = classes;
		if (null == this.classes) {
			this.classes = new ArrayList<Class<? extends DefaultData>>();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void processData(DefaultData defaultData) {
		getStorageWriter().write(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBeProcessed(DefaultData defaultData) {
		if (null != defaultData) {
			return classes.contains(defaultData.getClass());
		}
		return false;
	}

}
