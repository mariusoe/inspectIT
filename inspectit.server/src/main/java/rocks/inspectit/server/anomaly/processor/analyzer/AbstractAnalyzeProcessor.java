package rocks.inspectit.server.anomaly.processor.analyzer;

import rocks.inspectit.server.anomaly.configuration.model.IAnalyzeProcessorConfiguration;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.AbstractProcessor;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractAnalyzeProcessor<E extends IAnalyzeProcessorConfiguration<?>> extends AbstractProcessor<E> {

	public abstract void analyze(AnalyzableData<?> analyzable);

}
