package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.baseline.IBaselineProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IBaselineProcessorConfiguration<E extends IBaselineProcessor> extends IProcessorConfiguration<E> {

}
