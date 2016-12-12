package rocks.inspectit.server.anomaly.context.matcher.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
public class BusinessTransactionMatcher extends AbstractAnomalyContextMatcher<BusinessTransactionMatcherConfiguration> {

	@Autowired
	private IBusinessContextManagementService businessService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(DefaultData defaultData) {
		if (defaultData instanceof InvocationSequenceData) {
			// if (businessService == null) {
			// if (CMR.getBeanFactory() == null) {
			// return false;
			// }
			// businessService =
			// CMR.getBeanFactory().getBean(IBusinessContextManagementService.class);
			// }
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
