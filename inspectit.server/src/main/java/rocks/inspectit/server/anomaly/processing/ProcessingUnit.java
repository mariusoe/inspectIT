package rocks.inspectit.server.anomaly.processing;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
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

	private ProcessingContext context = new ProcessingContext();

	private String groupId;

	@PostConstruct
	private void postConstruct() {
	}

	/**
	 * Sets {@link #configuration}.
	 *
	 * @param configuration
	 *            New value for {@link #configuration}
	 */
	public void setConfiguration(AnomalyDetectionConfiguration configuration) {
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
			context.getValueStatistics().addValue(getCurrentValue(time));

			if ((context.getIterationCounter() % longInterval) == 0) {
				processLong(time);
			}

			processShort(time);

			// ###########
			Builder builder = preparePointBuilder(time);
			// ###########

			if (builder != null) {
				influx.insert(builder.build());
			}
		}

		context.incrementInterationCounter();
	}

	private double getCurrentValue(long time) {
		long timeWindow = context.getConfiguration().getIntervalShortProcessing() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;

		return context.getMetricProvider().getValue(time, timeWindow, TimeUnit.SECONDS);
	}

	private void processShort(long time) {
		log.debug("Process data at time {}", time);

		HealthStatus status = context.getClassifier().classify(context, time);

		context.setHealthStatus(status);
	}

	private void processLong(long time) {
		log.debug("Process baseline at time {}", time);

		context.getBaseline().process(context, time);
		context.getThreshold().process(context, time);
	}

	private Builder preparePointBuilder(long time) {
		Builder builder = Point.measurement("inspectit_anomaly").time(time, TimeUnit.MILLISECONDS);
		builder.tag("configuration_id", context.getConfiguration().getId());
		builder.tag("configuration_group_id", groupId);
		builder.tag("health_status", context.getHealthStatus().toString());

		boolean builderIsEmpty = true;

		if (!Double.isNaN(context.getCurrentValue())) {
			builder.addField("value", context.getCurrentValue());
		}

		double baseline = context.getBaseline().getBaseline();
		if (!Double.isNaN(baseline)) {
			builderIsEmpty = false;
			builder.addField("baseline", baseline);
		}

		for (ThresholdType type : ThresholdType.values()) {
			if (context.getThreshold().providesThreshold(type)) {
				double threshold = context.getThreshold().getThreshold(context, type);
				if (!Double.isNaN(threshold)) {
					builderIsEmpty = false;
					builder.addField(type.toString(), threshold);
				}
			}
		}

		if (builderIsEmpty) {
			return null;
		} else {
			return builder;
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

	/**
	 * Sets {@link #groupId}.
	 *
	 * @param groupId
	 *            New value for {@link #groupId}
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
}
