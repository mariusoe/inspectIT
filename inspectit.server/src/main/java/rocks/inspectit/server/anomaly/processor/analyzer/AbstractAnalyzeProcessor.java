package rocks.inspectit.server.anomaly.processor.analyzer;

import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.AbstractProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractAnalyzeProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractAnalyzeProcessor<E extends AbstractAnalyzeProcessorConfiguration> extends AbstractProcessor<E>
{

	public abstract void analyze(AnalyzableData<?> analyzable);

}
