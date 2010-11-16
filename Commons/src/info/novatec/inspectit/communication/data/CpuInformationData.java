package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

/**
 * This class provide dynamic informations about the underlying operating system
 * such as cpu usage and cpu time.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CpuInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 3575761562499283807L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The cpu time used by the process on which the virtual machine is running.
	 */
	private long processCpuTime = 0;

	/**
	 * The minimum cpu usage in percent.
	 */
	private float minCpuUsage = Float.MAX_VALUE;

	/**
	 * The maximum cpu usage in percent.
	 */
	private float maxCpuUsage = 0;

	/**
	 * The total cpu usage in percent.
	 */
	private float totalCpuUsage = 0;

	/**
	 * Default no-args constructor.
	 */
	public CpuInformationData() {
	}

	/**
	 * The constructor which needs three parameters.
	 * 
	 * @param timeStamp
	 *            The Timestamp.
	 * @param platformIdent
	 *            The PlatformIdent.
	 * @param sensorTypeIdent
	 *            The SensorTypeIdent.
	 */
	public CpuInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void incrementCount() {
		this.count++;
	}

	public void updateProcessCpuTime(long actualProcessCpuTime) {
		if (actualProcessCpuTime > processCpuTime) {
			processCpuTime = actualProcessCpuTime;
		}
	}

	public long getProcessCpuTime() {
		return processCpuTime;
	}

	public void setProcessCpuTime(long processCpuTime) {
		this.processCpuTime = processCpuTime;
	}

	public void addCpuUsage(float cpuUsage) {
		this.totalCpuUsage += cpuUsage;
	}

	public float getMinCpuUsage() {
		return minCpuUsage;
	}

	public void setMinCpuUsage(float minCpuUsage) {
		this.minCpuUsage = minCpuUsage;
	}

	public float getMaxCpuUsage() {
		return maxCpuUsage;
	}

	public void setMaxCpuUsage(float maxCpuUsage) {
		this.maxCpuUsage = maxCpuUsage;
	}

	public float getTotalCpuUsage() {
		return totalCpuUsage;
	}

	public void setTotalCpuUsage(float totalCpuUsage) {
		this.totalCpuUsage = totalCpuUsage;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + Float.floatToIntBits(maxCpuUsage);
		result = prime * result + Float.floatToIntBits(minCpuUsage);
		result = prime * result + (int) (processCpuTime ^ (processCpuTime >>> 32));
		result = prime * result + Float.floatToIntBits(totalCpuUsage);
		return result;
	}

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
		CpuInformationData other = (CpuInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (Float.floatToIntBits(maxCpuUsage) != Float.floatToIntBits(other.maxCpuUsage)) {
			return false;
		}
		if (Float.floatToIntBits(minCpuUsage) != Float.floatToIntBits(other.minCpuUsage)) {
			return false;
		}
		if (processCpuTime != other.processCpuTime) {
			return false;
		}
		if (Float.floatToIntBits(totalCpuUsage) != Float.floatToIntBits(other.totalCpuUsage)) {
			return false;
		}
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size =  super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 1, 3, 1, 0);
		return objectSizes.alignTo8Bytes(size);
	}
}
