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
import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroupContext;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.serializer.impl.SerializationManager;

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

	public void update(long time, ProcessingUnitGroupContext groupContext) {
		groupContext.getAnomalyStateContext().addHealthStatus(groupContext.getHealthStatus());

		HealthStatus nextAnomalyHealthStatus = getNextAnomalyHealthStatus(groupContext);
		HealthTransition healthTransition = getHealthTransition(groupContext.getAnomalyStateContext().getAnomalyHealthStauts(), nextAnomalyHealthStatus);
		groupContext.getAnomalyStateContext().setAnomalyHealthStauts(nextAnomalyHealthStatus);

		writeAnomalyState(time, groupContext, healthTransition);

		handleHealthTransition(time, groupContext, healthTransition);

		if (nextAnomalyHealthStatus == HealthStatus.CRITICAL) {
			groupContext.getAnomalyStateContext().getCurrentAnomaly().setCritical(true);
		}

		if (groupContext.getAnomalyStateContext().getCurrentAnomaly() != null) {
			int amountCriticalUnits = 0;
			for (ProcessingUnitContext unitContext : groupContext.getProcessingUnitContexts()) {
				if (unitContext.getHealthStatus() == HealthStatus.CRITICAL) {
					amountCriticalUnits++;
				}
			}
			groupContext.getAnomalyStateContext().getCurrentAnomaly().setParallelCriticalProcessingUnits(amountCriticalUnits);
		}
	}

	private void handleHealthTransition(long time, ProcessingUnitGroupContext groupContext, HealthTransition healthTransition) {
		switch (healthTransition) {
		case BEGIN:
			Anomaly newAnomaly = anomalyRegistration.startAnomaly(time);
			newAnomaly.setGroupConfiguration(serializationManager.copy(groupContext.getGroupConfiguration()));
			groupContext.getAnomalyStateContext().setCurrentAnomaly(newAnomaly);

			notificationService.handleHealthTransition(healthTransition, groupContext);

			break;
		case DOWNGRADE:
		case UPGRADE:
			groupContext.getAnomalyStateContext().getCurrentAnomaly().getHealthTransitionLog().put(time, healthTransition);

			notificationService.handleHealthTransition(healthTransition, groupContext);

			break;
		case END:
			anomalyRegistration.endAnomaly(time, groupContext.getAnomalyStateContext().getCurrentAnomaly());

			notificationService.handleHealthTransition(healthTransition, groupContext);

			groupContext.getAnomalyStateContext().setCurrentAnomaly(null);

			break;
		case NO_CHANGE:
		default:
			break;
		}
	}

	private HealthStatus getNextAnomalyHealthStatus(ProcessingUnitGroupContext groupContext) {
		HealthStatus currentAnomalyHealthStatus = groupContext.getAnomalyStateContext().getAnomalyHealthStauts();

		if ((currentAnomalyHealthStatus == HealthStatus.UNKNOWN) || (currentAnomalyHealthStatus == HealthStatus.NORMAL)) {
			return groupContext.getAnomalyStateContext().getLowestHealthStatus(groupContext.getGroupConfiguration().getAnomalyDefinition().getStartCount());
		} else {
			return groupContext.getAnomalyStateContext().getHighestHealthStatus(groupContext.getGroupConfiguration().getAnomalyDefinition().getEndCount());
		}
	}

	private void writeAnomalyState(long time, ProcessingUnitGroupContext groupContext, HealthTransition healthTransition) {
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
		builder.tag(Measurements.Anomalies.TAG_CONFIGURATION_GROUP_ID, groupContext.getGroupConfiguration().getGroupId());

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
