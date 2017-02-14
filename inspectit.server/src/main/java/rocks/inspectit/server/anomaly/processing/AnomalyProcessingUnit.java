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

	// private long initializationWindowSize = TimeUnit.DAYS.toSeconds(7);
	//
	// private long aggreagationWindowSize = TimeUnit.MINUTES.toSeconds(15);

	@PostConstruct
	private void postConstruct() {

		// context.getStats().setWindowSize((int) (TimeUnit.HOURS.toSeconds(2) /
		// aggreagationWindowSize));
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

	public void initialize() {
		log.info("start init");

		influx.query("DROP MEASUREMENT inspectit_anomaly");

		long initWindow = TimeUnit.HOURS.toSeconds(24 * 7);

		// double[] values = null;
		// while (values == null) {
		// values = valueSource.getValues((int) (initWindow / aggreagationWindowSize));
		// }

		long current = System.currentTimeMillis();
		long utc = current - (initWindow * 1000L);
		utc -= utc % (AnomalyDetectionSystem.PROCESSING_INTERVAL_S * 1000L);

		// for (double value : values) {
		while (utc < current) {
			process(utc);
			utc += AnomalyDetectionSystem.PROCESSING_INTERVAL_S * 1000;
		}

		// double[] parameter = forecastFactory.bruteForceParameterDetermination(values);
		// hwForecast = forecastFactory.createHoltWinters();
		// hwForecast.setSmoothingFactor(parameter[1]);
		// hwForecast.setTrendSmoothingFactor(parameter[2]);
		// hwForecast.setSeasonalSmoothingFactor(parameter[3]);
		//
		// double[] train = ArrayUtils.subarray(values, 0, 192);
		// double[] val = ArrayUtils.subarray(values, 192, values.length);
		//
		// hwForecast.train(train);
		//
		// utc += windowSizeInSeconds * 1000L * 192L;
		//
		// for (double value : val) {
		// utc += windowSizeInSeconds * 1000L;
		//
		// try {
		// hwForecast.fit(value);
		// } catch (Exception e) {
		// }
		// double forecast = hwForecast.forecast();
		// if (!Double.isNaN(forecast)) {
		// influx.insert(Point.measurement("test").time(utc,
		// TimeUnit.MILLISECONDS).addField("forecast", forecast).build());
		// }
		// }
		log.info("done init");

	}

	private void process(long time) {
		log.debug("iteration {}", context.getIterationCounter());

		if ((context.getIterationCounter() % context.getConfiguration().getIntervalBaselineProcessing()) == 0) {
			processBaseline(time);
		}

		if ((context.getIterationCounter() % context.getConfiguration().getIntervalDataProcessing()) == 0) {
			processData(time);
		}

		// double value = metricProvider.getValue(time, 15, TimeUnit.MINUTES);
		//
		// context.getStats().addValue(value);
		//
		// double mean = context.getStats().getMean();
		// double deviation = metricProvider.getStandardDeviation(time, 2, TimeUnit.HOURS);
		// double perc = metricProvider.getPercentile(95, time, 2, TimeUnit.HOURS);
		//
		// System.out.println(new Date(time) + " " + mean);
		// influx.insert(Point.measurement("test").time(time,
		// TimeUnit.MILLISECONDS).addField("mean", mean).addField("dev", deviation).addField("95th",
		// perc).build());

		context.incrementInterationCounter();
	}

	private void processData(long time) {
		log.debug("Process data at time {}", time);
	}

	private void processBaseline(long time) {
		log.debug("Process baseline at time {}", time);

		context.getBaseline().process(context, time);
		context.getThreshold().process(context, time);

		double baseline = context.getBaseline().getBaseline();
		double thresholdCritical = context.getThreshold().getThreshold(context, ThresholdType.UPPER_CRITICAL);
		double thresholdWarning = context.getThreshold().getThreshold(context, ThresholdType.UPPER_WARNING);

		Builder builder = Point.measurement("inspectit_anomaly").time(time, TimeUnit.MILLISECONDS);
		builder.tag("configuration_id", context.getConfiguration().getId());

		boolean builderIsEmpty = true;
		if (!Double.isNaN(baseline)) {
			builderIsEmpty = false;
			builder.addField("baseline", baseline);
		}
		if (!Double.isNaN(thresholdCritical)) {
			builderIsEmpty = false;
			builder.addField("upper_critical", thresholdCritical);
		}
		if (!Double.isNaN(thresholdWarning)) {
			builderIsEmpty = false;
			builder.addField("upper_warning", thresholdWarning);
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
	 * @param classifier
	 */
	public void setClassifier(AbstractThreshold<?> classifier) {
		context.setClassifier(classifier);
	}

}
