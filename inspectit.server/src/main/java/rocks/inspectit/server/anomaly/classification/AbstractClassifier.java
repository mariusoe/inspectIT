package rocks.inspectit.server.anomaly.classification;

import rocks.inspectit.server.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.server.anomaly.definition.classification.ClassifierDefinition;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractClassifier<E extends ClassifierDefinition> extends AbstractDefinitionAware<E> {

	public abstract HealthStatus classify(AnomalyProcessingContext context, long time);

}
