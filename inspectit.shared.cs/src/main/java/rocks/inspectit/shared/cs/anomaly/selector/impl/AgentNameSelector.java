package rocks.inspectit.shared.cs.anomaly.selector.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.anomaly.selector.AbstractDataSelector;
import rocks.inspectit.shared.cs.anomaly.selector.IDataSelectorConfiguration;
import rocks.inspectit.shared.cs.anomaly.selector.impl.AgentNameSelector.AgentNameSelectorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentNameSelector extends AbstractDataSelector<DefaultData, AgentNameSelectorConfiguration> {

	public static class AgentNameSelectorConfiguration implements IDataSelectorConfiguration {

		private String targetAgentName;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<? extends AbstractDataSelector<? extends DefaultData, ?>> getDataSelectorClass() {
			return AgentNameSelector.class;
		}

		/**
		 * Gets {@link #targetAgentName}.
		 *
		 * @return {@link #targetAgentName}
		 */
		public String getTargetAgentName() {
			return this.targetAgentName;
		}

		/**
		 * Sets {@link #targetAgentName}.
		 *
		 * @param targetAgentName
		 *            New value for {@link #targetAgentName}
		 */
		public void setTargetAgentName(String targetAgentName) {
			this.targetAgentName = targetAgentName;
		}

	}

	@Autowired
	private ICachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean select(DefaultData defaultData) {
		String agentName = cachedDataService.getPlatformIdentForId(defaultData.getPlatformIdent()).getAgentName();

		return getConfiguration().targetAgentName.equals(agentName);
	}
}
