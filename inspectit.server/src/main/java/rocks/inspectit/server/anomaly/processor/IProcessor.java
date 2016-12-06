package rocks.inspectit.server.anomaly.processor;

import rocks.inspectit.server.anomaly.configuration.model.IProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public interface IProcessor<E extends IProcessorConfiguration<?>> {

	void setConfiguration(E configuration);
}
