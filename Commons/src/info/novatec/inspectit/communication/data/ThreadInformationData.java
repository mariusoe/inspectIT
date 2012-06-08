package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

/**
 * This class provide dynamic informations about the threads running/started in the virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ThreadInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -4782628082344900101L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum number of live daemon threads.
	 */
	private int minDaemonThreadCount = Integer.MAX_VALUE;

	/**
	 * The maximum number of live daemon threads.
	 */
	private int maxDaemonThreadCount = 0;

	/**
	 * The total number of live daemon threads.
	 */
	private int totalDaemonThreadCount = 0;

	/**
	 * The minimum peak live thread count since the virtual machine has started.
	 */
	private int minPeakThreadCount = Integer.MAX_VALUE;

	/**
	 * The maximum peak live thread count since the virtual machine has started.
	 */
	private int maxPeakThreadCount = 0;

	/**
	 * The total peak live thread count since the virtual machine has started.
	 */
	private int totalPeakThreadCount = 0;

	/**
	 * The minimum number of live threads including both daemon and non-daemon threads.
	 */
	private int minThreadCount = Integer.MAX_VALUE;

	/**
	 * The maximum number of live threads including both daemon and non-daemon threads.
	 */
	private int maxThreadCount = 0;

	/**
	 * The total number of live threads including both daemon and non-daemon threads.
	 */
	private int totalThreadCount = 0;

	/**
	 * The minimum number of total threads created and also started since the virtual machine
	 * started.
	 */
	private long minTotalStartedThreadCount = Long.MAX_VALUE;

	/**
	 * The maximum number of total threads created and also started since the virtual machine
	 * started.
	 */
	private long maxTotalStartedThreadCount = 0;

	/**
	 * The total number of total threads created and also started since the virtual machine started.
	 */
	private long totalTotalStartedThreadCount = 0;

	/**
	 * Default no-args constructor.
	 */
	public ThreadInformationData() {
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
	public ThreadInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
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

	public void addPeakThreadCount(int peakThreadCount) {
		this.totalPeakThreadCount += peakThreadCount;
	}

	public void addDaemonThreadCount(int daemonThreadCount) {
		this.totalDaemonThreadCount += daemonThreadCount;
	}

	public void addThreadCount(int threadCount) {
		this.totalThreadCount += threadCount;
	}

	public int getMinDaemonThreadCount() {
		return minDaemonThreadCount;
	}

	public void setMinDaemonThreadCount(int minDaemonThreadCount) {
		this.minDaemonThreadCount = minDaemonThreadCount;
	}

	public int getMaxDaemonThreadCount() {
		return maxDaemonThreadCount;
	}

	public void setMaxDaemonThreadCount(int maxDaemonThreadCount) {
		this.maxDaemonThreadCount = maxDaemonThreadCount;
	}

	public int getTotalDaemonThreadCount() {
		return totalDaemonThreadCount;
	}

	public void setTotalDaemonThreadCount(int totalDaemonThreadCount) {
		this.totalDaemonThreadCount = totalDaemonThreadCount;
	}

	public int getMinPeakThreadCount() {
		return minPeakThreadCount;
	}

	public void setMinPeakThreadCount(int minPeakThreadCount) {
		this.minPeakThreadCount = minPeakThreadCount;
	}

	public int getMaxPeakThreadCount() {
		return maxPeakThreadCount;
	}

	public void setMaxPeakThreadCount(int maxPeakThreadCount) {
		this.maxPeakThreadCount = maxPeakThreadCount;
	}

	public int getTotalPeakThreadCount() {
		return totalPeakThreadCount;
	}

	public void setTotalPeakThreadCount(int totalPeakThreadCount) {
		this.totalPeakThreadCount = totalPeakThreadCount;
	}

	public int getMinThreadCount() {
		return minThreadCount;
	}

	public void setMinThreadCount(int minThreadCount) {
		this.minThreadCount = minThreadCount;
	}

	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	public int getTotalThreadCount() {
		return totalThreadCount;
	}

	public void setTotalThreadCount(int totalThreadCount) {
		this.totalThreadCount = totalThreadCount;
	}

	public void addTotalStartedThreadCount(long totalStartedThreadCount) {
		this.totalTotalStartedThreadCount += totalStartedThreadCount;
	}

	public long getMinTotalStartedThreadCount() {
		return minTotalStartedThreadCount;
	}

	public void setMinTotalStartedThreadCount(long minTotalStartedThreadCount) {
		this.minTotalStartedThreadCount = minTotalStartedThreadCount;
	}

	public long getMaxTotalStartedThreadCount() {
		return maxTotalStartedThreadCount;
	}

	public void setMaxTotalStartedThreadCount(long maxTotalStartedThreadCount) {
		this.maxTotalStartedThreadCount = maxTotalStartedThreadCount;
	}

	public long getTotalTotalStartedThreadCount() {
		return totalTotalStartedThreadCount;
	}

	public void setTotalTotalStartedThreadCount(long totalTotalStartedThreadCount) {
		this.totalTotalStartedThreadCount = totalTotalStartedThreadCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + maxDaemonThreadCount;
		result = prime * result + maxPeakThreadCount;
		result = prime * result + maxThreadCount;
		result = prime * result + (int) (maxTotalStartedThreadCount ^ (maxTotalStartedThreadCount >>> 32));
		result = prime * result + minDaemonThreadCount;
		result = prime * result + minPeakThreadCount;
		result = prime * result + minThreadCount;
		result = prime * result + (int) (minTotalStartedThreadCount ^ (minTotalStartedThreadCount >>> 32));
		result = prime * result + totalDaemonThreadCount;
		result = prime * result + totalPeakThreadCount;
		result = prime * result + totalThreadCount;
		result = prime * result + (int) (totalTotalStartedThreadCount ^ (totalTotalStartedThreadCount >>> 32));
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
		ThreadInformationData other = (ThreadInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxDaemonThreadCount != other.maxDaemonThreadCount) {
			return false;
		}
		if (maxPeakThreadCount != other.maxPeakThreadCount) {
			return false;
		}
		if (maxThreadCount != other.maxThreadCount) {
			return false;
		}
		if (maxTotalStartedThreadCount != other.maxTotalStartedThreadCount) {
			return false;
		}
		if (minDaemonThreadCount != other.minDaemonThreadCount) {
			return false;
		}
		if (minPeakThreadCount != other.minPeakThreadCount) {
			return false;
		}
		if (minThreadCount != other.minThreadCount) {
			return false;
		}
		if (minTotalStartedThreadCount != other.minTotalStartedThreadCount) {
			return false;
		}
		if (totalDaemonThreadCount != other.totalDaemonThreadCount) {
			return false;
		}
		if (totalPeakThreadCount != other.totalPeakThreadCount) {
			return false;
		}
		if (totalThreadCount != other.totalThreadCount) {
			return false;
		}
		if (totalTotalStartedThreadCount != other.totalTotalStartedThreadCount) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, false);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 10, 0, 3, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
