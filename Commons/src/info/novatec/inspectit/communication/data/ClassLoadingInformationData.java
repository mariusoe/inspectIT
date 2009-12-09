package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

/**
 * This class provide dynamic informations about the class loading system of the
 * virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */

public class ClassLoadingInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 3670151927025437963L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum number of loaded classes in the virtual machine.
	 */
	private int minLoadedClassCount = Integer.MAX_VALUE;

	/**
	 * The maximum number of loaded classes in the virtual machine.
	 */
	private int maxLoadedClassCount = 0;

	/**
	 * The total number of loaded classes in the virtual machine.
	 */
	private int totalLoadedClassCount = 0;

	/**
	 * The minimum number of total loaded classes since the virtual machine
	 * started.
	 */
	private long minTotalLoadedClassCount = Long.MAX_VALUE;

	/**
	 * The maximum number of total loaded classes since the virtual machine
	 * started.
	 */
	private long maxTotalLoadedClassCount = 0;

	/**
	 * The total number of total loaded classes since the virtual machine
	 * started.
	 */
	private long totalTotalLoadedClassCount = 0;

	/**
	 * The minimum number of unloaded classes since the virtual machine started.
	 */
	private long minUnloadedClassCount = Long.MAX_VALUE;

	/**
	 * The maximum number of unloaded classes since the virtual machine started.
	 */
	private long maxUnloadedClassCount = 0;

	/**
	 * The total number of unloaded classes since the virtual machine started.
	 */
	private long totalUnloadedClassCount = 0;

	/**
	 * Default no-args constructor.
	 */
	public ClassLoadingInformationData() {
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
	public ClassLoadingInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
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

	public void addLoadedClassCount(int loadedClassCount) {
		this.totalLoadedClassCount += loadedClassCount;
	}

	public int getMinLoadedClassCount() {
		return minLoadedClassCount;
	}

	public void setMinLoadedClassCount(int minLoadedClassCount) {
		this.minLoadedClassCount = minLoadedClassCount;
	}

	public int getMaxLoadedClassCount() {
		return maxLoadedClassCount;
	}

	public void setMaxLoadedClassCount(int maxLoadedClassCount) {
		this.maxLoadedClassCount = maxLoadedClassCount;
	}

	public int getTotalLoadedClassCount() {
		return totalLoadedClassCount;
	}

	public void setTotalLoadedClassCount(int totalLoadedClassCount) {
		this.totalLoadedClassCount = totalLoadedClassCount;
	}

	public void addTotalLoadedClassCount(long totalLoadedClassCount) {
		this.totalTotalLoadedClassCount += totalLoadedClassCount;
	}

	public long getMinTotalLoadedClassCount() {
		return minTotalLoadedClassCount;
	}

	public void setMinTotalLoadedClassCount(long minTotalLoadedClassCount) {
		this.minTotalLoadedClassCount = minTotalLoadedClassCount;
	}

	public long getMaxTotalLoadedClassCount() {
		return maxTotalLoadedClassCount;
	}

	public void setMaxTotalLoadedClassCount(long maxTotalLoadedClassCount) {
		this.maxTotalLoadedClassCount = maxTotalLoadedClassCount;
	}

	public long getTotalTotalLoadedClassCount() {
		return totalTotalLoadedClassCount;
	}

	public void setTotalTotalLoadedClassCount(long totalTotalLoadedClassCount) {
		this.totalTotalLoadedClassCount = totalTotalLoadedClassCount;
	}

	public void addUnloadedClassCount(long unloadedClassCount) {
		this.totalUnloadedClassCount += unloadedClassCount;
	}

	public long getMinUnloadedClassCount() {
		return minUnloadedClassCount;
	}

	public void setMinUnloadedClassCount(long minUnloadedClassCount) {
		this.minUnloadedClassCount = minUnloadedClassCount;
	}

	public long getMaxUnloadedClassCount() {
		return maxUnloadedClassCount;
	}

	public void setMaxUnloadedClassCount(long maxUnloadedClassCount) {
		this.maxUnloadedClassCount = maxUnloadedClassCount;
	}

	public long getTotalUnloadedClassCount() {
		return totalUnloadedClassCount;
	}

	public void setTotalUnloadedClassCount(long totalUnloadedClassCount) {
		this.totalUnloadedClassCount = totalUnloadedClassCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + maxLoadedClassCount;
		result = prime * result + (int) (maxTotalLoadedClassCount ^ (maxTotalLoadedClassCount >>> 32));
		result = prime * result + (int) (maxUnloadedClassCount ^ (maxUnloadedClassCount >>> 32));
		result = prime * result + minLoadedClassCount;
		result = prime * result + (int) (minTotalLoadedClassCount ^ (minTotalLoadedClassCount >>> 32));
		result = prime * result + (int) (minUnloadedClassCount ^ (minUnloadedClassCount >>> 32));
		result = prime * result + totalLoadedClassCount;
		result = prime * result + (int) (totalTotalLoadedClassCount ^ (totalTotalLoadedClassCount >>> 32));
		result = prime * result + (int) (totalUnloadedClassCount ^ (totalUnloadedClassCount >>> 32));
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
		ClassLoadingInformationData other = (ClassLoadingInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxLoadedClassCount != other.maxLoadedClassCount) {
			return false;
		}
		if (maxTotalLoadedClassCount != other.maxTotalLoadedClassCount) {
			return false;
		}
		if (maxUnloadedClassCount != other.maxUnloadedClassCount) {
			return false;
		}
		if (minLoadedClassCount != other.minLoadedClassCount) {
			return false;
		}
		if (minTotalLoadedClassCount != other.minTotalLoadedClassCount) {
			return false;
		}
		if (minUnloadedClassCount != other.minUnloadedClassCount) {
			return false;
		}
		if (totalLoadedClassCount != other.totalLoadedClassCount) {
			return false;
		}
		if (totalTotalLoadedClassCount != other.totalTotalLoadedClassCount) {
			return false;
		}
		if (totalUnloadedClassCount != other.totalUnloadedClassCount) {
			return false;
		}
		return true;
	}

}
