package rocks.inspectit.server.anomaly.baseline;

import rocks.inspectit.server.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.server.anomaly.definition.baseline.BaselineDefinition;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractBaseline<E extends BaselineDefinition> extends AbstractDefinitionAware<E> {

	public abstract void process(AnomalyProcessingContext context, long time);

	public abstract double getBaseline();
}
