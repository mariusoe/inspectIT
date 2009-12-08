package info.novatec.novaspy.cmr.model;

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
	 * The self-defined name of the NovaSpy Agent.
	 */
	private String agentName = "Agent";

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

}
