package info.novatec.inspectit.storage.processor.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

/**
 * This processor writes an cloned invocation without children to the {@link StorageWriter}.
 * 
 * @author Ivan Senic
 * 
 */
public class InvocationClonerDataProcessor extends AbstractDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4882060472220163089L;

	/**
	 * {@inheritDoc}
	 */
	protected void processData(DefaultData defaultData) {
		if (defaultData instanceof InvocationSequenceData) {
			InvocationSequenceData invocation = (InvocationSequenceData) defaultData;
			InvocationSequenceData clone = invocation.getClonedInvocationSequence();
			getStorageWriter().write(clone);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
