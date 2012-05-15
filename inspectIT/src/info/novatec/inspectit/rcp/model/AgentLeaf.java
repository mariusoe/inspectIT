package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;

/**
 * Agent leaf for the tree in the Repository Manager.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentLeaf extends Leaf {

	/**
	 * Agent.
	 */
	private PlatformIdent platformIdent;

	/**
	 * Default constructor.
	 * 
	 * @param platformIdent
	 *            Agent to display in leaf.
	 */
	public AgentLeaf(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
		this.setName(platformIdent.getAgentName() + " [" + platformIdent.getVersion() + "]");
		this.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_AGENT));
		this.setTooltip("Double click to explore Agent in the Data Explorer");
	}

	/**
	 * @return the platformIdent
	 */
	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((platformIdent == null) ? 0 : platformIdent.hashCode());
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
		AgentLeaf other = (AgentLeaf) obj;
		if (platformIdent == null) {
			if (other.platformIdent != null) {
				return false;
			}
		} else if (!platformIdent.equals(other.platformIdent)) {
			return false;
		}
		return true;
	}

}
