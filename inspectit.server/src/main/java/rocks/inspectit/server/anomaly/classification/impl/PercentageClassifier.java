package rocks.inspectit.server.anomaly.classification.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.AnomalyDetectionSystem;
import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.metric.MetricFilter;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.PercentageClassifierDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class PercentageClassifier extends AbstractClassifier<PercentageClassifierDefinition> {

	@Log
	private Logger log;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthStatus classify(ProcessingUnitContext context, long time) {
		if (!context.isWarmedUp()) {
			return HealthStatus.UNKNOWN;
		}

		long timeWindow = context.getConfiguration().getIntervalShortProcessing() * AnomalyDetectionSystem.PROCESSING_INTERVAL_SECONDS;

		long totalCount = context.getMetricProvider().getCount(time, timeWindow, TimeUnit.SECONDS);

		if (totalCount <= 0) {
			// TODO
			return HealthStatus.UNKNOWN;
		}

		MetricFilter filter = new MetricFilter(Double.NaN, context.getThreshold().getThreshold(context, ThresholdType.UPPER_CRITICAL));
		long upperCriticalCount = context.getMetricProvider().getCount(filter, time, timeWindow, TimeUnit.SECONDS);

		filter = new MetricFilter(context.getThreshold().getThreshold(context, ThresholdType.LOWER_CRITICAL), Double.NaN);
		long lowerCriticalCount = context.getMetricProvider().getCount(filter, time, timeWindow, TimeUnit.SECONDS);

		long criticalCount = upperCriticalCount + lowerCriticalCount;

		double problemRatio = (1D / totalCount) * criticalCount;

		if (!Double.isNaN(getDefinition().getPercentageCriticalLevel())) {
			if (problemRatio >= getDefinition().getPercentageCriticalLevel()) {
				return HealthStatus.CRITICAL;
			}
		}

		if (!Double.isNaN(getDefinition().getPercentageWarningLevel())) {
			if (problemRatio >= getDefinition().getPercentageWarningLevel()) {
				return HealthStatus.WARNING;
			}
		}

		return HealthStatus.NORMAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
		// TODO Auto-generated method stub

	}
}
