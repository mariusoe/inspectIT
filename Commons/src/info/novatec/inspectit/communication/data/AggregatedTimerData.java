package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.IAggregatedData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Aggregated {@link TimerData} object.
 * 
 * @author Ivan Senic
 * 
 */
public class AggregatedTimerData extends TimerData implements IAggregatedData<TimerData> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5458552145998385702L;

	/**
	 * Aggregated Ids.
	 */
	private Set<Long> aggregatedIds;

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(TimerData data) {
		this.aggregateTimerData(data);
		if (0 != data.getId()) {
			if (null == aggregatedIds) {
				aggregatedIds = new HashSet<Long>();
			}
			aggregatedIds.add(data.getId());
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public Collection<Long> getAggregatedIds() {
		return aggregatedIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public TimerData getData() {
		return this;
	}

	/**
	 * Aggregates the {@link AggregatedTimerData}.
	 * 
	 * @param data
	 *            {@link AggregatedTimerData}
	 */
	public void aggregateTimerData(AggregatedTimerData data) {
		if (null != data.getAggregatedIds()) {
			if (null != aggregatedIds) {
				aggregatedIds.addAll(data.getAggregatedIds());
			} else {
				aggregatedIds = new HashSet<Long>(data.getAggregatedIds());
			}
		}
		super.aggregateTimerData(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, false);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		if (null != aggregatedIds) {
			size += objectSizes.getSizeOfHashSet(aggregatedIds.size());
			size += aggregatedIds.size() * objectSizes.getSizeOfLongObject();
		}
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((aggregatedIds == null) ? 0 : aggregatedIds.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AggregatedTimerData other = (AggregatedTimerData) obj;
		if (aggregatedIds == null) {
			if (other.aggregatedIds != null) {
				return false;
			}
		} else if (!aggregatedIds.equals(other.aggregatedIds)) {
			return false;
		}
		return true;
	}

}
