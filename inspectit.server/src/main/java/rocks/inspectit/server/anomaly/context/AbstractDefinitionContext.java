package rocks.inspectit.server.anomaly.context;

import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinition;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractDefinitionContext<E extends AbstractDefinition> extends AbstractDefinitionAware<E> {

	public abstract void initialize();

}
