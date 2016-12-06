package rocks.inspectit.server.anomaly.configuration.model.classifier;

import rocks.inspectit.server.anomaly.configuration.model.IClassifyProcessorConfiguration;
import rocks.inspectit.server.anomaly.processor.classifier.impl.DummyClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public class DummyClassifyProcessorConfiguration implements IClassifyProcessorConfiguration<DummyClassifyProcessor> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<DummyClassifyProcessor> getProcessorClass() {
		return DummyClassifyProcessor.class;
	}

}
