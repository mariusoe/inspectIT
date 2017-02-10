package rocks.inspectit.server.anomaly.processing;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.server.anomaly.valuesource.impl.InfluxValueSource;
import rocks.inspectit.server.anomaly.valuesource.impl.InfluxValueSource.Function;
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
	InfluxValueSource valueSource;

	private AnomalyProcessingContext context = new AnomalyProcessingContext();

	@PostConstruct
	private void postConstruct() {
		valueSource.setAggregationWindowLength(60);
		valueSource.setMeasurement("data");
		valueSource.setFunction(Function.MEAN);
		valueSource.setTagMap(ImmutableMap.of("generated", "yes"));

		initialize();
	}

	private void initialize() {
		NaN beim ersten Aufruf
		double[] values = valueSource.getValues(24);
		DescriptiveStatistics statistics = new DescriptiveStatistics(values);

		context.setMean(statistics.getMean());
	}

	public void process() {
		try {
			double value = valueSource.getValue();
			log.info("InfluxValueSource: {} - is larger than mean: {}", value, value > context.getMean());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
