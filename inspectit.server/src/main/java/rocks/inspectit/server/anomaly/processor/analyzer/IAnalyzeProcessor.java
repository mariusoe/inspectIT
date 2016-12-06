package rocks.inspectit.server.anomaly.processor.analyzer;

import rocks.inspectit.server.anomaly.configuration.model.IAnalyzeProcessorConfiguration;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.IProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IAnalyzeProcessor extends IProcessor<IAnalyzeProcessorConfiguration<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	void setConfiguration(IAnalyzeProcessorConfiguration<?> configuration);

	void analyze(AnalyzableData<?> analyzable);
}
