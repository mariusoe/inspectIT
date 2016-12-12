package rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyContextMatcher;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.AbstractContextMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class BusinessTransactionMatcherConfiguration extends AbstractContextMatcherConfiguration {

	private String buisnessTransactionPattern;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnomalyContextMatcher getContextMatcher() {
		return AnomalyContextMatcher.BUSINESS_TRANSACTION_MATCHER;
	}

	/**
	 * Gets {@link #buisnessTransactionPattern}.
	 *
	 * @return {@link #buisnessTransactionPattern}
	 */
	public String getBuisnessTransactionPattern() {
		return this.buisnessTransactionPattern;
	}

	/**
	 * Sets {@link #buisnessTransactionPattern}.
	 *
	 * @param buisnessTransactionPattern
	 *            New value for {@link #buisnessTransactionPattern}
	 */
	public void setBuisnessTransactionPattern(String buisnessTransactionPattern) {
		this.buisnessTransactionPattern = buisnessTransactionPattern;
	}
}
