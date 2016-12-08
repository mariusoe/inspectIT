package rocks.inspectit.server.anomaly.context.matcher.impl;

import rocks.inspectit.server.CMR;
import rocks.inspectit.server.anomaly.context.matcher.IAnomalyContextMatcher;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
public class MachineMatcher implements IAnomalyContextMatcher {

	private ICachedDataService cachedDataService;

	private String agentNamePattern;

	/**
	 * @param agentNamePattern
	 */
	public MachineMatcher(String agentNamePattern) {
		this.agentNamePattern = agentNamePattern;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(DefaultData defaultData) {
		if (cachedDataService == null) {
			if (CMR.getBeanFactory() == null) {
				return false;
			}
			cachedDataService = CMR.getBeanFactory().getBean(ICachedDataService.class);
		}

		PlatformIdent platformIdent = cachedDataService.getPlatformIdentForId(defaultData.getPlatformIdent());

		return platformIdent.getAgentName().equals(agentNamePattern);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAnomalyContextMatcher createCopy() {
		return null;
	}

}
