package info.novatec.novaspy.cmr.util;

import java.util.List;

/**
 * This is a data helper class to uniquely identify a agent.
 * 
 * @author Dirk Maucher
 * 
 */
public class LicenseUtilData {
	/**
	 * The agents name.
	 */
	private String agentName;

	/**
	 * A list with defined ips of the agent.
	 */
	private List<String> definedIPs;

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public void setDefinedIPs(List<String> definedIPs) {
		this.definedIPs = definedIPs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentName == null) ? 0 : agentName.hashCode());
		result = prime * result + ((definedIPs == null) ? 0 : definedIPs.hashCode());
		return result;
	}

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
		LicenseUtilData other = (LicenseUtilData) obj;
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
		return true;
	}

}
