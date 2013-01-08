package info.novatec.inspectit.communication.data.ci;

import java.io.Serializable;

/**
 * The platform sensor definition data object is used to store the information about an platform
 * sensor definition.
 * 
 * @author Matthias Huber
 * 
 */
public class PlatformSensorDefinitionData implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -3051445682564607572L;

	/**
	 * The id of this instance.
	 */
	private long id;

	/**
	 * The many-to-one association to the {@link ProfileData} object.
	 */
	private ProfileData profileData;

	/**
	 * The fully qualified name of the platform sensor class.
	 */
	private String fullyQualifiedName;

	/**
	 * Indicates whether this definition is used for monitoring or not.
	 */
	private boolean activated;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (activated ? 1231 : 1237);
		result = prime * result + ((fullyQualifiedName == null) ? 0 : fullyQualifiedName.hashCode());
		result = prime * result + ((profileData == null) ? 0 : profileData.hashCode());
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
		PlatformSensorDefinitionData other = (PlatformSensorDefinitionData) obj;
		if (activated != other.activated) {
			return false;
		}
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null) {
				return false;
			}
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName)) {
			return false;
		}
		if (profileData == null) {
			if (other.profileData != null) {
				return false;
			}
		} else if (!profileData.equals(other.profileData)) {
			return false;
		}
		return true;
	}

}
