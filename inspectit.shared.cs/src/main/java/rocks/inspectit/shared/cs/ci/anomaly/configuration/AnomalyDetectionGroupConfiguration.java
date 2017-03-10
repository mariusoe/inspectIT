package rocks.inspectit.shared.cs.ci.anomaly.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.anomaly.definition.anomaly.AnomalyDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HistoricalBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.NonBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.HardClassifierDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.InfluxDBMetricDefinition.Function;
import rocks.inspectit.shared.cs.ci.anomaly.definition.notification.LogNotificationDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.notification.NotificationDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.FixedThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.PercentageDerivationThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.StandardDeviationThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-group-configuration")
public class AnomalyDetectionGroupConfiguration implements Serializable {

	public static AnomalyDetectionGroupConfiguration getTestConfiguration() {
		AnomalyDetectionConfiguration configuration = new AnomalyDetectionConfiguration();

		InfluxDBMetricDefinition metricDefinition = new InfluxDBMetricDefinition();
		metricDefinition.setMeasurement("businessTransactions");
		metricDefinition.setFunction(Function.COUNT);
		metricDefinition.setField("duration");
		metricDefinition.getTags().put("generated", "yes");
		metricDefinition.setDefaultValue(0);

		// #####################################

		// MovingAverageBaselineDefinition baselineDefinition = new
		// MovingAverageBaselineDefinition();
		// baselineDefinition.setWindowSize(24); // * 5min = 2h
		// baselineDefinition.setExcludeCriticalData(true);

		// ExponentialMovingAverageBaselineDefinition baselineDefinition = new
		// ExponentialMovingAverageBaselineDefinition();
		// baselineDefinition.setSmoothingFactor(0.05D);
		// baselineDefinition.setTrendSmoothingFactor(0.01D);
		// baselineDefinition.setExcludeCriticalData(true);
		// baselineDefinition.setExcludeWarningData(true);

		// HoltWintersBaselineDefinition baselineDefinition = new HoltWintersBaselineDefinition();
		// baselineDefinition.setSmoothingFactor(0.01D);
		// baselineDefinition.setTrendSmoothingFactor(0.05D);
		// baselineDefinition.setSeasonalSmoothingFactor(0.1D);
		// baselineDefinition.setSeasonalLength(288);

		HistoricalBaselineDefinition baselineDefinition = new HistoricalBaselineDefinition();
		baselineDefinition.setSeasonLength(288);
		baselineDefinition.setSmoothingFactor(0.1D);

		// #####################################

		// StandardDeviationThresholdDefinition thresholdDefinition = new
		// StandardDeviationThresholdDefinition();
		// thresholdDefinition.setWindowSize(24);
		// thresholdDefinition.setSigmaAmountCritical(4);
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
		thresholdDefinition.setPercentageDerivationWarning(0.25D);
		thresholdDefinition.setPercentageDerivationCritical(0.50D);
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

		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		AnomalyDetectionConfiguration configurationTwo = new AnomalyDetectionConfiguration();

		InfluxDBMetricDefinition metricDefinitionTwo = new InfluxDBMetricDefinition();
		metricDefinitionTwo.setMeasurement("businessTransactions");
		metricDefinitionTwo.setFunction(Function.MEAN);
		metricDefinitionTwo.setField("duration");

		NonBaselineDefinition baselineDefinitionTwo = new NonBaselineDefinition();

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

		AnomalyDetectionConfiguration configurationThree = new AnomalyDetectionConfiguration();

		InfluxDBMetricDefinition metricDefinitionThree = new InfluxDBMetricDefinition();
		metricDefinitionThree.setMeasurement("businessTransactions");
		metricDefinitionThree.setFunction(Function.MEAN);
		metricDefinitionThree.setField("duration");
		metricDefinitionThree.setOpperateOnAggregation(false);

		HistoricalBaselineDefinition baselineDefinitionThree = new HistoricalBaselineDefinition();
		baselineDefinitionThree.setSeasonLength(288);
		baselineDefinitionThree.setSmoothingFactor(0.1D);
		baselineDefinitionThree.setSmoothValue(true);
		baselineDefinitionThree.setValueSoothingFactor(0.1D);
		baselineDefinitionThree.setTrendSmoothingFactor(0.25D);

		StandardDeviationThresholdDefinition thresholdDefinitionThree = new StandardDeviationThresholdDefinition();
		thresholdDefinitionThree.setWindowSize(24);
		thresholdDefinitionThree.setSigmaAmountCritical(4);
		thresholdDefinitionThree.setSigmaAmountWarning(3);
		thresholdDefinitionThree.setExcludeCriticalData(true);
		thresholdDefinitionThree.setExcludeWarningData(false);
		thresholdDefinitionThree.setExponentialSmoothed(true);
		thresholdDefinitionThree.setSmoothingFactor(0.01D);

		HardClassifierDefinition classifierDefinitionThree = new HardClassifierDefinition();

		configurationThree.setMetricDefinition(metricDefinitionThree);
		configurationThree.setBaselineDefinition(baselineDefinitionThree);
		configurationThree.setClassifierDefinition(classifierDefinitionThree);
		configurationThree.setThresholdDefinition(thresholdDefinitionThree);

		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		AnomalyDetectionConfiguration configurationFour = new AnomalyDetectionConfiguration();

		InfluxDBMetricDefinition metricDefinitionFour = new InfluxDBMetricDefinition();
		metricDefinitionFour.setMeasurement("businessTransactions");
		metricDefinitionFour.setFunction(Function.MEAN);
		metricDefinitionFour.setField("duration");
		metricDefinitionFour.setOpperateOnAggregation(false);
		metricDefinitionFour.getTags().put("businessTxName", "shoppingcart");

		HistoricalBaselineDefinition baselineDefinitionFour = new HistoricalBaselineDefinition();
		baselineDefinitionFour.setSeasonLength(288);
		baselineDefinitionFour.setSmoothingFactor(0.1D);

		StandardDeviationThresholdDefinition thresholdDefinitionFour = new StandardDeviationThresholdDefinition();
		thresholdDefinitionFour.setWindowSize(24);
		thresholdDefinitionFour.setSigmaAmountCritical(4);
		thresholdDefinitionFour.setSigmaAmountWarning(2);
		thresholdDefinitionFour.setExcludeCriticalData(true);
		thresholdDefinitionFour.setExcludeWarningData(false);
		thresholdDefinitionFour.setExponentialSmoothed(true);
		thresholdDefinitionFour.setSmoothingFactor(0.01D);

		HardClassifierDefinition classifierDefinitionFour = new HardClassifierDefinition();

		configurationFour.setMetricDefinition(metricDefinitionFour);
		configurationFour.setBaselineDefinition(baselineDefinitionFour);
		configurationFour.setClassifierDefinition(classifierDefinitionFour);
		configurationFour.setThresholdDefinition(thresholdDefinitionFour);

		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		AnomalyDefinition anomalyDefinition = new AnomalyDefinition();
		anomalyDefinition.setStartCount(2);
		anomalyDefinition.setEndCount(8);

		LogNotificationDefinition notificationDefinition = new LogNotificationDefinition();

		AnomalyDetectionGroupConfiguration configurationGroup = new AnomalyDetectionGroupConfiguration();
		configurationGroup.setName("test-configuration");
		configurationGroup.setMode(Mode.WORST);
		// configurationGroup.getConfigurations().add(configuration); // pct
		// configurationGroup.getConfigurations().add(configurationTwo); // fixed
		// configurationGroup.getConfigurations().add(configurationThree); // stddev
		configurationGroup.getConfigurations().add(configurationFour); // stddev
		configurationGroup.setTimeTravelDuration(7, TimeUnit.DAYS);
		configurationGroup.setFaultSuppressionFactor(10D);

		configurationGroup.setAnomalyDefinition(anomalyDefinition);
		configurationGroup.getNotificationDefinitions().add(notificationDefinition);

		return configurationGroup;
	}

