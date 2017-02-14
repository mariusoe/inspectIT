package rocks.inspectit.server.anomaly.configuration;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.server.anomaly.definition.baseline.BaselineDefinition;
import rocks.inspectit.server.anomaly.definition.baseline.MovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.server.anomaly.definition.metric.InfluxDBMetricDefinition.Function;
import rocks.inspectit.server.anomaly.definition.metric.MetricDefinition;
import rocks.inspectit.server.anomaly.definition.threshold.StandardDeviationThresholdDefinition;
import rocks.inspectit.server.anomaly.definition.threshold.ThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDetectionConfiguration {

	public static AnomalyDetectionConfiguration getTestDefinition() {
		AnomalyDetectionConfiguration configuration = new AnomalyDetectionConfiguration();

		InfluxDBMetricDefinition metricDefinition = new InfluxDBMetricDefinition();
		metricDefinition.setMeasurement("businessTransactions");
		metricDefinition.setFunction(Function.MEAN);
		metricDefinition.setField("duration");
		metricDefinition.setTagMap(ImmutableMap.of("generated", "yes"));

		MovingAverageBaselineDefinition baselineDefinition = new MovingAverageBaselineDefinition();
		baselineDefinition.setWindowSize(24); // * 5min = 2h
		baselineDefinition.setExcludeCriticalData(true);
		// ExponentialMovingAverageBaselineDefinition baselineDefinition = new
		// ExponentialMovingAverageBaselineDefinition();
		// baselineDefinition.setSmoothingFactor(0.05D);
		// baselineDefinition.setTrendSmoothingFactor(0.1D);
		// baselineDefinition.setExcludeCriticalData(true);

		StandardDeviationThresholdDefinition thresholdDefinition = new StandardDeviationThresholdDefinition();
		thresholdDefinition.setWindowSize(36);// * 5min = 3h
		thresholdDefinition.setSigmaAmountCritical(5);
		thresholdDefinition.setSigmaAmountWarning(3);
		thresholdDefinition.setUseResiduals(false);
		thresholdDefinition.setExcludeCriticalData(false);
		thresholdDefinition.setExponentialSmoothed(true);
		thresholdDefinition.setSmoothingFactor(0.1D);

		configuration.setMetricDefinition(metricDefinition);
		configuration.setBaselineDefinition(baselineDefinition);
		configuration.setThresholdDefinition(thresholdDefinition);

		return configuration;
	}

	private String id = UUID.randomUUID().toString();

	private MetricDefinition metricDefinition;

	private BaselineDefinition baselineDefinition;

	private ThresholdDefinition classifierDefinition;

	// = 15s * 240 = 1h
	private int warmupLength = 240;

	// 1 = 15s
	private int intervalDataProcessing = 1;

	// 20 = 15s * 20 = 5min
	private int intervalBaselineProcessing = 20;

	/**
	 * Gets {@link #warmupLength}.
	 *
	 * @return {@link #warmupLength}
	 */
	public int getWarmupLength() {
		return this.warmupLength;
	}

	/**
	 * Sets {@link #warmupLength}.
	 *
	 * @param warmupLength
	 *            New value for {@link #warmupLength}
	 */
	public void setWarmupLength(int warmupLength) {
		this.warmupLength = warmupLength;
	}

	/**
	 * Gets {@link #classifierDefinition}.
	 *
	 * @return {@link #classifierDefinition}
	 */
	public ThresholdDefinition getClassifierDefinition() {
		return this.classifierDefinition;
	}

	/**
	 * Sets {@link #classifierDefinition}.
	 *
	 * @param classifierDefinition
	 *            New value for {@link #classifierDefinition}
	 */
	public void setThresholdDefinition(ThresholdDefinition classifierDefinition) {
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
	public MetricDefinition getMetricDefinition() {
		return this.metricDefinition;
	}

	/**
	 * Sets {@link #metricDefinition}.
	 *
	 * @param metricDefinition
	 *            New value for {@link #metricDefinition}
	 */
	public void setMetricDefinition(MetricDefinition metricDefinition) {
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
