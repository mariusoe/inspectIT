package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.AbstractProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IProcessorConfiguration<E extends AbstractProcessor<?>> {

	Class<E> getProcessorClass();

}
