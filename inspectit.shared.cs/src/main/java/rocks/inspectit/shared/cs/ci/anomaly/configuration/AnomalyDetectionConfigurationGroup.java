package rocks.inspectit.shared.cs.ci.anomaly.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.MovingAverageBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.HardClassifierDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.InfluxDBMetricDefinition.Function;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.FixedThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.StandardDeviationThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-configuration-group")
public class AnomalyDetectionConfigurationGroup implements Serializable {

	public static AnomalyDetectionConfigurationGroup getTestConfiguration() {
		AnomalyDetectionConfiguration configuration = new AnomalyDetectionConfiguration();

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
		baselineDefinition.setExcludeWarningData(true);

		// #####################################

		StandardDeviationThresholdDefinition thresholdDefinition = new StandardDeviationThresholdDefinition();
		thresholdDefinition.setWindowSize(24);
		thresholdDefinition.setSigmaAmountCritical(4);
		thresholdDefinition.setSigmaAmountWarning(3);
		thresholdDefinition.setExcludeCriticalData(true);
		thresholdDefinition.setExcludeWarningData(false);
		thresholdDefinition.setExponentialSmoothed(true);
		thresholdDefinition.setSmoothingFactor(0.01D);

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

		// PercentageDerivationThresholdDefinition thresholdDefinition = new
		// PercentageDerivationThresholdDefinition();
		// thresholdDefinition.setPercentageDerivationWarning(0.05D);
		// thresholdDefinition.setPercentageDerivationCritical(0.2D);
		// thresholdDefinition.setWindowSize(6);

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

		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		AnomalyDetectionConfiguration configurationTwo = new AnomalyDetectionConfiguration();

		InfluxDBMetricDefinition metricDefinitionTwo = new InfluxDBMetricDefinition();
		metricDefinitionTwo.setMeasurement("businessTransactions");
		metricDefinitionTwo.setFunction(Function.MEAN);
		metricDefinitionTwo.setField("duration");

		MovingAverageBaselineDefinition baselineDefinitionTwo = new MovingAverageBaselineDefinition();
		baselineDefinitionTwo.setWindowSize(24); // * 5min = 2h
		baselineDefinitionTwo.setExcludeCriticalData(false);

		FixedThresholdDefinition thresholdDefinitionTwo = new FixedThresholdDefinition();
		thresholdDefinitionTwo.setUpperWarningThreshold(100);
		thresholdDefinitionTwo.setUpperCriticalThreshold(125);

		HardClassifierDefinition classifierDefinitionTwo = new HardClassifierDefinition();

		configurationTwo.setMetricDefinition(metricDefinitionTwo);
		configurationTwo.setBaselineDefinition(baselineDefinitionTwo);
		configurationTwo.setClassifierDefinition(classifierDefinitionTwo);
		configurationTwo.setThresholdDefinition(thresholdDefinitionTwo);

		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		AnomalyDetectionConfigurationGroup configurationGroup = new AnomalyDetectionConfigurationGroup();
		configurationGroup.setName("test-configuration");
		configurationGroup.setMode(Mode.WORST);
		configurationGroup.getConfigurations().add(configuration);
		configurationGroup.getConfigurations().add(configurationTwo);
		configurationGroup.setTimeTravelDuration(7, TimeUnit.DAYS);

		configurationGroup.setAnomalyStartCount(2);
		configurationGroup.setAnomalyEndCount(8);

		return configurationGroup;
	}

	public enum Mode {
		WORST,

		BEST;
	}

	private String name;

	private String groupId = UUID.randomUUID().toString();

	private Mode mode = Mode.WORST;

	private List<AnomalyDetectionConfigurationGroup> configurationGroups = new ArrayList<>();

	private List<AnomalyDetectionConfiguration> configurations = new ArrayList<>();

	private long timeTravelDuration;

	private int anomalyStartCount;

	private int anomalyEndCount;

	/**
	 * Gets {@link #anomalyEndCount}.
	 *
	 * @return {@link #anomalyEndCount}
	 */
	public int getAnomalyEndCount() {
		return this.anomalyEndCount;
	}

	/**
	 * Sets {@link #anomalyEndCount}.
	 *
	 * @param anomalyEndCount
	 *            New value for {@link #anomalyEndCount}
	 */
	public void setAnomalyEndCount(int anomalyEndCount) {
		this.anomalyEndCount = anomalyEndCount;
	}

	/**
	 * Gets {@link #anomalyStartCount}.
	 *
	 * @return {@link #anomalyStartCount}
	 */
	public int getAnomalyStartCount() {
		return this.anomalyStartCount;
	}

	/**
	 * Sets {@link #anomalyStartCount}.
	 *
	 * @param anomalyStartCount
	 *            New value for {@link #anomalyStartCount}
	 */
	public void setAnomalyStartCount(int anomalyStartCount) {
		this.anomalyStartCount = anomalyStartCount;
	}

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #groupId}.
	 *
	 * @return {@link #groupId}
	 */
	public String getGroupId() {
		return this.groupId;
	}

	/**
	 * Sets {@link #groupId}.
	 *
	 * @param id
	 *            New value for {@link #groupId}
	 */
	public void setGroupId(String id) {
		this.groupId = id;
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
	 * Gets {@link #mode}.
	 *
	 * @return {@link #mode}
	 */
	public Mode getMode() {
		return this.mode;
	}

	/**
	 * Sets {@link #mode}.
	 *
	 * @param mode
	 *            New value for {@link #mode}
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * Gets {@link #configurationGroups}.
	 *
	 * @return {@link #configurationGroups}
	 */
	public List<AnomalyDetectionConfigurationGroup> getConfigurationGroups() {
		return this.configurationGroups;
	}

	/**
	 * Gets {@link #configurations}.
	 *
	 * @return {@link #configurations}
	 */
	public List<AnomalyDetectionConfiguration> getConfigurations() {
		return this.configurations;
	}
}
