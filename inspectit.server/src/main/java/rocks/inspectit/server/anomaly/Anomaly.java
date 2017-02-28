package rocks.inspectit.server.anomaly;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.MoreObjects;

import rocks.inspectit.server.anomaly.state.StateManager.HealthTransition;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class Anomaly {

	private final String id;

	private long startTime;

	private long endTime;

	private boolean critical;

	private final Map<Long, HealthTransition> healthTransitionLog = new LinkedHashMap<>();

	private AnomalyDetectionGroupConfiguration groupConfiguration;

	protected Anomaly(long startTime) {
		id = UUID.randomUUID().toString();
		this.startTime = startTime;
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues().add("startTime", startTime).add("endTime", endTime).add("isCritical", critical).add("healthTransitions", healthTransitionLog.size())
				.add("relatedGroupId", groupConfiguration.getId()).add("businessTransaction", groupConfiguration.getBusinessTransaction()).toString();
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
	 * Gets {@link #healthTransitionLog}.
	 *
	 * @return {@link #healthTransitionLog}
	 */
	public Map<Long, HealthTransition> getHealthTransitionLog() {
		return this.healthTransitionLog;
	}

	/**
	 * Gets {@link #startTime}.
	 *
	 * @return {@link #startTime}
	 */
	public long getStartTime() {
		return this.startTime;
	}

	/**
	 * Gets {@link #endTime}.
	 *
	 * @return {@link #endTime}
	 */
	public long getEndTime() {
		return this.endTime;
	}

	/**
	 * Sets {@link #endTime}.
	 *
	 * @param endTime
	 *            New value for {@link #endTime}
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets {@link #critical}.
	 *
	 * @return {@link #critical}
	 */
	public boolean isCritical() {
		return this.critical;
	}

	/**
	 * Sets {@link #critical}.
	 *
	 * @param critical
	 *            New value for {@link #critical}
	 */
	public void setCritical(boolean critical) {
		this.critical = critical;
	}
}
