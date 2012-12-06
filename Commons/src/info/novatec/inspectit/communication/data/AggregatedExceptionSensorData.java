package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.ExceptionEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
/**
 * Aggregated exception sensor data. This objects are used for the purpose of grouping the
 * {@link ExceptionSensorData} objects that have same properties.
 * 
 * @author Ivan Senic
 * 
 */
public class AggregatedExceptionSensorData extends ExceptionSensorData implements IAggregatedData<ExceptionSensorData> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -7258567013769154129L;

	/**
	 * Created count.
	 */
	private long created;

	/**
	 * Passed count.
	 */
	private long passed;

	/**
	 * Handled count.
	 */
	private long handled;

	/**
	 * Aggregated Ids.
	 */
	private Set<Long> aggregatedIds;

	/**
	 * Gets {@link #created}.
	 * 
	 * @return {@link #created}
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * Sets {@link #created}.
	 * 
	 * @param created
	 *            New value for {@link #created}
	 */
	public void setCreated(long created) {
		this.created = created;
	}

	/**
	 * Gets {@link #passed}.
	 * 
	 * @return {@link #passed}
	 */
	public long getPassed() {
		return passed;
	}

	/**
	 * Sets {@link #passed}.
	 * 
	 * @param passed
	 *            New value for {@link #passed}
	 */
	public void setPassed(long passed) {
		this.passed = passed;
	}

	/**
	 * Gets {@link #handled}.
	 * 
	 * @return {@link #handled}
	 */
	public long getHandled() {
		return handled;
	}

	/**
	 * Sets {@link #handled}.
	 * 
	 * @param handled
	 *            New value for {@link #handled}
	 */
	public void setHandled(long handled) {
		this.handled = handled;
	}

	/**
	 * {@inheritDoc}
	 */
	public double getInvocationAffiliationPercentage() {
		return (double) getObjectsInInvocationsCount() / created;
	}

	/**
	 * aggregates the given exception data to this instance.
	 * 
	 * @param exceptionData
	 *            {@link ExceptionSensorData}
	 */
	public void aggregateExceptionData(ExceptionSensorData exceptionData) {
		if (exceptionData.getExceptionEvent() == ExceptionEvent.CREATED) {
			created++;
		} else if (exceptionData.getExceptionEvent() == ExceptionEvent.PASSED) {
			passed++;
		} else if (exceptionData.getExceptionEvent() == ExceptionEvent.HANDLED) {
			handled++;
		}
		if (null != exceptionData.getInvocationParentsIdSet()) {
			for (Long parentId : exceptionData.getInvocationParentsIdSet()) {
				this.addInvocationParentId(parentId);
			}
		}
	}

	/**
	 * Aggregates the {@link AggregatedExceptionSensorData}.
	 * 
	 * @param aggregatedExceptionData
	 *            {@link AggregatedExceptionSensorData}
	 */
	public void aggregateExceptionData(AggregatedExceptionSensorData aggregatedExceptionData) {
		if (null != aggregatedExceptionData.getAggregatedIds()) {
			if (null != aggregatedIds) {
				aggregatedIds.addAll(aggregatedExceptionData.getAggregatedIds());
			} else {
				aggregatedIds = new HashSet<Long>(aggregatedExceptionData.getAggregatedIds());
			}
		}
		super.aggregateInvocationAwareData(aggregatedExceptionData);
		this.setCreated(this.getCreated() + aggregatedExceptionData.getCreated());
		this.setHandled(this.getHandled() + aggregatedExceptionData.getHandled());
		this.setPassed(this.getPassed() + aggregatedExceptionData.getPassed());
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(ExceptionSensorData data) {
		this.aggregateExceptionData(data);
		if (0 != data.getId()) {
			if (null == aggregatedIds) {
				aggregatedIds = new HashSet<Long>();
			}
			aggregatedIds.add(data.getId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<Long> getAggregatedIds() {
		return aggregatedIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public ExceptionSensorData getData() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, false);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 3, 0);
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
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + (int) (handled ^ (handled >>> 32));
		result = prime * result + (int) (passed ^ (passed >>> 32));
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
		AggregatedExceptionSensorData other = (AggregatedExceptionSensorData) obj;
		if (aggregatedIds == null) {
			if (other.aggregatedIds != null) {
				return false;
			}
		} else if (!aggregatedIds.equals(other.aggregatedIds)) {
			return false;
		}
		if (created != other.created) {
			return false;
		}
		if (handled != other.handled) {
			return false;
		}
		if (passed != other.passed) {
			return false;
		}
		return true;
	}

}
