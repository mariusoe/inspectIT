package rocks.inspectit.server.anomaly.configuration.model;

import rocks.inspectit.server.anomaly.processor.analyzer.IAnalyzeProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IAnalyzeProcessorConfiguration<E extends IAnalyzeProcessor> extends IProcessorConfiguration<E> {
}
