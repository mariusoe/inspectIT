package rocks.inspectit.server.anomaly.threshold;

import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.ThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractThreshold<E extends ThresholdDefinition> extends AbstractDefinitionAware<E> {

	public enum ThresholdType {
		UPPER_CRITICAL, UPPER_WARNING, LOWER_WARNING, LOWER_CRITICAL;
	}

	public abstract void process(ProcessingUnitContext context, long time);

	public abstract boolean providesThreshold(ThresholdType type);

	public abstract double getThreshold(ProcessingUnitContext context, ThresholdType type);
}
