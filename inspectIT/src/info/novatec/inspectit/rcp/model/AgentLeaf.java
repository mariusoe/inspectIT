package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;

import com.google.common.base.Objects;

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
	 * {@link AgentStatusData}.
	 */
	private AgentStatusData agentStatusData;

	/**
	 * Default constructor.
	 * 
	 * @param platformIdent
	 *            Agent to display in leaf.
	 * @param agentStatusData
	 *            {@link AgentStatusData}
	 */
	public AgentLeaf(PlatformIdent platformIdent, AgentStatusData agentStatusData) {
		this.platformIdent = platformIdent;
		this.agentStatusData = agentStatusData;
	}

	/**
	 * Gets {@link #platformIdent}.
	 * 
	 * @return {@link #platformIdent}
	 */
	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * Sets {@link #platformIdent}.
	 * 
	 * @param platformIdent
	 *            New value for {@link #platformIdent}
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #agentStatusData}.
	 * 
	 * @return {@link #agentStatusData}
	 */
	public AgentStatusData getAgentStatusData() {
		return agentStatusData;
	}

	/**
	 * Sets {@link #agentStatusData}.
	 * 
	 * @param agentStatusData
	 *            New value for {@link #agentStatusData}
	 */
	public void setAgentStatusData(AgentStatusData agentStatusData) {
		this.agentStatusData = agentStatusData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		AgentLeaf that = (AgentLeaf) object;
		return Objects.equal(this.platformIdent, that.platformIdent);
	}
}
