package rocks.inspectit.server.anomaly.classification.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.HardClassifierDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class HardClassifier extends AbstractClassifier<HardClassifierDefinition> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthStatus classify(ProcessingContext context, long time) {
		if (!context.isWarmedUp()) {
			return HealthStatus.UNKNOWN;
		}

		double value = context.getCurrentValue();

		// TODO check if thresholds are provided

		if (Double.isNaN(value)) {
			return HealthStatus.UNKNOWN;
		}

		if ((context.getThreshold().getThreshold(context, ThresholdType.LOWER_CRITICAL) > value) || (context.getThreshold().getThreshold(context, ThresholdType.UPPER_CRITICAL) < value)) {
			return HealthStatus.CRITICAL;
		}

		if ((context.getThreshold().getThreshold(context, ThresholdType.LOWER_WARNING) > value) || (context.getThreshold().getThreshold(context, ThresholdType.UPPER_WARNING) < value)) {
			return HealthStatus.WARNING;
		}

		return HealthStatus.NORMAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}

}
