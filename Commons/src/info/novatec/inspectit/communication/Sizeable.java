package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

/**
 * Interface for all objects that can report its size.
 * 
 * @author Ivan Senic
 * 
 */
public interface Sizeable {

	/**
	 * Returns the approximate size of the object in the memory in bytes.
	 * 
	 * @param objectSizes
	 *            Appropriate instance of {@link IObjectSizes} depending on the VM architecture.
	 * @return Approximate object size in bytes.
	 */
	long getObjectSize(IObjectSizes objectSizes);
}
