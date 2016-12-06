package rocks.inspectit.server.anomaly.configuration.model.baseline;

import rocks.inspectit.server.anomaly.configuration.model.IBaselineProcessorConfiguration;
import rocks.inspectit.server.anomaly.processor.baseline.impl.DummyBaselineProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyBaselineProcessorConfiguration implements IBaselineProcessorConfiguration<DummyBaselineProcessor> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<DummyBaselineProcessor> getProcessorClass() {
		return DummyBaselineProcessor.class;
	}

}
