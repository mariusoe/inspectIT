package rocks.inspectit.server.anomaly.processor.analyzer.impl;

import rocks.inspectit.server.anomaly.configuration.model.IAnalyzeProcessorConfiguration;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyAnalyzeProcessor extends AbstractAnalyzeProcessor<DummyAnalyzeProcessor.Configuration> {

	public static class Configuration implements IAnalyzeProcessorConfiguration<DummyAnalyzeProcessor> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<DummyAnalyzeProcessor> getProcessorClass() {
			return DummyAnalyzeProcessor.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void analyze(AnalyzableData<?> analyzable) {
		// TODO Auto-generated method stub
	}


}
