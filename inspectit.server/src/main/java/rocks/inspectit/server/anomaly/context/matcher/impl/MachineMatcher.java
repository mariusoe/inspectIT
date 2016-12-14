package rocks.inspectit.server.anomaly.context.matcher.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.context.matcher.AbstractAnomalyContextMatcher;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl.MachineMatcherConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class MachineMatcher extends AbstractAnomalyContextMatcher<MachineMatcherConfiguration> {

	@Autowired
	private ICachedDataService cachedDataService;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(DefaultData defaultData) {
		PlatformIdent platformIdent = cachedDataService.getPlatformIdentForId(defaultData.getPlatformIdent());

		return platformIdent.getAgentName().equals(configuration.getAgentName());
	}
}
