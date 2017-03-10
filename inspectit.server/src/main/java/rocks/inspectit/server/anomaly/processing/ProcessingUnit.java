package rocks.inspectit.server.anomaly.processing;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.classification.AbstractClassifier;
import rocks.inspectit.server.anomaly.metric.AbstractMetricProvider;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class ProcessingUnit {

	@Log
	private Logger log;

	private final ProcessingUnitContext context;

	/**
	 * @param groupContext
	 */
	@Autowired
	public ProcessingUnit(ProcessingUnitContext unitContext) {
		context = unitContext;
	}

	/**
	 * Gets {@link #context}.
	 *
	 * @return {@link #context}
	 */
	public ProcessingUnitContext getContext() {
		return this.context;
	}

	public void process(long time) {
		int shortInterval = context.getConfiguration().getIntervalShortProcessing();
		int longInterval = context.getConfiguration().getIntervalShortProcessing() * context.getConfiguration().getIntervalLongProcessingMultiplier();

		if ((context.getIterationCounter() % shortInterval) == 0) {
			context.getMetricProvider().next(context, time);

			if ((context.getIterationCounter() % longInterval) == 0) {
				processLong(time);
			}

			processShort(time);
		}

		context.incrementInterationCounter();
	}

	private void processShort(long time) {
		HealthStatus status = context.getClassifier().classify(context, time);
		context.setHealthStatus(status);
	}

	private void processLong(long time) {
		context.getBaseline().process(context, time);
		context.getThreshold().process(context, time);
	}

	/**
	 * @param metricProvider
	 */
	public void setMetricProvider(AbstractMetricProvider<?> metricProvider) {
		context.setMetricProvider(metricProvider);
	}

	/**
	 * @param baseline
	 */
	public void setBaseline(AbstractBaseline<?> baseline) {
		context.setBaseline(baseline);
	}

	/**
	 * @param threshold
	 */
	public void setThreshold(AbstractThreshold<?> threshold) {
		context.setThreshold(threshold);
	}

	/**
	 * @param classifier
	 */
	public void setClassifier(AbstractClassifier<?> classifier) {
		context.setClassifier(classifier);
	}

	public HealthStatus getHealthStatus() {
		return context.getHealthStatus();
	}
}
