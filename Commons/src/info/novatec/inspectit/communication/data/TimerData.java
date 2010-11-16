package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;

import java.sql.Timestamp;
import java.util.List;

/**
 * The timer data class stores information about the execution time of a java
 * method.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimerData extends MethodSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 7001746257144702456L;

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
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size =  super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 0, 0, 1, 9);
		return objectSizes.alignTo8Bytes(size);
	}

}
