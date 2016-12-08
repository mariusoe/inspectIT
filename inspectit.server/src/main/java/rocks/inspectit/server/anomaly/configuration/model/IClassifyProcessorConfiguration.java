package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IClassifyProcessorConfiguration<E extends AbstractClassifyProcessor> extends IProcessorConfiguration<E> {
}
