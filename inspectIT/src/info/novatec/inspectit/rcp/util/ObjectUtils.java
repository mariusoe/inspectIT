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
	public static boolean equals(Object object1, Object object2) {
		if (object1 == object2) {
			return true;
		}
		if ((null == object1) || (null == object2)) {
			return false;
		}
		return object1.equals(object2);
	}

}