	public enum Mode {
		WORST,

		BEST;
	}

	private String name;

	private Date createdDate;

	private String groupId = UUID.randomUUID().toString();

	private Mode mode = Mode.WORST;

	private final List<AnomalyDetectionConfiguration> configurations = new ArrayList<>();

	private long timeTravelDuration;

	private AnomalyDefinition anomalyDefinition;

	private final List<NotificationDefinition> notificationDefinitions = new ArrayList<>();

	private double faultSuppressionFactor;

	private boolean recordDataDuringAnomaly = true;

	/**
	 * Gets {@link #recordDataDuringAnomaly}.
	 *
	 * @return {@link #recordDataDuringAnomaly}
	 */
	public boolean isRecordDataDuringAnomaly() {
		return this.recordDataDuringAnomaly;
	}

	/**
	 * Sets {@link #recordDataDuringAnomaly}.
	 *
	 * @param recordDataDuringAnomaly
	 *            New value for {@link #recordDataDuringAnomaly}
	 */
	public void setRecordDataDuringAnomaly(boolean recordDataDuringAnomaly) {
		this.recordDataDuringAnomaly = recordDataDuringAnomaly;
	}

	/**
	 * Gets {@link #notificationDefinitions}.
	 *
	 * @return {@link #notificationDefinitions}
	 */
	public List<NotificationDefinition> getNotificationDefinitions() {
		return this.notificationDefinitions;
	}

	/**
	 * Gets {@link #createdDate}.
	 *
	 * @return {@link #createdDate}
	 */
	public Date getCreatedDate() {
		return this.createdDate;
	}

	/**
	 * Sets {@link #createdDate}.
	 *
	 * @param createdDate
	 *            New value for {@link #createdDate}
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Gets {@link #faultSuppressionFactor}.
	 *
	 * @return {@link #faultSuppressionFactor}
	 */
	public double getFaultSuppressionFactor() {
		return this.faultSuppressionFactor;
	}

	/**
	 * Sets {@link #faultSuppressionFactor}.
	 *
	 * @param faultSuppressionFactor
	 *            New value for {@link #faultSuppressionFactor}
	 */
	public void setFaultSuppressionFactor(double faultSuppressionFactor) {
		this.faultSuppressionFactor = faultSuppressionFactor;
	}

	/**
	 * Sets {@link #anomalyDefinition}.
	 *
	 * @param anomalyDefinition
	 *            New value for {@link #anomalyDefinition}
	 */
	public void setAnomalyDefinition(AnomalyDefinition anomalyDefinition) {
		this.anomalyDefinition = anomalyDefinition;
	}

	/**
	 * Gets {@link #anomalyDefinition}.
	 *
	 * @return {@link #anomalyDefinition}
	 */
	public AnomalyDefinition getAnomalyDefinition() {
		return this.anomalyDefinition;
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
	 * Gets {@link #configurations}.
	 *
	 * @return {@link #configurations}
	 */
	public List<AnomalyDetectionConfiguration> getConfigurations() {
		return this.configurations;
	}
}
