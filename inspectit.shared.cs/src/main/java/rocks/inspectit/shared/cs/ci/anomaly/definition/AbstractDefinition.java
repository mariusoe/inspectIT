package rocks.inspectit.shared.cs.ci.anomaly.definition;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HistoricalBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HoltWintersBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.MovingAverageBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.NonBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.HardClassifierDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.PercentageClassifierDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.metric.InfluxDBMetricDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.FixedThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.PercentageDerivationThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.PercentileThresholdDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.StandardDeviationThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ ExponentialMovingAverageBaselineDefinition.class, MovingAverageBaselineDefinition.class, HardClassifierDefinition.class, PercentageClassifierDefinition.class,
	InfluxDBMetricDefinition.class, FixedThresholdDefinition.class, PercentageDerivationThresholdDefinition.class, PercentileThresholdDefinition.class, StandardDeviationThresholdDefinition.class,
	NonBaselineDefinition.class, HoltWintersBaselineDefinition.class, HistoricalBaselineDefinition.class })
public abstract class AbstractDefinition implements Serializable {

	@XmlTransient
	private AnomalyDetectionConfiguration parentConfiguration;

	/**
	 * Gets {@link #parentConfiguration}.
	 *
	 * @return {@link #parentConfiguration}
	 */
	public AnomalyDetectionConfiguration getParentConfiguration() {
		return this.parentConfiguration;
	}

	/**
	 * Sets {@link #parentConfiguration}.
	 *
	 * @param parentConfiguration
	 *            New value for {@link #parentConfiguration}
	 */
	public void setParentConfiguration(AnomalyDetectionConfiguration parentConfiguration) {
		this.parentConfiguration = parentConfiguration;
	}

}
