package rocks.inspectit.server.anomaly.processing;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.constants.Measurements;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class ProcessingUnit implements IAnomalyProcessor {

	@Log
	private Logger log;

	@Autowired
	InfluxDBDao influx;

	private final ProcessingContext context;

	/**
	 * @param groupContext
	 */
	@Autowired
	public ProcessingUnit(ProcessingGroupContext groupContext, AnomalyDetectionConfiguration configuration) {
		context = new ProcessingContext();
		context.setGroupContext(groupContext);
		context.setConfiguration(configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(long time) {
		process(time);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(long time) {
		int shortInterval = context.getConfiguration().getIntervalShortProcessing();
		int longInterval = context.getConfiguration().getIntervalShortProcessing() * context.getConfiguration().getIntervalLongProcessingMultiplier();

		if ((context.getIterationCounter() % shortInterval) == 0) {
			context.getMetricProvider().next(context, time);

			if ((context.getIterationCounter() % longInterval) == 0) {
				processLong(time);
			}

			processShort(time);

			writeData(time);
		}

		context.incrementInterationCounter();
	}

	private void processShort(long time) {
		HealthStatus status = context.getClassifier().classify(context, time);
		context.setHealthStatus(status);
	}

	private void processLong(long time) {
		context.getBaseline().process(context, time);
		context.getThreshold().process(context, time);
	}

	private void writeData(long time) {
		Builder builder = Point.measurement(Measurements.Data.NAME).time(time, TimeUnit.MILLISECONDS);
		builder.tag(Measurements.Data.TAG_CONFIGURATION_ID, context.getConfiguration().getId());
		builder.tag(Measurements.Data.TAG_CONFIGURATION_GROUP_ID, context.getGroupContext().getGroupId());
		builder.tag(Measurements.Data.TAG_HEALTH_STATUS, context.getHealthStatus().toString());

		boolean builderIsEmpty = true;

		if (!Double.isNaN(context.getMetricProvider().getValue())) {
			builderIsEmpty = false;
			builder.addField(Measurements.Data.FIELD_METRIC_AGGREGATION, context.getMetricProvider().getValue());
		}

		double baseline = context.getBaseline().getBaseline();
		if (!Double.isNaN(baseline)) {
			builderIsEmpty = false;
			builder.addField(Measurements.Data.FIELD_BASELINE, baseline);
		}

		for (ThresholdType type : ThresholdType.values()) {
			if (context.getThreshold().providesThreshold(type)) {
				double threshold = context.getThreshold().getThreshold(context, type);
				if (!Double.isNaN(threshold)) {
					builderIsEmpty = false;

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

		if (!builderIsEmpty) {
			influx.insert(builder.build());
		}
	}

	/**
	 * @param metricProvider
	 */
	public void setMetricProvider(AbstractMetricProvider<?> metricProvider) {
		context.setMetricProvider(metricProvider);
	}

	/**
	 * @param baseline
	 */
	public void setBaseline(AbstractBaseline<?> baseline) {
		context.setBaseline(baseline);
	}

	/**
	 * @param threshold
	 */
	public void setThreshold(AbstractThreshold<?> threshold) {
		context.setThreshold(threshold);
	}

	/**
	 * @param classifier
	 */
	public void setClassifier(AbstractClassifier<?> classifier) {
		context.setClassifier(classifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthStatus getHealthStatus() {
		return context.getHealthStatus();
	}
}
