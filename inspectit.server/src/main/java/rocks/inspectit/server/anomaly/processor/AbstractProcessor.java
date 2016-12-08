package rocks.inspectit.server.anomaly.processor;

import rocks.inspectit.server.anomaly.configuration.model.IProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractProcessor<E extends IProcessorConfiguration<?>> {

	protected E configuration;

	public void setConfiguration(E configuration) {
		this.configuration = configuration;
	}
}
