package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

/**
 * This class provide dynamic informations about the runtime system of the virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class RuntimeInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -6969524429547729867L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum uptime of the virtual machine in milliseconds.
	 */
	private long minUptime = Long.MAX_VALUE;

	/**
	 * The maximum uptime of the virtual machine in milliseconds.
	 */
	private long maxUptime = 0;

	/**
	 * The total uptime of the virtual machine in milliseconds.
	 */
	private long totalUptime = 0;

	/**
	 * Default no-args constructor.
	 */
	public RuntimeInformationData() {
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
	public RuntimeInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * increases the count by 1.
	 */
	public void incrementCount() {
		this.count++;
	}

	/**
	 * increases the uptime by the given value.
	 * 
	 * @param uptime
	 *            the value to add to the uptime.
	 */
	public void addUptime(long uptime) {
		this.totalUptime += uptime;
	}

	public long getMinUptime() {
		return minUptime;
	}

	public void setMinUptime(long minUptime) {
		this.minUptime = minUptime;
	}

	public long getMaxUptime() {
		return maxUptime;
	}

	public void setMaxUptime(long maxUptime) {
		this.maxUptime = maxUptime;
	}

	public long getTotalUptime() {
		return totalUptime;
	}

	public void setTotalUptime(long totalUptime) {
		this.totalUptime = totalUptime;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + (int) (maxUptime ^ (maxUptime >>> 32));
		result = prime * result + (int) (minUptime ^ (minUptime >>> 32));
		result = prime * result + (int) (totalUptime ^ (totalUptime >>> 32));
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
		RuntimeInformationData other = (RuntimeInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxUptime != other.maxUptime) {
			return false;
		}
		if (minUptime != other.minUptime) {
			return false;
		}
		if (totalUptime != other.totalUptime) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, false);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 3, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
