package rocks.inspectit.shared.cs.ci.anomaly.definition;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.MovingAverageBaselineDefinition;
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
		InfluxDBMetricDefinition.class, FixedThresholdDefinition.class, PercentageDerivationThresholdDefinition.class, PercentileThresholdDefinition.class,
		StandardDeviationThresholdDefinition.class })
public abstract class AbstractDefinition implements Serializable {

}