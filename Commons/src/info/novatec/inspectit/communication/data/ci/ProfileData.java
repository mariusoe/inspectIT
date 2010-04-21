package info.novatec.inspectit.communication.data.ci;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The profile data object is used to store the information about a profile,
 * e.g. all sensor definitions defined within a profile.
 * 
 * @author Matthias Huber
 * 
 */
public class ProfileData implements Serializable {

	/**
	 * The serial version UIDs.
	 */
	private static final long serialVersionUID = 191946681243766599L;

	/**
	 * The id of this instance.
	 */
	private long id;

	/**
	 * The profile name
	 */
	private String name;

	/**
	 * The profile description.
	 */
	private String description;

	/**
	 * The many-to-one association to the {@link EnvironmentData} object.
	 */
	private EnvironmentData environmentData;

	/**
	 * The one-to-many association to the {@link MethodSensorDefinitionData}
	 * objects.
	 */
	private Set methodSensorDefinitions = new HashSet(0);

	/**
	 * The one-to-many association to the {@link PlatformSensorDefinitionData}
	 * objects.
	 */
	private Set platformSensorDefinitions = new HashSet(0);

	/**
	 * The one-to-many association to the {@link ExceptionSensorDefinitionData}
	 * objects.
	 */
	private Set exceptionSensorDefinitions = new HashSet(0);

	/**
	 * This boolean flag is used only by the client side to distinguish if a
	 * profile is completely loaded or not (all sensor definition loaded).
	 * Therefore this value is not stored into the database.
	 */
	private boolean isInitialized = false;

	/**
	 * Default no-args constructor.
	 */
	public ProfileData() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public EnvironmentData getEnvironmentData() {
		return environmentData;
	}

	public void setEnvironmentData(EnvironmentData environmentData) {
		this.environmentData = environmentData;
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

	public Set getMethodSensorDefinitions() {
		return methodSensorDefinitions;
	}

	public void setMethodSensorDefinitions(Set methodSensorDefinitions) {
		this.methodSensorDefinitions = methodSensorDefinitions;
	}

	public Set getPlatformSensorDefinitions() {
		return platformSensorDefinitions;
	}

	public void setPlatformSensorDefinitions(Set platformSensorDefinitions) {
		this.platformSensorDefinitions = platformSensorDefinitions;
	}

	public Set getExceptionSensorDefinitions() {
		return exceptionSensorDefinitions;
	}

	public void setExceptionSensorDefinitions(Set exceptionSensorDefinitions) {
		this.exceptionSensorDefinitions = exceptionSensorDefinitions;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((environmentData == null) ? 0 : environmentData.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProfileData other = (ProfileData) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (environmentData == null) {
			if (other.environmentData != null)
				return false;
		} else if (!environmentData.equals(other.environmentData))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
