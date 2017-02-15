package rocks.inspectit.server.anomaly.processing;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.classification.HealthStatus;
import rocks.inspectit.server.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyProcessingUnit {

	@Log
	private Logger log;

	@Autowired
	InfluxDBDao influx;

	private AnomalyProcessingContext context = new AnomalyProcessingContext();

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

	private long alignTime(long time, long align, TimeUnit unit) {
		return time - (time % unit.toMillis(align));
	}

	public void initialize() {
		log.info("start init");

		influx.query("DROP MEASUREMENT inspectit_anomaly");

		long currentTime = System.currentTimeMillis();
		long time = currentTime - context.getConfiguration().getTimeTravelDuration(TimeUnit.MILLISECONDS);

		time = alignTime(time, AnomalyDetectionSystem.PROCESSING_INTERVAL_S, TimeUnit.SECONDS);

		while (time < currentTime) {
			process(time);

			time += TimeUnit.SECONDS.toMillis(AnomalyDetectionSystem.PROCESSING_INTERVAL_S);
		}

		log.info("done init");

	}

	private void process(long time) {
		log.debug("iteration {}", context.getIterationCounter());

		if ((context.getIterationCounter() % context.getConfiguration().getIntervalLongProcessing()) == 0) {
			processLong(time);
		}

		if ((context.getIterationCounter() % context.getConfiguration().getIntervalShortProcessing()) == 0) {
			processShort(time);
		}

		context.incrementInterationCounter();
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

		writeData(time);
	}

	private void writeData(long time) {
		Builder builder = Point.measurement("inspectit_anomaly").time(time, TimeUnit.MILLISECONDS);
		builder.tag("configuration_id", context.getConfiguration().getId());

		builder.tag("health_status", context.getHealthStatus().toString());

		boolean builderIsEmpty = true;

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

		if (!builderIsEmpty) {
			influx.insert(builder.build());
		}
	}

	public void process() {
		long time = System.currentTimeMillis();
		process(time);
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

}
