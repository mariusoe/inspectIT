package rocks.inspectit.server.anomaly.processor.baseline;

import rocks.inspectit.server.anomaly.configuration.model.IBaselineProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.processor.IProcessor;

/**
 * @author Marius Oehler
 *
 */
public interface IBaselineProcessor extends IProcessor<IBaselineProcessorConfiguration<?>> {

	void process(AnomalyContext context);

}
