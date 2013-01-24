package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationAwareData;

/**
 * Comparators for the {@link InvocationAwareData}.
 * 
 * @author Ivan Senic
 * 
 */
public enum InvocationAwareDataComparatorEnum implements IDataComparator<InvocationAwareData> {

	/**
	 * Sorting by invocation affiliation percentage.
	 */
	INVOCATION_AFFILIATION;

	/**
	 * {@inheritDoc}
	 */
	public int compare(InvocationAwareData o1, InvocationAwareData o2, CachedDataService cachedDataService) {
		switch (this) {
		case INVOCATION_AFFILIATION:
			return Double.compare(o1.getInvocationAffiliationPercentage(), o2.getInvocationAffiliationPercentage());
		default:
			return 0;
		}
	}

}
