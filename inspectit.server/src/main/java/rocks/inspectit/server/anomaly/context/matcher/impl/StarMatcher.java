package rocks.inspectit.server.anomaly.context.matcher.impl;

import rocks.inspectit.server.anomaly.context.matcher.IAnomalyContextMatcher;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
public class StarMatcher implements IAnomalyContextMatcher {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(DefaultData defaultData) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAnomalyContextMatcher createCopy() {
		return new StarMatcher();
	}

}
