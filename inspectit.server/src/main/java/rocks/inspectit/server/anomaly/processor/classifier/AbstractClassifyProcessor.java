package rocks.inspectit.server.anomaly.processor.classifier;

import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.AbstractProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractClassifyProcessor<E extends AbstractClassifyProcessorConfiguration> extends AbstractProcessor<E> {

	public abstract void classify(AnomalyContext context, AnalyzableData<?> analyzable);
}
