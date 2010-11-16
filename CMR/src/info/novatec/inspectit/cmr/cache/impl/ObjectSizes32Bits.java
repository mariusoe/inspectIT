package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.AbstractObjectSizes;
import info.novatec.inspectit.cmr.cache.IObjectSizes;

/**
 * This class provides a implementation of {@link IObjectSizes} appropriate for calculations of
 * object sizes on 32-bit VM.
 * 
 * @author Ivan Senic
 * 
 */
public class ObjectSizes32Bits extends AbstractObjectSizes {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getReferenceSize() {
		return 4;
	}

}
