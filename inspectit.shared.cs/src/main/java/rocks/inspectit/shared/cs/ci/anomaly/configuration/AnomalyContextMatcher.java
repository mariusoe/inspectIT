package rocks.inspectit.shared.cs.ci.anomaly.configuration;

/**
 * @author Marius Oehler
 *
 */
public enum AnomalyContextMatcher {

	BUSINESS_TRANSACTION_MATCHER("rocks.inspectit.server.anomaly.context.matcher.impl.BusinessTransactionMatcher"),

	MACHINE_MATCHER("rocks.inspectit.server.anomaly.context.matcher.impl.MachineMatcher"),

	MATCH_EVERYTHING_MATCHER("rocks.inspectit.server.anomaly.context.matcher.impl.MatchEverythingMatcher");

	private String fqn;

	AnomalyContextMatcher(String fqn) {
		this.fqn = fqn;
	}

	/**
	 * Gets {@link #fqn}.
	 *
	 * @return {@link #fqn}
	 */
	public String getFqn() {
		return this.fqn;
	}

}
