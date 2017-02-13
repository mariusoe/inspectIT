package rocks.inspectit.server.anomaly.processing;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
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

	private AbstractMetricProvider<?> metricProvider;

	private AnomalyProcessingContext context = new AnomalyProcessingContext();

	private long initializationWindowSize = TimeUnit.DAYS.toSeconds(7);

	private long aggreagationWindowSize = TimeUnit.MINUTES.toSeconds(15);

	@PostConstruct
	private void postConstruct() {

		context.getStats().setWindowSize((int) (TimeUnit.HOURS.toSeconds(2) / aggreagationWindowSize));

		initialize();
	}

	private void initialize() {
		influx.query("DROP MEASUREMENT test");

		long initWindow = TimeUnit.DAYS.toSeconds(5);

		// double[] values = null;
		// while (values == null) {
		// values = valueSource.getValues((int) (initWindow / aggreagationWindowSize));
		// }

		long current = System.currentTimeMillis();
		long utc = current - (initWindow * 1000L);
		utc -= utc % (aggreagationWindowSize * 1000L);

		// for (double value : values) {
		while (utc < current) {
			process(utc);
			utc += aggreagationWindowSize * 1000;
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

	}

	private void process(long time) {

		double value = metricProvider.getValue(time, 15, TimeUnit.MINUTES);

		context.getStats().addValue(value);

		double mean = context.getStats().getMean();
		double deviation = metricProvider.getStandardDeviation(time, 2, TimeUnit.HOURS);
		double perc = metricProvider.getPercentile(95, time, 2, TimeUnit.HOURS);

		System.out.println(new Date(time) + " " + mean);
		influx.insert(Point.measurement("test").time(time, TimeUnit.MILLISECONDS).addField("mean", mean).addField("dev", deviation).addField("95th", perc).build());

	}

	public void process() {
		process(System.currentTimeMillis());
	}

	/**
	 * @param metricProvider
	 */
	public void setMetricProvider(AbstractMetricProvider<?> metricProvider) {
		this.metricProvider = metricProvider;
	}

}
