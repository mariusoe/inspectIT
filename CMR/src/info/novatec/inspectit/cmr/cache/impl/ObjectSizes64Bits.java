package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.AbstractObjectSizes;
import info.novatec.inspectit.cmr.cache.IObjectSizes;

/**
 * This class provides a implementation of {@link IObjectSizes} appropriate for calculations of
 * object sizes on 64-bit Sun VM. Works only with Java 7.
 * 
 * @author Ivan Senic
 * 
 */
public class ObjectSizes64Bits extends AbstractObjectSizes {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getReferenceSize() {
		return 8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfObjectHeader() {
		return 12;
	}
}
