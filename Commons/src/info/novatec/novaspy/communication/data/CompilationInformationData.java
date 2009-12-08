package info.novatec.novaspy.communication.data;

import info.novatec.novaspy.communication.SystemSensorData;

import java.sql.Timestamp;

/**
 * This class provide dynamic informations about the compilation system of the
 * virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CompilationInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -7529958619378902534L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum approximate accumulated elapsed time (milliseconds) spent in
	 * compilation.
	 */
	private long minTotalCompilationTime = Long.MAX_VALUE;

	/**
	 * The maximum approximate accumulated elapsed time (milliseconds) spent in
	 * compilation.
	 */
	private long maxTotalCompilationTime = 0;

	/**
	 * The total approximate accumulated elapsed time (milliseconds) spent in
	 * compilation.
	 */
	private long totalTotalCompilationTime = 0;

	/**
	 * Default no-args constructor.
	 */
	public CompilationInformationData() {
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
	public CompilationInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
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

	public void addTotalCompilationTime(long totalCompilationTime) {
		this.totalTotalCompilationTime += totalCompilationTime;
	}

	public long getMinTotalCompilationTime() {
		return minTotalCompilationTime;
	}

	public void setMinTotalCompilationTime(long minTotalCompilationTime) {
		this.minTotalCompilationTime = minTotalCompilationTime;
	}

	public long getMaxTotalCompilationTime() {
		return maxTotalCompilationTime;
	}

	public void setMaxTotalCompilationTime(long maxTotalCompilationTime) {
		this.maxTotalCompilationTime = maxTotalCompilationTime;
	}

	public long getTotalTotalCompilationTime() {
		return totalTotalCompilationTime;
	}

	public void setTotalTotalCompilationTime(long totalTotalCompilationTime) {
		this.totalTotalCompilationTime = totalTotalCompilationTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + (int) (maxTotalCompilationTime ^ (maxTotalCompilationTime >>> 32));
		result = prime * result + (int) (minTotalCompilationTime ^ (minTotalCompilationTime >>> 32));
		result = prime * result + (int) (totalTotalCompilationTime ^ (totalTotalCompilationTime >>> 32));
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
		CompilationInformationData other = (CompilationInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxTotalCompilationTime != other.maxTotalCompilationTime) {
			return false;
		}
		if (minTotalCompilationTime != other.minTotalCompilationTime) {
			return false;
		}
		if (totalTotalCompilationTime != other.totalTotalCompilationTime) {
			return false;
		}
		return true;
	}

}
