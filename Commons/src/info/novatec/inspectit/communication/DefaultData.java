package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.indexing.IIndexQuery;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The {@link DefaultData} class is the base class for all data and value objects. Data objects are
 * persisted on the CMR and can be requested from the interfaces. Value Objects on the other hand
 * are only used as a transmission container from the Agent(s) to the CMR.
 * <p>
 * Every value object implementation needs to override the {@link #finalizeData()} method to return
 * a data object which can be persisted.
 * <p>
 * Data objects are free to use the {@link #finalizeData()} method to generate some additional
 * values (like the average).
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class DefaultData implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5195625080367033147L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private long id;

	/**
	 * The unique identifier of the platform.
	 */
	private long platformIdent;

	/**
	 * The unique identifier of the sensor type.
	 */
	private long sensorTypeIdent;

	/**
	 * The timestamp which shows when this information was created on the Agent.
	 */
	private Timestamp timeStamp;

	/**
	 * Default no-args constructor.
	 */
	public DefaultData() {
	}

	/**
	 * Constructor which accepts three parameters to initialize itself.
	 * 
	 * @param timeStamp
	 *            The timestamp.
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param sensorTypeIdent
	 *            The unique identifier of the sensor type.
	 */
	public DefaultData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		this.timeStamp = timeStamp;
		this.platformIdent = platformIdent;
		this.sensorTypeIdent = sensorTypeIdent;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * This method has to be overridden by every implementation of a value object to return a
	 * {@link DefaultData} object which can be persisted.
	 * 
	 * @return Returns a {@link DefaultData} object which can be persisted.
	 */
	public DefaultData finalizeData() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (platformIdent ^ (platformIdent >>> 32));
		result = prime * result + (int) (sensorTypeIdent ^ (sensorTypeIdent >>> 32));
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DefaultData other = (DefaultData) obj;
		if (id != other.id) {
			return false;
		}
		if (platformIdent != other.platformIdent) {
			return false;
		}
		if (sensorTypeIdent != other.sensorTypeIdent) {
			return false;
		}
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the approximate size of the object in the memory in bytes.
	 * <p>
	 * This method needs to be overridden by all subclasses.
	 * 
	 * @param objectSizes
	 *            Appropriate instance of {@link IObjectSizes} depending on the VM architecture.
	 * @return Approximate object size in bytes.
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size = objectSizes.getSizeOfObject();
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 3, 0);
		size += objectSizes.getSizeOf(timeStamp);
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * Returns if the object is complied with passed {@link IIndexQuery}. This method will only
	 * return true if the object data correspond to the searching parameters set in
	 * {@link IIndexQuery}.
	 * 
	 * @param query
	 *            Query to be check against.
	 * @return True if the object is complied with query, otherwise false.
	 */
	public boolean isQueryComplied(IIndexQuery query) {
		if (query.getObjectClasses() != null && !query.getObjectClasses().contains(this.getClass())) {
			return false;
		}
		if (query.getMinId() > id) {
			return false;
		}
		if (query.getPlatformIdent() != 0 && query.getPlatformIdent() != platformIdent) {
			return false;
		}
		if (query.getSensorTypeIdent() != 0 && query.getSensorTypeIdent() != sensorTypeIdent) {
			return false;
		}
		if (!query.isInInterval(timeStamp)) {
			return false;
		}
		if (!query.areAllRestrictionsFulfilled(this)) {
			return false;
		}
		
		return true;
	}
}
