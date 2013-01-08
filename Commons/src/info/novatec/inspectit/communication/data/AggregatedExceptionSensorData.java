package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.communication.ExceptionEvent;

/**
 * Aggregated exception sensor data. This objects are used for the purpose of grouping the
 * {@link ExceptionSensorData} objects that have same properties.
 * 
 * @author Ivan Senic
 * 
 */
public class AggregatedExceptionSensorData extends ExceptionSensorData {

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

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getPassed() {
		return passed;
	}

	public void setPassed(long passed) {
		this.passed = passed;
	}

	public long getHandled() {
		return handled;
	}

	public void setHandled(long handled) {
		this.handled = handled;
	}

	/**
	 * {@inheritDoc}
	 */
	public double getInvocationAffiliationPercentage() {
		return (double) getObjectsInInvocationsCount() / created;
	}

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
	 * Aggregates the exception data.
	 * 
	 * @param aggregatedExceptionData
	 */
	public void aggregateExceptionData(AggregatedExceptionSensorData aggregatedExceptionData) {
		super.aggregateInvocationAwareData(aggregatedExceptionData);
		this.setCreated(this.getCreated() + aggregatedExceptionData.getCreated());
		this.setHandled(this.getHandled() + aggregatedExceptionData.getHandled());
		this.setPassed(this.getPassed() + aggregatedExceptionData.getPassed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
