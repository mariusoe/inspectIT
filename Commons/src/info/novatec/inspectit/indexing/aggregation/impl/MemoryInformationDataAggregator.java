package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * {@link IAggregator} for {@link MemoryInformationData}.
 * 
 * @author Ivan Senic
 * 
 */
public class MemoryInformationDataAggregator implements IAggregator<MemoryInformationData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 1258455695092762583L;

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<MemoryInformationData> aggregatedObject, MemoryInformationData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public MemoryInformationData getClone(MemoryInformationData memoryInformationData) {
		MemoryInformationData clone = new MemoryInformationData();
		clone.setPlatformIdent(memoryInformationData.getPlatformIdent());
		clone.setSensorTypeIdent(memoryInformationData.getSensorTypeIdent());
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
	public Object getAggregationKey(MemoryInformationData object) {
		return object.getPlatformIdent();
	}

}
