package rocks.inspectit.server.anomaly.context.matcher.impl;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.context.matcher.AbstractAnomalyContextMatcher;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl.MachineMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class MatchEverythingMatcher extends AbstractAnomalyContextMatcher<MachineMatcherConfiguration> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(DefaultData defaultData) {
		return true;
	}

}
