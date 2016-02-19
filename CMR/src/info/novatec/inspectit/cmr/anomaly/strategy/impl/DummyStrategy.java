package info.novatec.inspectit.cmr.anomaly.strategy.impl;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.influxdb.InfluxDBService;

import java.util.List;

import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link AbstractAnomalyDetectionStrategy}. This is only a dummy
 * strategy for testing purpose.
 *
 * @author Marius Oehler
 *
 */
public class DummyStrategy extends AbstractAnomalyDetectionStrategy {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractAnomalyDetectionStrategy.class);

	/**
	 * Constructor.
	 *
	 * @param influxDb
	 *            the influx db service
	 */
	public DummyStrategy(InfluxDBService influxDb) {
		super(influxDb);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAnalysis() {
		log.info("start analysis..");

		QueryResult queryResult = influx.query("select mean(total_cpu_usage) from cpu_information where time > now() - 90m group by time(3s)");

		if (queryResult.hasError()) {
			log.warn("query was not successful. Erro: {}", queryResult.getError());
		} else {
			for (Result r : queryResult.getResults()) {
				List<Series> series = r.getSeries();
				for (Series s : series) {
					List<List<Object>> values = s.getValues();

					double maxValue = Double.NaN;
					String maxValueTime = null;

					for (int i = 0; i < values.size(); i++) {
						Object meanObject = values.get(i).get(1);
						if (meanObject == null) {
							continue;
						}

						double mean = ((Double) meanObject).doubleValue();
						if (Double.isNaN(maxValue) || mean > maxValue) {
							maxValue = mean;
							maxValueTime = (String) values.get(i).get(0);
						}
					}

					if (maxValueTime == null) {
						log.info("No max system load found");
					} else {
						log.info("Max. system load was {} at {}", maxValue, maxValueTime);
					}

					Point build = Point.measurement(s.getName() + "_base").field("maxMean", maxValue).build();
					influx.write(build);

					/*
					 * List<String> columns = s.getColumns(); List<List<Object>> values =
					 * s.getValues(); for (List<Object> valueList : values) { System.out.println(
					 * " {"); for (int i = 0; i < columns.size(); i++) { System.out.println("  " +
					 * columns.get(i) + ": " + valueList.get(i)); } System.out.println(" }"); }
					 */

				}
			}

		}

	}
}
