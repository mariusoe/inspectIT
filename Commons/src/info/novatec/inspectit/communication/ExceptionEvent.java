package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

/**
 * Enumeration for the exception events.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public enum ExceptionEvent implements Sizeable {

	CREATED, RETHROWN, PASSED, HANDLED, UNREGISTERED_PASSED; // NOCHK

	/**
	 * Utility method to convert an ordinal value into the respective enumeration. Used e.g. for
	 * hibernate.
	 * 
	 * @param i
	 *            the ordinal value.
	 * @return the exception event
	 */
	public static ExceptionEvent fromOrd(int i) {
		if (i < 0 || i >= ExceptionEvent.values().length) {
			throw new IndexOutOfBoundsException("Invalid ordinal");
		}
		return ExceptionEvent.values()[i];
	}

	/**
	 * Returns the approximate size of the object in the memory in bytes.
	 * 
	 * @param objectSizes
	 *            Appropriate instance of {@link IObjectSizes} depending on the VM architecture.
	 * @return Approximate object size in bytes.
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(1, 0, 1, 0, 0, 0);
		size += objectSizes.getSizeOf(name());
		return objectSizes.alignTo8Bytes(size);
	}

}
