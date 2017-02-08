package rocks.inspectit.server.anomaly.baseline;

import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.BaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractBaseline<E extends BaselineDefinition> extends AbstractDefinitionAware<E> {

	public abstract void process(ProcessingUnitContext context, long time);

	public abstract double getBaseline();
}
