package info.novatec.inspectit.cmr.cache.indexing;

import java.sql.Timestamp;

/**
 * {@link IndexQuery} represent an object that is used in querying the tree structure of the buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class IndexQuery {
	
	/**
	 * Minimum id that returned objects should have. 
	 */
	private long minId;

	/**
	 * Platform id.
	 */
	private long platformIdent;

	/**
	 * Sensor type id.
	 */
	private long sensorTypeIdent;

	/**
	 * Method id.
	 */
	private long methodIdent;

	/**
	 * Object class type.
	 */
	private Class objectClass;

	/**
	 * From date.
	 */
	private Timestamp fromDate;

	/**
	 * Till date.
	 */
	private Timestamp toDate;

	public long getMinId() {
		return minId;
	}

	public void setMinId(long minId) {
		this.minId = minId;
	}

	public long getPlatformIdent() {
		return platformIdent;
	}

	public void setPlatformIdent(long platformIdent) {
		this.platformIdent = platformIdent;
	}

	public long getSensorTypeIdent() {
		return sensorTypeIdent;
	}

	public void setSensorTypeIdent(long sensorTypeIdent) {
		this.sensorTypeIdent = sensorTypeIdent;
	}

	public long getMethodIdent() {
		return methodIdent;
	}

	public void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	public Class getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(Class objectClass) {
		this.objectClass = objectClass;
	}

	public Timestamp getFromDate() {
		return fromDate;
	}

	public void setFromDate(Timestamp fromDate) {
		this.fromDate = fromDate;
	}

	public Timestamp getToDate() {
		return toDate;
	}

	public void setToDate(Timestamp toDate) {
		this.toDate = toDate;
	}

	/**
	 * Returns if the interval is set for current {@link IndexQuery} object. The method will return
	 * true only when both {@link #fromDate} and {@link #toDate} time stamps are not null, and when
	 * {@link #toDate} is after {@link #fromDate}.
	 * 
	 * @return
	 */
	public boolean isIntervalSet() {
		return ((null != fromDate) && (null != toDate) && fromDate.before(toDate));
	}

	/**
	 * Returns if the given time stamp is belonging to the interval set in the {@link IndexQuery}
	 * object. This method will return true only when interval is set for current object (see
	 * {@link #isInIntervalSet()}) and given time stamp object is in interval ({@link #fromDate} >=
	 * timestamp >= {@link #toDate}).
	 * 
	 * @param timestamp
	 * @return
	 */
	public boolean isInInterval(Timestamp timestamp) {
		if (isIntervalSet()) {
			if (null == timestamp) {
				return false;
			} else {
				if (fromDate.compareTo(timestamp) > 0) {
					return false;
				}
				if (toDate.compareTo(timestamp) < 0) {
					return false;
				}
				return true;
			}
		} else {
			return true;
		}
	}

}
