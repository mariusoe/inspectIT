package info.novatec.inspectit.communication.data.ci;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The environment data object is used to store all information relating to an
 * 'Environment' defined via the configuration interface.
 * 
 * @author Matthias Huber
 * 
 */
public class EnvironmentData implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -1993990791423286112L;

	/**
	 * The id of this instance.
	 */
	private long id;

	/**
	 * The environment name.
	 */
	private String name;

	/**
	 * The environment description.
	 */
	private String description;

	/**
	 * The used buffer strategy.
	 */
	private String bufferStrategy;

	/**
	 * The used send strategy.
	 */
	private String sendStrategy;

	/**
	 * The one-to-many association to the {@link ProfileData} objects.
	 */
	private Set profiles = new HashSet(0);

	/**
	 * The one-to-many association to the {@link SensorTypeData} objects.
	 */
	private Set sensorTypes = new HashSet(0);

	/**
	 * Default no-args constructor.
	 */
	public EnvironmentData() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBufferStrategy() {
		return bufferStrategy;
	}

	public void setBufferStrategy(String bufferStrategy) {
		this.bufferStrategy = bufferStrategy;
	}

	public String getSendStrategy() {
		return sendStrategy;
	}

	public void setSendStrategy(String sendStrategy) {
		this.sendStrategy = sendStrategy;
	}

	public Set getProfiles() {
		return profiles;
	}

	public void setProfiles(Set profiles) {
		this.profiles = profiles;
	}

	public Set getSensorTypes() {
		return sensorTypes;
	}

	public void setSensorTypes(Set sensorTypes) {
		this.sensorTypes = sensorTypes;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bufferStrategy == null) ? 0 : bufferStrategy.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sendStrategy == null) ? 0 : sendStrategy.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnvironmentData other = (EnvironmentData) obj;
		if (bufferStrategy == null) {
			if (other.bufferStrategy != null)
				return false;
		} else if (!bufferStrategy.equals(other.bufferStrategy))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sendStrategy == null) {
			if (other.sendStrategy != null)
				return false;
		} else if (!sendStrategy.equals(other.sendStrategy))
			return false;
		return true;
	}

}
