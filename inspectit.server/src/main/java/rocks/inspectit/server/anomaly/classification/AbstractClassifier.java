package rocks.inspectit.server.anomaly.classification;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.classification.ClassifierDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractClassifier<E extends ClassifierDefinition> extends AbstractDefinitionAware<E> {

	public abstract HealthStatus classify(ProcessingContext context, long time);

}