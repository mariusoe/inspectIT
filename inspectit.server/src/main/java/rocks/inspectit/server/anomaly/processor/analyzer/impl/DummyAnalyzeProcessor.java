package rocks.inspectit.server.anomaly.processor.analyzer.impl;

import rocks.inspectit.server.anomaly.configuration.model.IAnalyzeProcessorConfiguration;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.analyzer.IAnalyzeProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyAnalyzeProcessor implements IAnalyzeProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConfiguration(IAnalyzeProcessorConfiguration<?> configuration) {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void analyze(AnalyzableData<?> analyzable) {
		// TODO Auto-generated method stub

	}

}
