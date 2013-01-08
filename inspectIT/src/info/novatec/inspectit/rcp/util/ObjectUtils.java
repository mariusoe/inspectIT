package info.novatec.inspectit.rcp.util;

/**
 * Utility class for handling currently a proper equals comparison of objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class ObjectUtils {

	/**
	 * Private constructor.
	 */
	private ObjectUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Compares two objects for equality, where either one or both objects may be <code>null</code>.
	 * This method is originally being taken from the Apache Commons project.
	 * </p>
	 * 
	 * <pre>
	 * ObjectUtils.equals(null, null) = true
	 * ObjectUtils.equals(null, "") = false
	 * ObjectUtils.equals("", null) = false
	 * ObjectUtils.equals("", "") = true
	 * ObjectUtils.equals(Boolean.TRUE, null) = false
	 * ObjectUtils.equals(Boolean.TRUE, "true") = false
	 * ObjectUtils.equals(Boolean.TRUE, Boolean.TRUE) = true
	 * ObjectUtils.equals(Boolean.TRUE, Boolean.FALSE) = false
	 * </pre>
	 * 
	 * @param object1
	 *            the first object, may be <code>null</code>
	 * @param object2
	 *            the second object, may be <code>null</code>
	 * @return <code>true</code> if the values of both objects are the same
	 */
	public static boolean equals(Object object1, Object object2) { // NOPMD
		if (object1 == object2) { // NOPMD
			return true;
		}
		if ((null == object1) || (null == object2)) {
			return false;
		}
		return object1.equals(object2);
	}

	/**
	 * Null safe compare. Returns following results:
	 * 
	 * ObjectUtils.equals(Comparable, Object) = Comparable.compareTo(object)
	 * ObjectUtils.equals(null, Object) = -1 ObjectUtils.equals(Comparable, null) = 1
	 * ObjectUtils.equals(null, null) = 0
	 * 
	 * @param <T>
	 *            Type of comparing objects.
	 * @param object1
	 *            Object1
	 * @param object2
	 *            Object2
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal
	 *         to, or greater than the specified object.
	 * @see Comparable#compareTo(Object)
	 */
	public static <T> int compare(Comparable<T> object1, T object2) {
		if (null != object1 && null != object2) {
			return object1.compareTo(object2);
		} else if (null != object1) {
			return 1;
		} else if (null != object2) {
			return -1;
		} else {
			return 0;
		}
	}

}
