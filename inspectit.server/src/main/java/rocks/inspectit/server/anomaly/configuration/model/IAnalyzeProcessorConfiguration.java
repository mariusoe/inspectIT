package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IAnalyzeProcessorConfiguration<E extends AbstractAnalyzeProcessor> extends IProcessorConfiguration<E> {
}
