package rocks.inspectit.server.anomaly.state;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.constants.Measurements;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroup;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.cs.ci.anomaly.definition.anomaly.AnomalyDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
public class StateManager {

	public enum HealthTransition {
		BEGIN, UPGRADE, DOWNGRADE, END, NO_CHANGE;
	}

	@Autowired
	InfluxDBDao influx;

	private Map<String, StateContext> contextMap = new HashMap<>();

	/**
	 * @param time
	 * @param rootProcessingUnitGroup
	 */
	public void update(long time, ProcessingUnitGroup unitGroup) {
		StateContext context = contextMap.get(unitGroup.getConfigurationGroup().getGroupId());
		AnomalyDefinition anomalyDefinition = unitGroup.getConfigurationGroup().getAnomalyDefinition();

		if (context == null) {
			context = initialize(unitGroup);
		}
		context.addHealthStatus(unitGroup.getHealthStatus());

		Pair<HealthStatus, HealthStatus> healthStatus = updateHealthStatus(context, anomalyDefinition);

		HealthTransition healthTransition = getHealthTransition(healthStatus.getFirst(), healthStatus.getSecond());

		writeAnomalyState(time, unitGroup, healthTransition);
	}

	private Pair<HealthStatus, HealthStatus> updateHealthStatus(StateContext context, AnomalyDefinition anomalyDefinition) {
		HealthStatus currentHealthStatus = context.getHealthStauts();

		HealthStatus nextHealthStatus;
		if ((currentHealthStatus == HealthStatus.UNKNOWN) || (currentHealthStatus == HealthStatus.NORMAL)) {
			nextHealthStatus = context.getLowestHealthStatus(anomalyDefinition.getStartCount());
		} else {
			nextHealthStatus = context.getHighestHealthStatus(anomalyDefinition.getEndCount());
		}

		context.setHealthStauts(nextHealthStatus);

		return new Pair<HealthStatus, HealthStatus>(currentHealthStatus, nextHealthStatus);
	}

	private StateContext initialize(ProcessingUnitGroup unitGroup) {
		int contextHealthListSize = Math.max(unitGroup.getConfigurationGroup().getAnomalyDefinition().getStartCount(), unitGroup.getConfigurationGroup().getAnomalyDefinition().getEndCount());

		StateContext context = new StateContext();
		context.setMaxHealthListSize(contextHealthListSize);

		contextMap.put(unitGroup.getConfigurationGroup().getGroupId(), context);

		return context;
	}

	private void writeAnomalyState(long time, ProcessingUnitGroup unitGroup, HealthTransition healthTransition) {
		Builder builder = Point.measurement(Measurements.Anomalies.NAME);

		long timeDelta = 0L;
		switch (healthTransition) {
		case BEGIN:
		case DOWNGRADE:
		case UPGRADE:
			timeDelta = TimeUnit.SECONDS.toMillis(unitGroup.getConfigurationGroup().getAnomalyDefinition().getStartCount() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S);
			builder.addField(Measurements.Anomalies.FIELD_ANOMALY_ACTIVE, 1);
			break;
		case END:
			timeDelta = TimeUnit.SECONDS.toMillis(unitGroup.getConfigurationGroup().getAnomalyDefinition().getEndCount() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S);
			builder.addField(Measurements.Anomalies.FIELD_ANOMALY_ACTIVE, 0);
			break;
		case NO_CHANGE:
		default:
			return;
		}

		builder.time(time - timeDelta, TimeUnit.MILLISECONDS);
		builder.tag(Measurements.Anomalies.TAG_EVENT, healthTransition.toString());
		builder.tag(Measurements.Anomalies.TAG_CONFIGURATION_GROUP_ID, unitGroup.getConfigurationGroup().getGroupId());

		influx.insert(builder.build());
	}

	private HealthTransition getHealthTransition(HealthStatus current, HealthStatus next) {
		if ((current == HealthStatus.NORMAL) && ((next == HealthStatus.WARNING) || (next == HealthStatus.CRITICAL))) {
			return HealthTransition.BEGIN;
		}

		if ((current == HealthStatus.WARNING) && (next == HealthStatus.CRITICAL)) {
			return HealthTransition.UPGRADE;
		}

		if ((current == HealthStatus.CRITICAL) && (next == HealthStatus.WARNING)) {
			return HealthTransition.DOWNGRADE;
		}

		if (((current == HealthStatus.WARNING) || (current == HealthStatus.CRITICAL)) && (next == HealthStatus.NORMAL)) {
			return HealthTransition.END;
		}

		return HealthTransition.NO_CHANGE;
	}
}
