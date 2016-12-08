package rocks.inspectit.server.anomaly.processor.baseline.impl;

import rocks.inspectit.server.anomaly.configuration.model.IBaselineProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyBaselineProcessor extends AbstractBaselineProcessor<DummyBaselineProcessor.Configuration> {

	public static class Configuration implements IBaselineProcessorConfiguration<DummyBaselineProcessor> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<DummyBaselineProcessor> getProcessorClass() {
			return DummyBaselineProcessor.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AnomalyContext context) {
		// TODO Auto-generated method stub

	}

}
