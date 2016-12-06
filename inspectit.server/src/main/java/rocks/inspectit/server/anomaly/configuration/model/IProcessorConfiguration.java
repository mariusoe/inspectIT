package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.IProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IProcessorConfiguration<E extends IProcessor<?>> {

	Class<E> getProcessorClass();

}
