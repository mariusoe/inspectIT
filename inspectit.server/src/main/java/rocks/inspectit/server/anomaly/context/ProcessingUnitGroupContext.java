package rocks.inspectit.server.anomaly.context;

import java.util.LinkedList;
import java.util.List;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.ProcessingUnit;
import rocks.inspectit.server.anomaly.state.AnomalyStateContext;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class ProcessingUnitGroupContext {

	/**
	 * The group id.
	 */
	private String groupId;

	/**
	 * The context of the anomaly state of this group.
	 */
	private AnomalyStateContext anomalyStateContext;

	/**
	 * The configuration of this processing group.
	 */
	private AnomalyDetectionGroupConfiguration groupConfiguration;

	/**
	 * The current health status of this group.
	 */
	private HealthStatus healthStatus = HealthStatus.UNKNOWN;

	/**
	 * Whether the processing group has been initialized.
	 */
	private boolean initialized = false;

	/**
	 * All contexts of {@link ProcessingUnit}s related to this processing group.
	 */
	private List<ProcessingUnitContext> processingUnitContexts = new LinkedList<>();

	/**
	 * Gets {@link #anomalyStateContext}.
	 *
	 * @return {@link #anomalyStateContext}
	 */
	public AnomalyStateContext getAnomalyStateContext() {
		return this.anomalyStateContext;
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
	 * Gets {@link #groupId}.
	 *
	 * @return {@link #groupId}
	 */
	public String getGroupId() {
		return this.groupId;
	}

	/**
	 * Gets {@link #healthStatus}.
	 *
	 * @return {@link #healthStatus}
	 */
	public HealthStatus getHealthStatus() {
		return this.healthStatus;
	}

	/**
	 * Gets {@link #processingUnitContexts}.
	 *
	 * @return {@link #processingUnitContexts}
	 */
	public List<ProcessingUnitContext> getProcessingUnitContexts() {
		return this.processingUnitContexts;
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
	 * Sets {@link #anomalyStateContext}.
	 *
	 * @param stateContext
	 *            New value for {@link #anomalyStateContext}
	 */
	public void setAnomalyStateContext(AnomalyStateContext stateContext) {
		this.anomalyStateContext = stateContext;
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
	 * Sets {@link #groupId}.
	 *
	 * @param groupId
	 *            New value for {@link #groupId}
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Sets {@link #healthStatus}.
	 *
	 * @param healthStatus
	 *            New value for {@link #healthStatus}
	 */
	public void setHealthStatus(HealthStatus healthStatus) {
		this.healthStatus = healthStatus;
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

}
