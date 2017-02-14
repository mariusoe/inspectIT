package rocks.inspectit.server.anomaly;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.server.anomaly.baseline.BaselineDefinition;
import rocks.inspectit.server.anomaly.baseline.impl.MovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.classification.IClassifierDefinition;
import rocks.inspectit.server.anomaly.classification.stddev.StandardDeviationClassifierDefinition;
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

		MovingAverageBaselineDefinition baselineDefinition = new MovingAverageBaselineDefinition();
		baselineDefinition.setWindowSize(24);

		StandardDeviationClassifierDefinition classifierDefinition = new StandardDeviationClassifierDefinition();
		configuration.setClassifierDefinition(classifierDefinition);

		configuration.setMetricDefinition(metricDefinition);
		configuration.setBaselineDefinition(baselineDefinition);

		return configuration;
	}

	private String id = UUID.randomUUID().toString();

	private AbstractMetricDefinition metricDefinition;

	private BaselineDefinition baselineDefinition;

	private IClassifierDefinition classifierDefinition;

	// 1 = 15s
	private int intervalDataProcessing = 1;

	// 20 = 15s * 20 = 5min
	private int intervalBaselineProcessing = 20;

	/**
	 * Gets {@link #classifierDefinition}.
	 *
	 * @return {@link #classifierDefinition}
	 */
	public IClassifierDefinition getClassifierDefinition() {
		return this.classifierDefinition;
	}

	/**
	 * Sets {@link #classifierDefinition}.
	 *
	 * @param classifierDefinition
	 *            New value for {@link #classifierDefinition}
	 */
	public void setClassifierDefinition(IClassifierDefinition classifierDefinition) {
		this.classifierDefinition = classifierDefinition;
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets {@link #baselineDefinition}.
	 *
	 * @return {@link #baselineDefinition}
	 */
	public BaselineDefinition getBaselineDefinition() {
		return this.baselineDefinition;
	}

	/**
	 * Sets {@link #baselineDefinition}.
	 *
	 * @param baselineDefinition
	 *            New value for {@link #baselineDefinition}
	 */
	public void setBaselineDefinition(BaselineDefinition baselineDefinition) {
		this.baselineDefinition = baselineDefinition;
	}

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

	/**
	 * Gets {@link #intervalDataProcessing}.
	 *
	 * @return {@link #intervalDataProcessing}
	 */
	public int getIntervalDataProcessing() {
		return this.intervalDataProcessing;
	}

	/**
	 * Gets {@link #intervalBaselineProcessing}.
	 *
	 * @return {@link #intervalBaselineProcessing}
	 */
	public int getIntervalBaselineProcessing() {
		return this.intervalBaselineProcessing;
	}

	/**
	 * Sets {@link #intervalDataProcessing}.
	 *
	 * @param intervalDataAggregation
	 *            New value for {@link #intervalDataProcessing}
	 */
	public void setIntervalDataAggregation(int intervalDataAggregation) {
		this.intervalDataProcessing = intervalDataAggregation;
	}

	/**
	 * Sets {@link #intervalBaselineProcessing}.
	 *
	 * @param intervalBaselineProcessing
	 *            New value for {@link #intervalBaselineProcessing}
	 */
	public void setIntervalBaselineProcessing(int intervalBaselineProcessing) {
		this.intervalBaselineProcessing = intervalBaselineProcessing;
	}
}
