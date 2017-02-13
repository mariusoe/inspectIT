package rocks.inspectit.server.anomaly;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.server.anomaly.metric.definition.AbstractMetricDefinition;
import rocks.inspectit.server.anomaly.metric.definition.InfluxDBMetricDefinition;
import rocks.inspectit.server.anomaly.metric.definition.InfluxDBMetricDefinition.Function;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDetectionConfiguration {

	public static AnomalyDetectionConfiguration getTestDefinition() {
		AnomalyDetectionConfiguration configuration = new AnomalyDetectionConfiguration();

		InfluxDBMetricDefinition metricDefinition = new InfluxDBMetricDefinition();
		metricDefinition.setMeasurement("data");
		metricDefinition.setFunction(Function.MEAN);
		metricDefinition.setField("value");
		metricDefinition.setTagMap(ImmutableMap.of("generated", "yes"));

		configuration.setMetricDefinition(metricDefinition);

		return configuration;
	}

	private AbstractMetricDefinition metricDefinition;

	/**
	 * Gets {@link #metricDefinition}.
	 *
	 * @return {@link #metricDefinition}
	 */
	public AbstractMetricDefinition getMetricDefinition() {
		return this.metricDefinition;
	}

	/**
	 * Sets {@link #metricDefinition}.
	 *
	 * @param metricDefinition
	 *            New value for {@link #metricDefinition}
	 */
	public void setMetricDefinition(AbstractMetricDefinition metricDefinition) {
		this.metricDefinition = metricDefinition;
	}

}
