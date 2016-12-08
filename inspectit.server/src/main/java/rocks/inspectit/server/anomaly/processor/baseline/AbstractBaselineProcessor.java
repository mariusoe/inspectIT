package rocks.inspectit.server.anomaly.processor.baseline;

import rocks.inspectit.server.anomaly.configuration.model.IBaselineProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.processor.AbstractProcessor;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractBaselineProcessor<E extends IBaselineProcessorConfiguration<?>> extends AbstractProcessor<E> {

	public abstract void process(AnomalyContext context);

}
