package rocks.inspectit.server.anomaly.context.matcher.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.context.matcher.AbstractAnomalyContextMatcher;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.impl.BusinessTransactionMatcherConfiguration;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class BusinessTransactionMatcher extends AbstractAnomalyContextMatcher<BusinessTransactionMatcherConfiguration> {

	@Autowired
	private IBusinessContextManagementService businessService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(DefaultData defaultData) {
		if (defaultData instanceof InvocationSequenceData) {
			InvocationSequenceData invocationSequence = ((InvocationSequenceData) defaultData);
			BusinessTransactionData btxData = businessService.getBusinessTransactionForId(invocationSequence.getApplicationId(), invocationSequence.getBusinessTransactionId());

			if ((btxData == null) || StringUtils.isEmpty(btxData.getName())) {
				return false;
			}

			return configuration.getBuisnessTransactionPattern().equals(btxData.getName());
		}

		return false;
	}
}
