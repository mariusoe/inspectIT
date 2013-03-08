package info.novatec.inspectit.cmr.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Class that connects the {@link MethodIdent} and {@link MethodSensorTypeIdent} and provides
 * additional intormation on the relationship.
 * 
 * @author Ivan Senic
 * 
 */
public class MethodIdentToSensorType implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3767712432753232084L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private Long id;

	/**
	 * {@link MethodIdent}.
	 */
	private MethodIdent methodIdent;

	/**
	 * {@link MethodSensorTypeIdent}.
	 */
	private MethodSensorTypeIdent methodSensorTypeIdent;

	/**
	 * Time-stamp represents last time the sensor on the method was registered.
	 */
	private Timestamp timestamp;

	/**
	 * No-arg constructor.
	 */
	public MethodIdentToSensorType() {
	}

	/**
	 * Constructor that allows setting all values.
	 * 
	 * @param methodIdent
	 *            {@link MethodIdent}.
	 * @param methodSensorTypeIdent
	 *            {@link MethodSensorTypeIdent}.
	 * @param timestamp
	 *            Time-stamp represents last time the sensor on the method was registered.
	 */
	public MethodIdentToSensorType(MethodIdent methodIdent, MethodSensorTypeIdent methodSensorTypeIdent, Timestamp timestamp) {
		this.methodIdent = methodIdent;
		this.methodSensorTypeIdent = methodSensorTypeIdent;
		this.timestamp = timestamp;
	}

	/**
	 * Returns if the {@link MethodIdentToSensorType} is active, meaning if the latest agent
	 * registration included this instrumentation.
	 * 
	 * @return True if the latest agent registration included the {@link MethodSensorTypeIdent}
	 *         instrumentation on {@link MethodIdent}.
	 */
	public boolean isActive() {
		return timestamp.after(methodIdent.getPlatformIdent().getTimeStamp());
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #methodIdent}.
	 * 
	 * @return {@link #methodIdent}
	 */
	public MethodIdent getMethodIdent() {
		return methodIdent;
	}

	/**
	 * Sets {@link #methodIdent}.
	 * 
	 * @param methodIdent
	 *            New value for {@link #methodIdent}
	 */
	public void setMethodIdent(MethodIdent methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * Gets {@link #methodSensorTypeIdent}.
	 * 
	 * @return {@link #methodSensorTypeIdent}
	 */
	public MethodSensorTypeIdent getMethodSensorTypeIdent() {
		return methodSensorTypeIdent;
	}

	/**
	 * Sets {@link #methodSensorTypeIdent}.
	 * 
	 * @param methodSensorTypeIdent
	 *            New value for {@link #methodSensorTypeIdent}
	 */
	public void setMethodSensorTypeIdent(MethodSensorTypeIdent methodSensorTypeIdent) {
		this.methodSensorTypeIdent = methodSensorTypeIdent;
	}

	/**
	 * Gets {@link #timestamp}.
	 * 
	 * @return {@link #timestamp}
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets {@link #timestamp}.
	 * 
	 * @param timestamp
	 *            New value for {@link #timestamp}
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((methodIdent == null) ? 0 : methodIdent.hashCode());
		result = prime * result + ((methodSensorTypeIdent == null) ? 0 : methodSensorTypeIdent.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
		MethodIdentToSensorType other = (MethodIdentToSensorType) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (methodIdent == null) {
			if (other.methodIdent != null) {
				return false;
			}
		} else if (!methodIdent.equals(other.methodIdent)) {
			return false;
		}
		if (methodSensorTypeIdent == null) {
			if (other.methodSensorTypeIdent != null) {
				return false;
			}
		} else if (!methodSensorTypeIdent.equals(other.methodSensorTypeIdent)) {
			return false;
		}
		if (timestamp == null) {
			if (other.timestamp != null) {
				return false;
			}
		} else if (!timestamp.equals(other.timestamp)) {
			return false;
		}
		return true;
	}

}
