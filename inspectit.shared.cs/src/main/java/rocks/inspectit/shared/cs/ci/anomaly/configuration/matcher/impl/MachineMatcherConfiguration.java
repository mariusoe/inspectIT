package rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyContextMatcher;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.AbstractContextMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class MachineMatcherConfiguration extends AbstractContextMatcherConfiguration {

	private String agentName;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnomalyContextMatcher getContextMatcher() {
		return AnomalyContextMatcher.MACHINE_MATCHER;
	}

	/**
	 * Gets {@link #agentName}.
	 * 
	 * @return {@link #agentName}
	 */
	public String getAgentName() {
		return this.agentName;
	}

	/**
	 * Sets {@link #agentName}.
	 * 
	 * @param agentName
	 *            New value for {@link #agentName}
	 */
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

}
