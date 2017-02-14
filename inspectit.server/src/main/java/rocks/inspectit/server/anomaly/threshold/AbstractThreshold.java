package rocks.inspectit.server.anomaly.threshold;

import rocks.inspectit.server.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.server.anomaly.definition.threshold.ThresholdDefinition;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractThreshold<E extends ThresholdDefinition> extends AbstractDefinitionAware<E> {

	public enum ThresholdType {
		UPPER_CRITICAL, UPPER_WARNING, LOWER_WARNING, LOWER_CRITICAL;
	}

	public abstract void process(AnomalyProcessingContext context, long time);

	public abstract double getThreshold(AnomalyProcessingContext context, ThresholdType type);
}
