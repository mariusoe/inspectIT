package info.novatec.inspectit.communication.data.cmr;

import java.io.Serializable;

/**
 * Class that holds agent status data.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentStatusData implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -822865263748943014L;

	/**
	 * Enumeration that defines the connectsion status.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum AgentConnection {

		/**
		 * Agent was never connected.
		 */
		NEVER_CONNECTED,

		/**
		 * Agent is connected at the moment.
		 */
		CONNECTED,

		/**
		 * Agent is disconnected.
		 */
		DISCONNECTED;

	}

	/**
	 * {@link AgentConnection}.
	 */
	private AgentConnection agentConnection;

	/**
	 * Time in milliseconds when last data was sent.
	 */
	private long lastDataSendTimestamp;

	/**
	 * Current CMR time.
	 */
	private long serverTimestamp;

	/**
	 * No-arg constructor. Sets {@link #agentConnection} to {@link AgentConnection#NEVER_CONNECTED}.
	 */
	public AgentStatusData() {
		this.agentConnection = AgentConnection.NEVER_CONNECTED;
	}

	/**
	 * Default constructor.
	 * 
	 * @param agentConnection
	 *            {@link AgentConnection}.
	 */
	public AgentStatusData(AgentConnection agentConnection) {
		this.agentConnection = agentConnection;
	}

	/**
	 * Returns the information about how much milliseconds passed since last data sending for the
	 * agent.
	 * <p>
	 * This information can be obtained only if valid information is stored in
	 * {@link #lastDataSendTimestamp} and {@link #serverTimestamp}.
	 * 
	 * @return Milliseconds or <code>null</code>.
	 */
	public Long getMillisSinceLastData() {
		if (0 < lastDataSendTimestamp && lastDataSendTimestamp < serverTimestamp) {
			return serverTimestamp - lastDataSendTimestamp;
		} else {
			return null;
		}
	}

	/**
	 * Gets {@link #agentConnection}.
	 * 
	 * @return {@link #agentConnection}
	 */
	public AgentConnection getAgentConnection() {
		return agentConnection;
	}

	/**
	 * Sets {@link #agentConnection}.
	 * 
	 * @param agentConnection
	 *            New value for {@link #agentConnection}
	 */
	public void setAgentConnection(AgentConnection agentConnection) {
		this.agentConnection = agentConnection;
	}

	/**
	 * Sets {@link #lastDataSendTimestamp}.
	 * 
	 * @param lastDataSendTimestamp
	 *            New value for {@link #lastDataSendTimestamp}
	 */
	public void setLastDataSendTimestamp(long lastDataSendTimestamp) {
		this.lastDataSendTimestamp = lastDataSendTimestamp;
	}

	/**
	 * Sets {@link #serverTimestamp}.
	 * 
	 * @param serverTimestamp
	 *            New value for {@link #serverTimestamp}
	 */
	public void setServerTimestamp(long serverTimestamp) {
		this.serverTimestamp = serverTimestamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentConnection == null) ? 0 : agentConnection.hashCode());
		result = prime * result + (int) (lastDataSendTimestamp ^ (lastDataSendTimestamp >>> 32));
		result = prime * result + (int) (serverTimestamp ^ (serverTimestamp >>> 32));
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
		AgentStatusData other = (AgentStatusData) obj;
		if (agentConnection != other.agentConnection) {
			return false;
		}
		if (lastDataSendTimestamp != other.lastDataSendTimestamp) {
			return false;
		}
		if (serverTimestamp != other.serverTimestamp) {
			return false;
		}
		return true;
	}

}
