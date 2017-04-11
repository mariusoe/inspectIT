package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;

/**
 * Privacy data processor which executes privacy settings.
 *
 * @author Marius Oehler
 *
 */
public class PrivacyCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * Whether IP addresses should be stored.
	 */
	@Value("${privacy.storeIps}")
	boolean storeIpAddress;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData data = (InvocationSequenceData) defaultData;

		if (!storeIpAddress && InvocationSequenceDataHelper.hasHttpTimerData(data)) {
			removeIpAddress(data);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

	/**
	 * Removes the IP address of the {@link HttpInfo} if it is existing.
	 *
	 * @param data
	 *            the data where the IP address is removed
	 */
	private void removeIpAddress(InvocationSequenceData data) {
		HttpInfo httpInfo = ((HttpTimerData) data.getTimerData()).getHttpInfo();

		if (httpInfo != null) {
			httpInfo.setRemoteAddress(null);
		}
	}
}
