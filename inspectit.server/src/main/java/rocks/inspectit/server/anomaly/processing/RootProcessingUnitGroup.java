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
import rocks.inspectit.server.anomaly.constants.Measurements;
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
		log.info("██╗███╗   ██╗███████╗██████╗ ███████╗ ██████╗████████╗██╗████████╗");
		log.info("██║████╗  ██║██╔════╝██╔══██╗██╔════╝██╔════╝╚══██╔══╝██║╚══██╔══╝");
		log.info("██║██╔██╗ ██║███████╗██████╔╝█████╗  ██║        ██║   ██║   ██║");
		log.info("██║██║╚██╗██║╚════██║██╔═══╝ ██╔══╝  ██║        ██║   ██║   ██║");
		log.info("██║██║ ╚████║███████║██║     ███████╗╚██████╗   ██║   ██║   ██║");
		log.info("╚═╝╚═╝  ╚═══╝╚══════╝╚═╝     ╚══════╝ ╚═════╝   ╚═╝   ╚═╝   ╚═╝");

		log.info("Started initilazation of anomaly detection system..");

		dropMeasurements();

		long currentTime = System.currentTimeMillis();
		long time = currentTime - getConfigurationGroup().getTimeTravelDuration(TimeUnit.MILLISECONDS);

		time = alignTime(time, AnomalyDetectionSystem.PROCESSING_INTERVAL_S, TimeUnit.SECONDS);

		while (time < currentTime) {

			super.initialize(time);
			stateManager.update(time, this);
			writeGroupHealth(time);

			time += TimeUnit.SECONDS.toMillis(AnomalyDetectionSystem.PROCESSING_INTERVAL_S);
		}

		log.info("Initilazation of anomaly detection system is done.");
	}

	private void dropMeasurements() {
		influx.query("DROP MEASUREMENT " + Measurements.Anomalies.NAME);
		influx.query("DROP MEASUREMENT " + Measurements.Data.NAME);
		influx.query("DROP MEASUREMENT " + Measurements.ProcessingUnitGroupStatistics.NAME);
	}

	private void writeGroupHealth(long time) {
		Builder builder = Point.measurement(Measurements.ProcessingUnitGroupStatistics.NAME).time(time, TimeUnit.MILLISECONDS);
		builder.tag(Measurements.ProcessingUnitGroupStatistics.TAG_CONFIGURATION_GROUP_ID, getConfigurationGroup().getGroupId());
		builder.tag("name", getConfigurationGroup().getName());

		builder.tag(Measurements.ProcessingUnitGroupStatistics.TAG_HEALTH_STATUS, getHealthStatus().toString());

		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_UNKNOWN, (getHealthStatus() == HealthStatus.UNKNOWN) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_NORMAL, (getHealthStatus() == HealthStatus.NORMAL) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_WARNING, (getHealthStatus() == HealthStatus.WARNING) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_CRITICAL, (getHealthStatus() == HealthStatus.CRITICAL) ? 1 : 0);

		influx.insert(builder.build());
	}

	private long alignTime(long time, long align, TimeUnit unit) {
		return time - (time % unit.toMillis(align));
	}
}
