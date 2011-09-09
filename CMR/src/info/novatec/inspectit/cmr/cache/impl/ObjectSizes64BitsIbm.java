package info.novatec.inspectit.cmr.cache.impl;

/**
 * This class is needed because IBM JVM has a larger footprint for a {@link Object} objects. It is
 * 16 bytes, and not 8 like in Sun JVM.
 * 
 * @author Ivan Senic
 * 
 */
public class ObjectSizes64BitsIbm extends ObjectSizes64Bits {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Size of the object footprint on the IBM JVM is 16 bytes.
	 */
	public long getSizeOfObject() {
		return 16;
	}

}
