package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Bean that saves the time when the last time platform ident received the data.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class AgentStatusDataProvider {

	/**
	 * Map that holds IDs of the platform idents and {@link AgentStatusData} objects.
	 */
	private Map<Long, AgentStatusData> agentStatusDataMap = new ConcurrentHashMap<Long, AgentStatusData>(8, 0.75f, 1);

	/**
	 * Registers that the agent was connected.
	 * 
	 * @param platformIdent
	 *            ID of the platform ident.
	 */
	public synchronized void registerConnected(long platformIdent) {
		AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent);
		if (null != agentStatusData) {
			agentStatusData.setAgentConnection(AgentConnection.CONNECTED);
		} else {
			agentStatusDataMap.put(platformIdent, new AgentStatusData(AgentConnection.CONNECTED));
		}
	}

	/**
	 * Registers the time when last data was received for a given platform ident.
	 * 
	 * @param platformIdent
	 *            ID of the platform ident.
	 */
	public synchronized void registerDataSent(long platformIdent) {
		AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent);
		if (null != agentStatusData) {
			agentStatusData.setLastDataSendTimestamp(System.currentTimeMillis());
		}
	}

	/**
	 * Informs the {@link AgentStatusDataProvider} that the platform has been deleted from the CMR.
	 * All kept information will be deleted.
	 * 
	 * @param platformId
	 *            ID of the platform ident.
	 */
	public synchronized void registerDeleted(long platformId) {
		agentStatusDataMap.remove(platformId);
	}

	/**
	 * @return Returns the map of platform ident IDs and dates when the last data was received.
	 */
	public Map<Long, AgentStatusData> getAgentStatusDataMap() {
		long currentTime = System.currentTimeMillis();
		Map<Long, AgentStatusData> map = new HashMap<Long, AgentStatusData>();
		for (Entry<Long, AgentStatusData> entry : agentStatusDataMap.entrySet()) {
			entry.getValue().setServerTimestamp(currentTime);
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
}
