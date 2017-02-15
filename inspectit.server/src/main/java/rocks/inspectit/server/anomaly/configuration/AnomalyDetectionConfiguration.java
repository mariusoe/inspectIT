package rocks.inspectit.server.anomaly.configuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.server.anomaly.definition.baseline.BaselineDefinition;
import rocks.inspectit.server.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;
import rocks.inspectit.server.anomaly.definition.classification.ClassifierDefinition;
import rocks.inspectit.server.anomaly.definition.classification.HardClassifierDefinition;
import rocks.inspectit.server.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.server.anomaly.definition.metric.InfluxDBMetricDefinition.Function;
import rocks.inspectit.server.anomaly.definition.metric.MetricDefinition;
import rocks.inspectit.server.anomaly.definition.threshold.PercentageDerivationThresholdDefinition;
import rocks.inspectit.server.anomaly.definition.threshold.ThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDetectionConfiguration {

	public static AnomalyDetectionConfiguration getTestDefinition() {
		AnomalyDetectionConfiguration configuration = new AnomalyDetectionConfiguration();
		configuration.setTimeTravelDuration(2, TimeUnit.DAYS);

		InfluxDBMetricDefinition metricDefinition = new InfluxDBMetricDefinition();
		metricDefinition.setMeasurement("businessTransactions");
		metricDefinition.setFunction(Function.MEAN);
		metricDefinition.setField("duration");
		metricDefinition.setTagMap(ImmutableMap.of("generated", "yes"));

		// #####################################

		// MovingAverageBaselineDefinition baselineDefinition = new
		// MovingAverageBaselineDefinition();
		// baselineDefinition.setWindowSize(24); // * 5min = 2h
		// baselineDefinition.setExcludeCriticalData(true);

		ExponentialMovingAverageBaselineDefinition baselineDefinition = new ExponentialMovingAverageBaselineDefinition();
		baselineDefinition.setSmoothingFactor(0.05D);
		baselineDefinition.setTrendSmoothingFactor(0.01D);
		baselineDefinition.setExcludeCriticalData(true);

		// #####################################

		// StandardDeviationThresholdDefinition thresholdDefinition = new
		// StandardDeviationThresholdDefinition();
		// thresholdDefinition.setWindowSize(24);
		// thresholdDefinition.setSigmaAmountCritical(5);
		// thresholdDefinition.setSigmaAmountWarning(3);
		// thresholdDefinition.setExcludeCriticalData(true);
		// thresholdDefinition.setExcludeWarningData(false);
		// thresholdDefinition.setExponentialSmoothed(true);
		// thresholdDefinition.setSmoothingFactor(0.01D);

		// PercentileThresholdDefinition thresholdDefinition = new PercentileThresholdDefinition();
		// thresholdDefinition.setUpperCriticalPercentile(99);
		// thresholdDefinition.setUpperWarningPercentile(90);
		// thresholdDefinition.setLowerWarningPercentile(10);
		// thresholdDefinition.setLowerCriticalPercentile(1);
		// thresholdDefinition.setExponentialSmoothed(true);
		// thresholdDefinition.setSmoothingFactor(0.1D);
		// thresholdDefinition.setWindowSize(6);

		// FixedThresholdDefinition thresholdDefinition = new FixedThresholdDefinition();
		// thresholdDefinition.setUpperCriticalThreshold(200D);
		// thresholdDefinition.setUpperWarningThreshold(150D);
		// thresholdDefinition.setLowerCriticalThreshold(50);

		PercentageDerivationThresholdDefinition thresholdDefinition = new PercentageDerivationThresholdDefinition();
		thresholdDefinition.setPercentageDerivationWarning(0.05D);
		thresholdDefinition.setPercentageDerivationCritical(0.2D);
		thresholdDefinition.setWindowSize(6);

		// #####################################

		// PercentageClassifierDefinition classifierDefinition = new
		// PercentageClassifierDefinition();
		// classifierDefinition.setPercentageWarningLevel(0.05D);
		// classifierDefinition.setPercentageCriticalLevel(0.10D);

		HardClassifierDefinition classifierDefinition = new HardClassifierDefinition();

		// #####################################

		configuration.setMetricDefinition(metricDefinition);
		configuration.setBaselineDefinition(baselineDefinition);
		configuration.setThresholdDefinition(thresholdDefinition);
		configuration.setClassifierDefinition(classifierDefinition);

		return configuration;
	}

	private String id = UUID.randomUUID().toString();

	private MetricDefinition metricDefinition;

	private BaselineDefinition baselineDefinition;

	private ThresholdDefinition thresholdDefinition;

	private ClassifierDefinition classifierDefinition;

	// = 15s * 240 = 1h
	private int warmupLength = 240;

	// 1 = 15s
	private int intervalShortProcessing = 1;

	// 20 = 15s * 20 = 5min
	private int intervalLongProcessing = 20;

	private long timeTravelDuration;

	/**
	 * Gets {@link #classifierDefinition}.
	 *
	 * @return {@link #classifierDefinition}
	 */
	public ClassifierDefinition getClassifierDefinition() {
		return this.classifierDefinition;
	}

	/**
	 * Sets {@link #classifierDefinition}.
	 *
	 * @param classifierDefinition
	 *            New value for {@link #classifierDefinition}
	 */
	public void setClassifierDefinition(ClassifierDefinition classifierDefinition) {
		this.classifierDefinition = classifierDefinition;
	}

	/**
	 * Gets {@link #timeTravelDuration}.
	 *
	 * @return {@link #timeTravelDuration}
	 */
	public long getTimeTravelDuration(TimeUnit unit) {
		return unit.convert(timeTravelDuration, TimeUnit.SECONDS);
	}

	/**
	 * Sets {@link #timeTravelDuration}.
	 *
	 * @param timeTracelDuration
	 *            New value for {@link #timeTravelDuration}
	 */
	public void setTimeTravelDuration(long timeTracelDuration, TimeUnit unit) {
		this.timeTravelDuration = unit.toSeconds(timeTracelDuration);
	}

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
	 * Gets {@link #thresholdDefinition}.
	 *
	 * @return {@link #thresholdDefinition}
	 */
	public ThresholdDefinition getThresholdDefinition() {
		return this.thresholdDefinition;
	}

	/**
	 * Sets {@link #thresholdDefinition}.
	 *
	 * @param trhesholdDefinition
	 *            New value for {@link #thresholdDefinition}
	 */
	public void setThresholdDefinition(ThresholdDefinition trhesholdDefinition) {
		this.thresholdDefinition = trhesholdDefinition;
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
	 * Gets {@link #intervalShortProcessing}.
	 *
	 * @return {@link #intervalShortProcessing}
	 */
	public int getIntervalShortProcessing() {
		return this.intervalShortProcessing;
	}

	/**
	 * Gets {@link #intervalLongProcessing}.
	 *
	 * @return {@link #intervalLongProcessing}
	 */
	public int getIntervalLongProcessing() {
		return this.intervalLongProcessing;
	}

	/**
	 * Sets {@link #intervalShortProcessing}.
	 *
	 * @param intervalShortProcessing
	 *            New value for {@link #intervalShortProcessing}
	 */
	public void setIntervalShortProcessing(int intervalShortProcessing) {
		this.intervalShortProcessing = intervalShortProcessing;
	}

	/**
	 * Sets {@link #intervalLongProcessing}.
	 *
	 * @param intervalLongProcessing
	 *            New value for {@link #intervalLongProcessing}
	 */
	public void setIntervalLongProcessing(int intervalLongProcessing) {
		this.intervalLongProcessing = intervalLongProcessing;
	}

}
