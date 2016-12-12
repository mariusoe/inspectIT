package rocks.inspectit.server.anomaly.context.matcher.impl;

import rocks.inspectit.server.anomaly.context.matcher.AbstractAnomalyContextMatcher;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl.MachineMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
public class MachineMatcher extends AbstractAnomalyContextMatcher<MachineMatcherConfiguration> {

	private ICachedDataService cachedDataService;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(DefaultData defaultData) {
		// if (cachedDataService == null) {
		// if (CMR.getBeanFactory() == null) {
		// return false;
		// }
		// cachedDataService = CMR.getBeanFactory().getBean(ICachedDataService.class);
		// }
		PlatformIdent platformIdent = cachedDataService.getPlatformIdentForId(defaultData.getPlatformIdent());

		return platformIdent.getAgentName().equals(configuration.getAgentName());
	}
}
