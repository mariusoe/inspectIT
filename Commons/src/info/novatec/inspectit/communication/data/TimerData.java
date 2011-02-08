package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

/**
 * The timer data class stores information about the execution time of a java method.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimerData extends InvocationAwareData {

	/**
	 * Generated serial UID.
	 */
	private static final long serialVersionUID = 8992128958802371539L;

	/**
	 * The minimum value.
	 */
	private double min = Double.MAX_VALUE;

	/**
	 * The maximum value.
	 */
	private double max = 0;

	/**
	 * The count.
	 */
	private long count = 0;

	/**
	 * The complete duration.
	 */
	private double duration = 0;

	/**
	 * The average time.
	 */
	private double average = 0;

	/**
	 * The variance (optional parameter).
	 */
	private double variance;

	/**
	 * The cpu minimum value.
	 */
	private double cpuMin = Double.MAX_VALUE;

	/**
	 * The cpu maximum value.
	 */
	private double cpuMax = 0;

	/**
	 * The cpu complete duration.
	 */
	private double cpuDuration = 0;

	/**
	 * The cpu average time.
	 */
	private double cpuAverage = 0;

	/**
	 * Exclusive count. Needed because this count can be less than the total count.
	 */
	private long exclusiveCount = 0;

	/**
	 * Exclusive duration.
	 */
	private double exclusiveDuration;

	/**
	 * Exclusive max duration;
	 */
	private double exclusiveMax = 0;

	/**
	 * Exclusive min duration.
	 */
	private double exclusiveMin = Double.MAX_VALUE;

	/**
	 * Default no-args constructor.
	 */
	public TimerData() {
	}

	public TimerData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	public TimerData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List parameterContentData) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void increaseCount() {
		this.count++;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public void addDuration(double duration) {
		this.duration += duration;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	/**
	 * @return the cpuMin
	 */
	public double getCpuMin() {
		return cpuMin;
	}

	/**
	 * @param cpuMin
	 *            the cpuMin to set
	 */
	public void setCpuMin(double cpuMin) {
		this.cpuMin = cpuMin;
	}

	/**
	 * @return the cpuMax
	 */
	public double getCpuMax() {
		return cpuMax;
	}

	/**
	 * @param cpuMax
	 *            the cpuMax to set
	 */
	public void setCpuMax(double cpuMax) {
		this.cpuMax = cpuMax;
	}

	/**
	 * @return the cpuDuration
	 */
	public double getCpuDuration() {
		return cpuDuration;
	}

	/**
	 * @param cpuDuration
	 *            the cpuDuration to set
	 */
	public void setCpuDuration(double cpuDuration) {
		this.cpuDuration = cpuDuration;
	}

	/**
	 * @param cpuDuration
	 *            the cpuDuration to add
	 */
	public void addCpuDuration(double cpuDuration) {
		this.cpuDuration += cpuDuration;
	}

	/**
	 * @return the cpuAverage
	 */
	public double getCpuAverage() {
		return cpuAverage;
	}

	/**
	 * @param cpuAverage
	 *            the cpuAverage to set
	 */
	public void setCpuAverage(double cpuAverage) {
		this.cpuAverage = cpuAverage;
	}

	public long getExclusiveCount() {
		return exclusiveCount;
	}

	public void setExclusiveCount(long exclusiveCount) {
		this.exclusiveCount = exclusiveCount;
	}

	public void increaseExclusiveCount() {
		this.exclusiveCount++;
	}

	public double getExclusiveDuration() {
		return exclusiveDuration;
	}

	public void setExclusiveDuration(double exclusiveDuration) {
		this.exclusiveDuration = exclusiveDuration;
	}

	public void addExclusiveDuration(double exclusiveDuration) {
		this.exclusiveDuration += exclusiveDuration;
	}

	public double getExclusiveMax() {
		return exclusiveMax;
	}

	public void setExclusiveMax(double exclusiveMax) {
		this.exclusiveMax = exclusiveMax;
	}

	public double getExclusiveMin() {
		return exclusiveMin;
	}

	public void setExclusiveMin(double exclusiveMin) {
		this.exclusiveMin = exclusiveMin;
	}

	/**
	 * Returns the average exclusive time calculated as exclusive duration % count.
	 * 
	 * @return Average exclusive time.
	 */
	public double getExclusiveAverage() {
		return exclusiveDuration / exclusiveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(average);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (count ^ (count >>> 32));
		temp = Double.doubleToLongBits(cpuAverage);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cpuDuration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cpuMax);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cpuMin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(duration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (exclusiveCount ^ (exclusiveCount >>> 32));
		temp = Double.doubleToLongBits(exclusiveDuration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(exclusiveMax);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(exclusiveMin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(variance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
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
		TimerData other = (TimerData) obj;
		if (Double.doubleToLongBits(average) != Double.doubleToLongBits(other.average)) {
			return false;
		}
		if (count != other.count) {
			return false;
		}
		if (Double.doubleToLongBits(cpuAverage) != Double.doubleToLongBits(other.cpuAverage)) {
			return false;
		}
		if (Double.doubleToLongBits(cpuDuration) != Double.doubleToLongBits(other.cpuDuration)) {
			return false;
		}
		if (Double.doubleToLongBits(cpuMax) != Double.doubleToLongBits(other.cpuMax)) {
			return false;
		}
		if (Double.doubleToLongBits(cpuMin) != Double.doubleToLongBits(other.cpuMin)) {
			return false;
		}
		if (Double.doubleToLongBits(duration) != Double.doubleToLongBits(other.duration)) {
			return false;
		}
		if (exclusiveCount != other.exclusiveCount) {
			return false;
		}
		if (Double.doubleToLongBits(exclusiveDuration) != Double.doubleToLongBits(other.exclusiveDuration)) {
			return false;
		}
		if (Double.doubleToLongBits(exclusiveMax) != Double.doubleToLongBits(other.exclusiveMax)) {
			return false;
		}
		if (Double.doubleToLongBits(exclusiveMin) != Double.doubleToLongBits(other.exclusiveMin)) {
			return false;
		}
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max)) {
			return false;
		}
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min)) {
			return false;
		}
		if (Double.doubleToLongBits(variance) != Double.doubleToLongBits(other.variance)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData finalizeData() {
		average = duration / count;
		if (Double.MAX_VALUE != cpuMin) {
			cpuAverage = cpuDuration / count;
		} else {
			cpuAverage = -1.0d;
			cpuMin = -1.0d;
			cpuMax = -1.0d;
			cpuDuration = -1.0d;
		}
		if (Double.MAX_VALUE == exclusiveMin) {
			exclusiveDuration = -1.0d;
			exclusiveMin = -1.0d;
			exclusiveMax = -1.0d;
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size = super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 0, 0, 2, 12);
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public double getInvocationAffiliationPercentage() {
		return (double) getObjectsInInvocationsCount() / count;
	}

	/**
	 * Aggregates the values given in the supplied timer data parameter to the objects data.
	 * 
	 * @param timerData
	 *            Data to be aggregated into current object.
	 */
	public void aggregateTimerData(TimerData timerData) {
		this.setCount(this.getCount() + timerData.getCount());
		this.setDuration(this.getDuration() + timerData.getDuration());
		this.setAverage(this.getDuration() / this.getCount());
		this.setMax(Math.max(this.getMax(), timerData.getMax()));
		this.setMin(Math.min(this.getMin(), timerData.getMin()));

		if (-1 != timerData.getCpuDuration()) {
			this.setCpuDuration(this.getCpuDuration() + timerData.getCpuDuration());
			this.setCpuAverage(this.getCpuDuration() / this.getCount());
			this.setCpuMax(Math.max(this.getCpuMax(), timerData.getCpuMax()));
			this.setCpuMin(Math.min(this.getCpuMin(), timerData.getCpuMin()));
		}
		if (null != timerData.getInvocationParentsIdSet()) {
			Iterator it = timerData.getInvocationParentsIdSet().iterator();
			while (it.hasNext()) {
				this.addInvocationParentId((Long) it.next());
			}
		}
		if (-1 != timerData.getExclusiveDuration()) {
			this.addExclusiveDuration(timerData.getExclusiveDuration());
			this.setExclusiveCount(this.getExclusiveCount() + timerData.getExclusiveCount());
			this.setExclusiveMax(Math.max(this.getExclusiveMax(), timerData.getExclusiveMax()));
			this.setExclusiveMin(Math.min(this.getExclusiveMin(), timerData.getExclusiveMin()));
		}
	}

}
