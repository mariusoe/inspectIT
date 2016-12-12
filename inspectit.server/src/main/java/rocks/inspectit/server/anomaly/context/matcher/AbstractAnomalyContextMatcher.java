package rocks.inspectit.server.anomaly.context.matcher;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.AbstractContextMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractAnomalyContextMatcher<E extends AbstractContextMatcherConfiguration> {

	protected E configuration;

	public void setConfiguration(E configuration) {
		this.configuration = configuration;
	}

	public abstract boolean matches(DefaultData defaultData);
}
