package rocks.inspectit.server.anomaly.processing;

import java.util.ArrayList;
import java.util.List;
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
import rocks.inspectit.server.anomaly.processing.health.BestHealthDeclaration;
import rocks.inspectit.server.anomaly.processing.health.WorstHealthDeclaration;
import rocks.inspectit.server.anomaly.state.StateManager;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class ProcessingUnitGroup {

	@Log
	private Logger log;

	private List<ProcessingUnit> processingUnits = new ArrayList<>();

	private final ProcessingUnitGroupContext context;

	@Autowired
	InfluxDBDao influx;

	@Autowired
	StateManager stateManager;

	@Autowired
	public ProcessingUnitGroup(ProcessingUnitGroupContext groupContext) {
		context = groupContext;
	}

	/**
	 * Gets {@link #context}.
	 *
	 * @return {@link #context}
	 */
	public ProcessingUnitGroupContext getGroupContext() {
		return this.context;
	}

	private void updateHealthStatus() {
		switch (context.getGroupConfiguration().getMode()) {
		case WORST:
			context.setHealthStatus(WorstHealthDeclaration.INSTANCE.declareHelthStatus(processingUnits));
			return;
		case BEST:
			context.setHealthStatus(BestHealthDeclaration.INSTANCE.declareHelthStatus(processingUnits));
			return;
		default:
			throw new RuntimeException("Unknown mode for health");
		}
	}

	/**
	 * Gets {@link #context}.
	 *
	 * @return {@link #context}
	 */
	public ProcessingUnitGroupContext getContext() {
		return this.context;
	}

	/**
	 * Gets {@link #processingUnits}.
	 *
	 * @return {@link #processingUnits}
	 */
	public List<ProcessingUnit> getProcessingUnits() {
		return this.processingUnits;
	}

	public void process() {
		long time = System.currentTimeMillis();

		for (ProcessingUnit processor : processingUnits) {
			processor.process(time);
		}

		updateHealthStatus();

		stateManager.update(time, getGroupContext());
		writeGroupHealth(time);
	}

	public void initialize() {
		log.info("Started initilazation of anomaly detection system..");

		dropMeasurements();

		long currentTime = System.currentTimeMillis();
		long time = currentTime - context.getGroupConfiguration().getTimeTravelDuration(TimeUnit.MILLISECONDS);

		time = alignTime(time, AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS, TimeUnit.SECONDS);

		while (time < currentTime) {

			for (ProcessingUnit processor : processingUnits) {
				processor.initialize(time);
			}

			updateHealthStatus();

			stateManager.update(time, getGroupContext());
			writeGroupHealth(time);

			time += TimeUnit.SECONDS.toMillis(AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS);
		}

		getGroupContext().setInitialized(true);

		log.info("Initilazation of anomaly detection system is done.");
	}

	private void dropMeasurements() {
		influx.query("DROP MEASUREMENT " + Measurements.Anomalies.NAME);
		influx.query("DROP MEASUREMENT " + Measurements.Data.NAME);
		influx.query("DROP MEASUREMENT " + Measurements.ProcessingUnitGroupStatistics.NAME);
	}

	private void writeGroupHealth(long time) {
		Builder builder = Point.measurement(Measurements.ProcessingUnitGroupStatistics.NAME).time(time, TimeUnit.MILLISECONDS);
		builder.tag(Measurements.ProcessingUnitGroupStatistics.TAG_CONFIGURATION_GROUP_ID, context.getGroupConfiguration().getGroupId());
		builder.tag("name", context.getGroupConfiguration().getName());

		builder.tag(Measurements.ProcessingUnitGroupStatistics.TAG_HEALTH_STATUS, context.getHealthStatus().toString());

		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_UNKNOWN, (context.getHealthStatus() == HealthStatus.UNKNOWN) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_NORMAL, (context.getHealthStatus() == HealthStatus.NORMAL) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_WARNING, (context.getHealthStatus() == HealthStatus.WARNING) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_CRITICAL, (context.getHealthStatus() == HealthStatus.CRITICAL) ? 1 : 0);

		influx.insert(builder.build());
	}

	private long alignTime(long time, long align, TimeUnit unit) {
		return time - (time % unit.toMillis(align));
	}
}
