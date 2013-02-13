package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * {@link IAggregator} for the {@link ThreadInformationData}.
 * 
 * @author Ivan Senic
 * 
 */
public class ThreadInformationDataAggregator implements IAggregator<ThreadInformationData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 749269646913958594L;

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<ThreadInformationData> aggregatedObject, ThreadInformationData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public ThreadInformationData getClone(ThreadInformationData threadInformationData) {
		ThreadInformationData clone = new ThreadInformationData();
		clone.setPlatformIdent(threadInformationData.getPlatformIdent());
		clone.setSensorTypeIdent(threadInformationData.getSensorTypeIdent());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCloning() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(ThreadInformationData object) {
		return object.getPlatformIdent();
	}

}
