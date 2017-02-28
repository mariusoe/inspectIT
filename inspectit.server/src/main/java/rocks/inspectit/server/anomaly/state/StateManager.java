package rocks.inspectit.server.anomaly.state;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.Anomaly;
import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.AnomalyRegistry;
import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.constants.Measurements;
import rocks.inspectit.server.anomaly.notification.NotificationService;
import rocks.inspectit.server.anomaly.processing.ProcessingGroupContext;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.serializer.impl.SerializationManager;
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

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private AnomalyRegistry anomalyRegistration;

	@Autowired
	private SerializationManager serializationManager;

	/**
	 * @param time
	 * @param rootProcessingUnitGroup
	 */
	public void update(long time, ProcessingGroupContext groupContext) {
		AnomalyDefinition anomalyDefinition = groupContext.getGroupConfiguration().getAnomalyDefinition();

		if (groupContext.getStateContext() == null) {
			initialize(groupContext);
		}
		groupContext.getStateContext().addHealthStatus(groupContext.getGroupHealthStatus());

		Pair<HealthStatus, HealthStatus> healthStatus = updateHealthStatus(groupContext.getStateContext(), anomalyDefinition);

		HealthTransition healthTransition = getHealthTransition(healthStatus.getFirst(), healthStatus.getSecond());

		writeAnomalyState(time, groupContext, healthTransition);

		switch (healthTransition) {
		case BEGIN:
			startAnomaly(time, groupContext);
			break;
		case DOWNGRADE:
		case UPGRADE:
			groupContext.getStateContext().getCurrentAnomaly().getHealthTransitionLog().put(time, healthTransition);
			break;
		case END:
			endAnomaly(time, groupContext);
			break;
		case NO_CHANGE:
		default:
			break;
		}

		if (healthStatus.getSecond() == HealthStatus.CRITICAL) {
			groupContext.getStateContext().getCurrentAnomaly().setCritical(true);
		}

		notificationService.handleHealthTransition(healthTransition, groupContext);
	}

	private Anomaly startAnomaly(long time, ProcessingGroupContext groupContext) {
		Anomaly anomaly = anomalyRegistration.startAnomaly(time);
		anomaly.setGroupConfiguration(serializationManager.copy(groupContext.getGroupConfiguration()));

		groupContext.getStateContext().setCurrentAnomaly(anomaly);

		return anomaly;
	}

	private void endAnomaly(long time, ProcessingGroupContext groupContext) {
		anomalyRegistration.endAnomaly(time, groupContext.getStateContext().getCurrentAnomaly());
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

	private void initialize(ProcessingGroupContext groupContext) {
		int contextHealthListSize = Math.max(groupContext.getGroupConfiguration().getAnomalyDefinition().getStartCount(), groupContext.getGroupConfiguration().getAnomalyDefinition().getEndCount());

		StateContext context = new StateContext();
		context.setMaxHealthListSize(contextHealthListSize);

		groupContext.setStateContext(context);
	}

	private void writeAnomalyState(long time, ProcessingGroupContext groupContext, HealthTransition healthTransition) {
		Builder builder = Point.measurement(Measurements.Anomalies.NAME);

		long timeDelta = 0L;
		switch (healthTransition) {
		case BEGIN:
			timeDelta = TimeUnit.SECONDS.toMillis(groupContext.getGroupConfiguration().getAnomalyDefinition().getStartCount() * AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS);
			builder.addField(Measurements.Anomalies.FIELD_ANOMALY_ACTIVE, 1);
			break;
		case DOWNGRADE:
		case UPGRADE:
		case END:
			timeDelta = TimeUnit.SECONDS.toMillis(groupContext.getGroupConfiguration().getAnomalyDefinition().getEndCount() * AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS);
			builder.addField(Measurements.Anomalies.FIELD_ANOMALY_ACTIVE, 0);
			break;
		case NO_CHANGE:
		default:
			return;
		}

		builder.time(time - timeDelta, TimeUnit.MILLISECONDS);
		builder.tag(Measurements.Anomalies.TAG_EVENT, healthTransition.toString());
		builder.tag(Measurements.Anomalies.TAG_CONFIGURATION_GROUP_ID, groupContext.getGroupConfiguration().getId());

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
