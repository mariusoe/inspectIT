package rocks.inspectit.server.anomaly.processor.baseline;

import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.processor.AbstractProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractBaselineProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractBaselineProcessor<E extends AbstractBaselineProcessorConfiguration> extends AbstractProcessor<E> {

	public abstract void process(AnomalyContext context);

}
