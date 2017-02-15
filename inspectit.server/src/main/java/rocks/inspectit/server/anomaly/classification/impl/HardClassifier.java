package rocks.inspectit.server.anomaly.classification.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.classification.HealthStatus;
import rocks.inspectit.server.anomaly.definition.classification.HardClassifierDefinition;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;

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
	public HealthStatus classify(AnomalyProcessingContext context, long time) {
		if (!context.isWarmedUp()) {
			return HealthStatus.UNKNOWN;
		}

		long timeWindow = context.getConfiguration().getIntervalShortProcessing() * AnomalyDetectionSystem.PROCESSING_INTERVAL_S;

		double value = context.getMetricProvider().getValue(time, timeWindow, TimeUnit.SECONDS);

		// TODO check if thresholds are provided

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
