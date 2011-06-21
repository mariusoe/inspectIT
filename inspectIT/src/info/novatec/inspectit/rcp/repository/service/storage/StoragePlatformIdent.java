package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;

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

	public Set<SensorTypeIdent> getSensorTypeIdents() {
		return platformIdent.getSensorTypeIdents();
	}

	public void setSensorTypeIdents(Set<SensorTypeIdent> platformSensorTypeIdents) {
		platformIdent.setSensorTypeIdents(platformSensorTypeIdents);
	}

	public Set<MethodIdent> getMethodIdents() {
		return platformIdent.getMethodIdents();
	}

	public void setMethodIdents(Set<MethodIdent> methodIdents) {
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

	public List<String> getDefinedIPs() {
		return platformIdent.getDefinedIPs();
	}

	public void setDefinedIPs(List<String> definedIPs) {
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

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}
}