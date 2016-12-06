package rocks.inspectit.server.anomaly.processor.classifier;

import rocks.inspectit.server.anomaly.configuration.model.IClassifyProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.IProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IClassifyProcessor extends IProcessor<IClassifyProcessorConfiguration<?>> {

	public abstract void classify(AnomalyContext context, AnalyzableData<?> analyzable);

	public abstract double[] getBoundaries(AnomalyContext context);
}
