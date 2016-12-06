package rocks.inspectit.server.anomaly.configuration.model.analyzer;

import rocks.inspectit.server.anomaly.configuration.model.IAnalyzeProcessorConfiguration;
import rocks.inspectit.server.anomaly.processor.analyzer.impl.DummyAnalyzeProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyAnalyzeProcessorConfiguration implements IAnalyzeProcessorConfiguration<DummyAnalyzeProcessor> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<DummyAnalyzeProcessor> getProcessorClass() {
		return DummyAnalyzeProcessor.class;
	}

}
