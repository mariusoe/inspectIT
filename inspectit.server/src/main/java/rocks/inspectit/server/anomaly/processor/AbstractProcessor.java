package rocks.inspectit.server.anomaly.processor;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractProcessor<E extends AbstractProcessorConfiguration> {

	protected E configuration;

	public void setConfiguration(E configuration) {
		this.configuration = configuration;
	}
}
