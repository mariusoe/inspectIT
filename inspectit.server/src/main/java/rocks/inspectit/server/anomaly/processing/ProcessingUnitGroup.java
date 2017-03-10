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
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.server.influx.util.InfluxUtils;
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

	private final ProcessingUnitGroupContext groupContext;

	@Autowired
	InfluxDBDao influx;

	@Autowired
	StateManager stateManager;

	@Autowired
	public ProcessingUnitGroup(ProcessingUnitGroupContext groupContext) {
		this.groupContext = groupContext;
	}

	/**
	 * Gets {@link #groupContext}.
	 *
	 * @return {@link #groupContext}
	 */
	public ProcessingUnitGroupContext getGroupContext() {
		return this.groupContext;
	}

	private void updateHealthStatus() {
		switch (groupContext.getGroupConfiguration().getMode()) {
		case WORST:
			groupContext.setHealthStatus(WorstHealthDeclaration.INSTANCE.declareHelthStatus(processingUnits));
			return;
		case BEST:
			groupContext.setHealthStatus(BestHealthDeclaration.INSTANCE.declareHelthStatus(processingUnits));
			return;
		default:
			throw new RuntimeException("Unknown mode for health");
		}
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
			writeProcessingUnitData(time, processor.getContext(), false);
		}

		updateHealthStatus();

		stateManager.update(time, getGroupContext());
		writeGroupHealth(time);
	}

	public void initialize() {
		if (log.isInfoEnabled()) {
			log.info("Started initilazation of processing unit group '{}'", getGroupContext().getGroupConfiguration().getName());
		}

		dropMeasurements();

		long currentTime = System.currentTimeMillis();
		long time = currentTime - groupContext.getGroupConfiguration().getTimeTravelDuration(TimeUnit.MILLISECONDS);

		time = alignTime(time, AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS, TimeUnit.SECONDS);

		while (time < currentTime) {

			for (ProcessingUnit processor : processingUnits) {
				processor.process(time);
				writeProcessingUnitData(time, processor.getContext(), true);
			}

			updateHealthStatus();

			stateManager.update(time, getGroupContext());
			writeGroupHealth(time);

			time += TimeUnit.SECONDS.toMillis(AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS);
		}

		getGroupContext().setInitialized(true);

		if (log.isInfoEnabled()) {
			log.info("Initilazation of processing unit group '{}' has been finished.", getGroupContext().getGroupConfiguration().getName());
		}
	}

	private void writeProcessingUnitData(long time, ProcessingUnitContext unitContext, boolean initializing) {
		String measurement;
		if (initializing) {
			measurement = Measurements.PreviewData.NAME;
		} else {
			measurement = Measurements.Data.NAME;
		}

		Builder builder = Point.measurement(measurement).time(time, TimeUnit.MILLISECONDS);
		builder.tag(Measurements.Data.TAG_CONFIGURATION_ID, unitContext.getConfiguration().getId());
		builder.tag(Measurements.Data.TAG_CONFIGURATION_GROUP_ID, unitContext.getGroupContext().getGroupId());
		builder.tag(Measurements.Data.TAG_HEALTH_STATUS, groupContext.getHealthStatus().toString());

		double baseline = unitContext.getBaseline().getBaseline();
		if (!Double.isNaN(baseline)) {
			builder.addField(Measurements.Data.FIELD_BASELINE, baseline);
		}

		for (ThresholdType type : ThresholdType.values()) {
			if (unitContext.getThreshold().providesThreshold(type)) {
				double threshold = unitContext.getThreshold().getThreshold(unitContext, type);
				if (!Double.isNaN(threshold)) {
					String columnName;
					switch (type) {
					case LOWER_CRITICAL:
						columnName = Measurements.Data.FIELD_LOWER_CRITICAL;
						break;
					case LOWER_WARNING:
						columnName = Measurements.Data.FIELD_LOWER_WARNING;
						break;
					case UPPER_CRITICAL:
						columnName = Measurements.Data.FIELD_UPPER_CRITICAL;
						break;
					case UPPER_WARNING:
						columnName = Measurements.Data.FIELD_UPPER_WARNING;
						break;
					default:
						continue;
					}
					builder.addField(columnName, threshold);
				}
			}
		}

		if (!Double.isNaN(unitContext.getMetricProvider().getValue())) {
			if (initializing) {
				Builder builderData = Point.measurement(Measurements.Data.NAME).time(time, TimeUnit.MILLISECONDS);
				builderData.tag(Measurements.Data.TAG_CONFIGURATION_ID, unitContext.getConfiguration().getId());
				builderData.tag(Measurements.Data.TAG_CONFIGURATION_GROUP_ID, unitContext.getGroupContext().getGroupId());
				builderData.addField(Measurements.Data.FIELD_METRIC_AGGREGATION, unitContext.getMetricProvider().getValue());
				influx.insert(builderData.build());
			} else {
				builder.addField(Measurements.Data.FIELD_METRIC_AGGREGATION, unitContext.getMetricProvider().getValue());
			}
		}

		Point point = InfluxUtils.build(builder);
		if (point != null) {
			influx.insert(point);
		}
	}

	private void dropMeasurements() {
		influx.query("DROP MEASUREMENT " + Measurements.Anomalies.NAME);
		influx.query("DROP MEASUREMENT " + Measurements.Data.NAME);
		influx.query("DROP MEASUREMENT " + Measurements.PreviewData.NAME);
		influx.query("DROP MEASUREMENT " + Measurements.ProcessingUnitGroupStatistics.NAME);
	}

	private void writeGroupHealth(long time) {
		Builder builder = Point.measurement(Measurements.ProcessingUnitGroupStatistics.NAME).time(time, TimeUnit.MILLISECONDS);
		builder.tag(Measurements.ProcessingUnitGroupStatistics.TAG_CONFIGURATION_GROUP_ID, groupContext.getGroupConfiguration().getGroupId());
		builder.tag("name", groupContext.getGroupConfiguration().getName());

		builder.tag(Measurements.ProcessingUnitGroupStatistics.TAG_HEALTH_STATUS, groupContext.getHealthStatus().toString());

		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_UNKNOWN, (groupContext.getHealthStatus() == HealthStatus.UNKNOWN) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_NORMAL, (groupContext.getHealthStatus() == HealthStatus.NORMAL) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_WARNING, (groupContext.getHealthStatus() == HealthStatus.WARNING) ? 1 : 0);
		builder.addField(Measurements.ProcessingUnitGroupStatistics.FIELD_CRITICAL, (groupContext.getHealthStatus() == HealthStatus.CRITICAL) ? 1 : 0);

		influx.insert(builder.build());
	}

	private long alignTime(long time, long align, TimeUnit unit) {
		return time - (time % unit.toMillis(align));
	}
}
