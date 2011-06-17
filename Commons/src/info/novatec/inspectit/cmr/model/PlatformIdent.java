package info.novatec.inspectit.cmr.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Platform Ident class is used to store the unique information of an Agent,
 * so every Agent in different JVMs on the same target server receives a
 * different one.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlatformIdent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8501768676196666426L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private Long id;

	/**
	 * The timestamp which shows when this information was created on the CMR.
	 */
	private Timestamp timeStamp;

	/**
	 * The many-to-many association to the {@link SensorTypeIdent} objects.
	 */
	private Set sensorTypeIdents = new HashSet(0);

	/**
	 * The one-to-many association to the {@link MethodIdent} objects.
	 */
	private Set methodIdents = new HashSet(0);

	/**
	 * The list of ip's of the target system (including v4 and v6).
	 */
	private List definedIPs;

	/**
	 * The self-defined name of the inspectIT Agent.
	 */
	private String agentName = "Agent";
	
	/**
	 * the current version of the agent
	 */
	private String version = "n/a";

	public PlatformIdent() {
	}

	public Set getSensorTypeIdents() {
		return sensorTypeIdents;
	}

	public void setSensorTypeIdents(Set platformSensorTypeIdents) {
		this.sensorTypeIdents = platformSensorTypeIdents;
	}

	public Set getMethodIdents() {
		return methodIdents;
	}

	public void setMethodIdents(Set methodIdents) {
		this.methodIdents = methodIdents;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	public List getDefinedIPs() {
		return definedIPs;
	}

	public void setDefinedIPs(List definedIPs) {
		this.definedIPs = definedIPs;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentName == null) ? 0 : agentName.hashCode());
		result = prime * result + ((definedIPs == null) ? 0 : definedIPs.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		PlatformIdent other = (PlatformIdent) obj;
		if (agentName == null) {
			if (other.agentName != null) {
				return false;
			}
		} else if (!agentName.equals(other.agentName)) {
			return false;
		}
		if (definedIPs == null) {
			if (other.definedIPs != null) {
				return false;
			}
		} else if (!definedIPs.equals(other.definedIPs)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}
}
