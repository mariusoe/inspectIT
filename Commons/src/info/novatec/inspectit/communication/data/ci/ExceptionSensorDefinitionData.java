package info.novatec.inspectit.communication.data.ci;

import java.io.Serializable;

/**
 * The exception sensor definition data object is used to store the information
 * about an exception sensor definition.
 * 
 * @author Matthias Huber
 * 
 */
public class ExceptionSensorDefinitionData implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2639827368526192185L;

	/**
	 * This constant defines that no sensor option is set.
	 */
	public static final int NO_SENSOR_OPTION = 0;

	/**
	 * This constant defines that the sensor option 'superclass' is set.
	 */
	public static final int SUPERCLASS = 1;

	/**
	 * This constant defines that the sensor option 'interface' is set.
	 */
	public static final int INTERFACE = 2;

	/**
	 * The id of this instance.
	 */
	private long id;

	/**
	 * The many-to-one association to the {@link ProfileData} object.
	 */
	private ProfileData profileData;

	/**
	 * The description of the method sensor definition.
	 */
	private String description;

	/**
	 * The fully qualified name of the exception class which will be monitored.
	 */
	private String fullyQualifiedName;

	/**
	 * Indicates whether this definition is used for monitoring or not.
	 */
	private boolean activated = true;

	/**
	 * Indicates which additional sensor option is used for this definition.
	 * 
	 * 0: no sensor option is set
	 * 1: the flag 'superclass' is set
	 * 2: the flag 'interface' is set
	 */
	private int sensorOption = NO_SENSOR_OPTION;

	/**
	 * Default no-args constructor.
	 */
	public ExceptionSensorDefinitionData() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ProfileData getProfileData() {
		return profileData;
	}

	public void setProfileData(ProfileData profileData) {
		this.profileData = profileData;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public int getSensorOption() {
		return sensorOption;
	}

	public void setSensorOption(int sensorOption) {
		this.sensorOption = sensorOption;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (activated ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((fullyQualifiedName == null) ? 0 : fullyQualifiedName.hashCode());
		result = prime * result + ((profileData == null) ? 0 : profileData.hashCode());
		result = prime * result + sensorOption;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExceptionSensorDefinitionData other = (ExceptionSensorDefinitionData) obj;
		if (activated != other.activated)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null)
				return false;
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
			return false;
		if (profileData == null) {
			if (other.profileData != null)
				return false;
		} else if (!profileData.equals(other.profileData))
			return false;
		if (sensorOption != other.sensorOption)
			return false;
		return true;
	}

}
