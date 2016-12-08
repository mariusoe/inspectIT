package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IBaselineProcessorConfiguration<E extends AbstractBaselineProcessor> extends IProcessorConfiguration<E> {

}
