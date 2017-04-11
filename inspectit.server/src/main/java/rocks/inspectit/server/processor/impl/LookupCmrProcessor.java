package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.util.lookup.CountryLookupUtil;
import rocks.inspectit.server.util.lookup.Network;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;

/**
 * @author Marius Oehler
 *
 */
public class LookupCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The {@link CountryLookupUtil}.
	 */
	@Autowired
	private CountryLookupUtil countryLookupUtil;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData data = (InvocationSequenceData) defaultData;

		if (InvocationSequenceDataHelper.hasHttpTimerData(data)) {
			HttpInfo httpInfo = ((HttpTimerData) data.getTimerData()).getHttpInfo();
			String remoteAddress = httpInfo.getRemoteAddress();
			if (null != remoteAddress) {
				Network network = countryLookupUtil.lookup(remoteAddress);
				if ((network != null) && (network.getCountry() != null)) {
					httpInfo.setCountryCode(network.getCountry().getIsoCode());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
