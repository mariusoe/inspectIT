package rocks.inspectit.server.anomaly;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.map.LinkedMap;

import com.google.common.base.MoreObjects;

import rocks.inspectit.server.anomaly.state.StateManager.HealthTransition;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;
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

	private final LinkedMap<Long, Double> violationValues = new LinkedMap<>();

	private AnomalyDetectionGroupConfiguration groupConfiguration;

	private int parallelCriticalProcessingUnits = 0;

	private double maxViolationValue = Double.NaN;

	private double maxViolationDelta = Double.NaN;

	private ThresholdType maxViolationThresholdType = null;

	private long maxViolationTime;

	private double maxValue = Double.NaN;

	protected Anomaly(long startTime) {
		id = UUID.randomUUID().toString();
		this.startTime = startTime;
	}

	/**
	 * Gets {@link #maxViolationTime}.
	 *
	 * @return {@link #maxViolationTime}
	 */
	public long getMaxViolationTime() {
		return this.maxViolationTime;
	}

	/**
	 * Sets {@link #maxViolationTime}.
	 *
	 * @param maxViolationTime
	 *            New value for {@link #maxViolationTime}
	 */
	public void setMaxViolationTime(long maxViolationTime) {
		this.maxViolationTime = maxViolationTime;
	}

	/**
	 * Gets {@link #maxViolationThresholdType}.
	 *
	 * @return {@link #maxViolationThresholdType}
	 */
	public ThresholdType getMaxViolationThresholdType() {
		return this.maxViolationThresholdType;
	}

	/**
	 * Sets {@link #maxViolationThresholdType}.
	 *
	 * @param maxViolationThresholdType
	 *            New value for {@link #maxViolationThresholdType}
	 */
	public void setMaxViolationThresholdType(ThresholdType maxViolationThresholdType) {
		this.maxViolationThresholdType = maxViolationThresholdType;
	}

	/**
	 * Gets {@link #maxViolationDelta}.
	 *
	 * @return {@link #maxViolationDelta}
	 */
	public double getMaxViolationDelta() {
		return this.maxViolationDelta;
	}

	/**
	 * Sets {@link #maxViolationDelta}.
	 *
	 * @param maxViolationDelta
	 *            New value for {@link #maxViolationDelta}
	 */
	public void setMaxViolationDelta(double maxViolationDelta) {
		this.maxViolationDelta = maxViolationDelta;
	}

	/**
	 * Gets {@link #maxViolationValue}.
	 *
	 * @return {@link #maxViolationValue}
	 */
	public double getMaxViolationValue() {
		return this.maxViolationValue;
	}

	/**
	 * Sets {@link #maxViolationValue}.
	 *
	 * @param maxViolationValue
	 *            New value for {@link #maxViolationValue}
	 */
	public void setMaxViolationValue(double maxViolationValue) {
		this.maxViolationValue = maxViolationValue;
	}

	/**
	 * Gets {@link #maxValue}.
	 *
	 * @return {@link #maxValue}
	 */
	public double getMaxValue() {
		return this.maxValue;
	}

	/**
	 * Sets {@link #maxValue}.
	 *
	 * @param maxValue
	 *            New value for {@link #maxValue}
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * Gets {@link #parallelCriticalProcessingUnits}.
	 *
	 * @return {@link #parallelCriticalProcessingUnits}
	 */
	public int getParallelCriticalProcessingUnits() {
		return this.parallelCriticalProcessingUnits;
	}

	/**
	 * Sets {@link #parallelCriticalProcessingUnits}.
	 *
	 * @param parallelCriticalProcessingUnits
	 *            New value for {@link #parallelCriticalProcessingUnits}
	 */
	public void setParallelCriticalProcessingUnits(int parallelCriticalProcessingUnits) {
		this.parallelCriticalProcessingUnits = parallelCriticalProcessingUnits;
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
				.add("relatedGroupId", groupConfiguration.getGroupId()).toString();
	}

	public boolean isBusinessTransactionRelated() {
		for (AnomalyDetectionConfiguration adc : groupConfiguration.getConfigurations()) {
			if (!adc.getMetricDefinition().isBusinessTransactionRelated()) {
				return false;
			}
		}
		return true;
	}

	public String getBusinessTransactionName() {
		String btxName = null;
		for (AnomalyDetectionConfiguration adc : groupConfiguration.getConfigurations()) {
			if (btxName == null) {
				btxName = adc.getMetricDefinition().getBusinessTransactionName();
			} else {
				if (!btxName.equals(adc.getMetricDefinition().getBusinessTransactionName())) {
					throw new RuntimeException("not unique");
				}
			}
		}
		return btxName;
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
	 * Gets {@link #violationValues}.
	 *
	 * @return {@link #violationValues}
	 */
	public LinkedMap<Long, Double> getViolationValues() {
		return this.violationValues;
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
