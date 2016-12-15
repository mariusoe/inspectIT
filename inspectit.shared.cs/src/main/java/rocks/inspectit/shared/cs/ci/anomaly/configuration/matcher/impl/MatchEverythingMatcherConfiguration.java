package rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyContextMatcher;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.AbstractContextMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class MatchEverythingMatcherConfiguration extends AbstractContextMatcherConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnomalyContextMatcher getContextMatcher() {
		return AnomalyContextMatcher.MATCH_EVERYTHING_MATCHER;
	}

}
