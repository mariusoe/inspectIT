package rocks.inspectit.server.anomaly.processing;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.state.StateManager;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class RootProcessingUnitGroup extends ProcessingUnitGroup {

	@Log
	private Logger log;

	@Autowired
	InfluxDBDao influx;

	@Autowired
	StateManager stateManager;

	public void process() {
		long time = System.currentTimeMillis();
		super.process(time);
		stateManager.update(time, this);
		writeGroupHealth(time);
	}

	public void initialize() {
		influx.query("DROP MEASUREMENT inspectit_anomaly");
		influx.query("DROP MEASUREMENT inspectit_anomaly_groups");
		influx.query("DROP MEASUREMENT inspectit_anomaly_status");

		log.info("start init of group");

		long currentTime = System.currentTimeMillis();
		long time = currentTime - getConfigurationGroup().getTimeTravelDuration(TimeUnit.MILLISECONDS);

		time = alignTime(time, AnomalyDetectionSystem.PROCESSING_INTERVAL_S, TimeUnit.SECONDS);

		while (time < currentTime) {


			super.initialize(time);
			stateManager.update(time, this);
			writeGroupHealth(time);


			time += TimeUnit.SECONDS.toMillis(AnomalyDetectionSystem.PROCESSING_INTERVAL_S);
		}

		log.info("done init");
	}

	private void writeGroupHealth(long time) {
		Builder builder = Point.measurement("inspectit_anomaly_groups").time(time, TimeUnit.MILLISECONDS);
		builder.tag("configuration_group_id", getConfigurationGroup().getGroupId());
		builder.tag("name", getConfigurationGroup().getName());

		builder.tag("health_status", getHealthStatus().toString());

		// if (healthTransition != HealthTransition.NO_CHANGE) {
		// builder.tag("health_transition", healthTransition.toString());
		// }

		builder.addField("unknown", (getHealthStatus() == HealthStatus.UNKNOWN) ? 1 : 0);
		builder.addField("normal", (getHealthStatus() == HealthStatus.NORMAL) ? 1 : 0);
		builder.addField("warning", (getHealthStatus() == HealthStatus.WARNING) ? 1 : 0);
		builder.addField("critical", (getHealthStatus() == HealthStatus.CRITICAL) ? 1 : 0);

		influx.insert(builder.build());
	}

	private long alignTime(long time, long align, TimeUnit unit) {
		return time - (time % unit.toMillis(align));
	}
}
