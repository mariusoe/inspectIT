package rocks.inspectit.server.anomaly.processing;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.state.StateContext;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class ProcessingGroupContext {

	private String groupId;

	private AnomalyDetectionGroupConfiguration groupConfiguration;

	private HealthStatus groupHealthStatus;

	private boolean initialized = false;

	private StateContext stateContext;

	/**
	 * Gets {@link #stateContext}.
	 * 
	 * @return {@link #stateContext}
	 */
	public StateContext getStateContext() {
		return this.stateContext;
	}

	/**
	 * Sets {@link #stateContext}.
	 * 
	 * @param stateContext
	 *            New value for {@link #stateContext}
	 */
	public void setStateContext(StateContext stateContext) {
		this.stateContext = stateContext;
	}

	/**
	 * Gets {@link #groupHealthStatus}.
	 *
	 * @return {@link #groupHealthStatus}
	 */
	public HealthStatus getGroupHealthStatus() {
		return this.groupHealthStatus;
	}

	/**
	 * Sets {@link #groupHealthStatus}.
	 *
	 * @param groupHealthStatus
	 *            New value for {@link #groupHealthStatus}
	 */
	public void setGroupHealthStatus(HealthStatus groupHealthStatus) {
		this.groupHealthStatus = groupHealthStatus;
	}

	/**
	 * Gets {@link #initialized}.
	 *
	 * @return {@link #initialized}
	 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Sets {@link #initialized}.
	 *
	 * @param initialized
	 *            New value for {@link #initialized}
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Gets {@link #groupConfiguration}.
	 *
	 * @return {@link #groupConfiguration}
	 */
	public AnomalyDetectionGroupConfiguration getGroupConfiguration() {
		return this.groupConfiguration;
	}

	/**
	 * Sets {@link #groupConfiguration}.
	 *
	 * @param groupConfiguration
	 *            New value for {@link #groupConfiguration}
	 */
	public void setGroupConfiguration(AnomalyDetectionGroupConfiguration groupConfiguration) {
		this.groupConfiguration = groupConfiguration;
	}

	/**
	 * Gets {@link #groupId}.
	 *
	 * @return {@link #groupId}
	 */
	public String getGroupId() {
		return this.groupId;
	}

	/**
	 * Sets {@link #groupId}.
	 *
	 * @param groupId
	 *            New value for {@link #groupId}
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

}
