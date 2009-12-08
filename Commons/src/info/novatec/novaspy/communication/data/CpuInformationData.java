package info.novatec.novaspy.communication.data;

import info.novatec.novaspy.communication.SystemSensorData;

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
	 * The minimum cpu time used by the process on which the virtual machine is
	 * running.
	 */
	private long minProcessCpuTime = Long.MAX_VALUE;

	/**
	 * The maximum cpu time used by the process on which the virtual machine is
	 * running.
	 */
	private long maxProcessCpuTime = 0;

	/**
	 * The total cpu time used by the process on which the virtual machine is
	 * running.
	 */
	private long totalProcessCpuTime = 0;

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

	public void addProcessCpuTime(long processCpuTime) {
		this.totalProcessCpuTime += processCpuTime;
	}

	public long getMinProcessCpuTime() {
		return minProcessCpuTime;
	}

	public void setMinProcessCpuTime(long minProcessCpuTime) {
		this.minProcessCpuTime = minProcessCpuTime;
	}

	public long getMaxProcessCpuTime() {
		return maxProcessCpuTime;
	}

	public void setMaxProcessCpuTime(long maxProcessCpuTime) {
		this.maxProcessCpuTime = maxProcessCpuTime;
	}

	public long getTotalProcessCpuTime() {
		return totalProcessCpuTime;
	}

	public void setTotalProcessCpuTime(long totalProcessCpuTime) {
		this.totalProcessCpuTime = totalProcessCpuTime;
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

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + Float.floatToIntBits(maxCpuUsage);
		result = prime * result + (int) (maxProcessCpuTime ^ (maxProcessCpuTime >>> 32));
		result = prime * result + Float.floatToIntBits(minCpuUsage);
		result = prime * result + (int) (minProcessCpuTime ^ (minProcessCpuTime >>> 32));
		result = prime * result + Float.floatToIntBits(totalCpuUsage);
		result = prime * result + (int) (totalProcessCpuTime ^ (totalProcessCpuTime >>> 32));
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
		CpuInformationData other = (CpuInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (Float.floatToIntBits(maxCpuUsage) != Float.floatToIntBits(other.maxCpuUsage)) {
			return false;
		}
		if (maxProcessCpuTime != other.maxProcessCpuTime) {
			return false;
		}
		if (Float.floatToIntBits(minCpuUsage) != Float.floatToIntBits(other.minCpuUsage)) {
			return false;
		}
		if (minProcessCpuTime != other.minProcessCpuTime) {
			return false;
		}
		if (Float.floatToIntBits(totalCpuUsage) != Float.floatToIntBits(other.totalCpuUsage)) {
			return false;
		}
		if (totalProcessCpuTime != other.totalProcessCpuTime) {
			return false;
		}
		return true;
	}

}
