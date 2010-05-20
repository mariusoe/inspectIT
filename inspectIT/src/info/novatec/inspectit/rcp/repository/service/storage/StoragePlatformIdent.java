package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class StoragePlatformIdent extends PlatformIdent {

	private PlatformIdent platformIdent;

	private String path;

	private String folderName;

	public StoragePlatformIdent(PlatformIdent platformIdent, String path, String folderName) {
		this.platformIdent = platformIdent;
		this.path = path;
		this.folderName = folderName;
	}

	public Set getSensorTypeIdents() {
		return platformIdent.getSensorTypeIdents();
	}

	public void setSensorTypeIdents(Set platformSensorTypeIdents) {
		platformIdent.setSensorTypeIdents(platformSensorTypeIdents);
	}

	public Set getMethodIdents() {
		return platformIdent.getMethodIdents();
	}

	public void setMethodIdents(Set methodIdents) {
		platformIdent.setMethodIdents(methodIdents);
	}

	public int hashCode() {
		return platformIdent.hashCode();
	}

	public Long getId() {
		return platformIdent.getId();
	}

	public void setId(Long id) {
		platformIdent.setId(id);
	}

	public Timestamp getTimeStamp() {
		return platformIdent.getTimeStamp();
	}

	public void setTimeStamp(Timestamp timeStamp) {
		platformIdent.setTimeStamp(timeStamp);
	}

	public List getDefinedIPs() {
		return platformIdent.getDefinedIPs();
	}

	public void setDefinedIPs(List definedIPs) {
		platformIdent.setDefinedIPs(definedIPs);
	}

	public String getAgentName() {
		return platformIdent.getAgentName();
	}

	public void setAgentName(String agentName) {
		platformIdent.setAgentName(agentName);
	}

	public String getVersion() {
		return platformIdent.getVersion();
	}

	public void setVersion(String version) {
		platformIdent.setVersion(version);
	}

	public boolean equals(Object obj) {
		return platformIdent.equals(obj);
	}

	public String toString() {
		return platformIdent.toString();
	}

	public String getPath() {
		return path;
	}

	public String getFolderName() {
		return folderName;
	}
}