package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.classifier.IClassifyProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IClassifyProcessorConfiguration<E extends IClassifyProcessor> extends IProcessorConfiguration<E> {
}
