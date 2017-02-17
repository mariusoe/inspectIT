package rocks.inspectit.shared.cs.ci.anomaly.configuration;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.BaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.ClassifierDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.MetricDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.ThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-configuration")
public class AnomalyDetectionConfiguration implements Serializable {

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
