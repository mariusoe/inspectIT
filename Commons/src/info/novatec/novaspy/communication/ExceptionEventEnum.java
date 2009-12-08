package info.novatec.novaspy.communication;

import java.io.Serializable;

/**
 * As Enumerations aren't directly supported in java 1.4, we have to create some
 * methods which will behave like an enumeration in 1.5.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionEventEnum implements Serializable {
	// TODO ET: check what we need exactly from this class.

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -613364227121414500L;

	public static final ExceptionEventEnum CREATED = new ExceptionEventEnum(0);

	public static final ExceptionEventEnum RETHROWN = new ExceptionEventEnum(1);

	public static final ExceptionEventEnum PASSED = new ExceptionEventEnum(2);

	public static final ExceptionEventEnum HANDLED = new ExceptionEventEnum(3);

	public static final ExceptionEventEnum UNREGISTERED_PASSED = new ExceptionEventEnum(4);

	/**
	 * Defines the current event type.
	 */
	private int value;

	/**
	 * The constructor takes one argument, an integer which defines the current
	 * event type.
	 * 
	 * @param value
	 *            The int value of this priority.
	 */
	public ExceptionEventEnum(int value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String toString() {
		// return java.lang.String.valueOf(value);
		return (String) names.get(getValue());
	}

	/**
	 * Creates an instance of ExceptionEventEnum from <code>value</code>.
	 * 
	 * @param value
	 *            the value to create the ExceptionEventEnum from.
	 * @return The ExceptionEventEnum instance.
	 */
	public static ExceptionEventEnum fromInt(final int value) {
		final ExceptionEventEnum typeValue = (ExceptionEventEnum) VALUES.get(new java.lang.Integer(value));
		if (typeValue == null) {
			throw new IllegalArgumentException("invalid value '" + value + "', possible values are: " + literals);
		}
		return typeValue;
	}

	/**
	 * Gets the underlying value of this type safe enumeration.
	 * 
	 * @return the underlying value.
	 */
	public final int getValue() {
		return this.value;
	}

	/**
	 * Compares an instance of ExceptionEventEnum to this instance.
	 * 
	 * @param that
	 *            The other instance of ExceptionEventEnum.
	 * @return Returns -1 if the instances aren't equal, 0 for equality.
	 */
	public final int compareTo(final Object that) {
		if (this.getValue() < ((ExceptionEventEnum) that).getValue()) {
			return -1;
		} else {
			if (this.getValue() == ((ExceptionEventEnum) that).getValue()) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	/**
	 * Returns an unmodifiable list containing the literals that are known by
	 * this enumeration.
	 * 
	 * @return A List containing the actual literals defined by this
	 *         enumeration, this list can not be modified.
	 */
	public static java.util.List literals() {
		return literals;
	}

	/**
	 * Returns an unmodifiable list containing the names of the literals that
	 * are known by this enumeration.
	 * 
	 * @return A List containing the actual names of the literals defined by
	 *         this enumeration, this list can not be modified.
	 */
	public static java.util.List names() {
		return names;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object object) {
		return (this == object) || ((object instanceof ExceptionEventEnum) && (((ExceptionEventEnum) object).getValue() == this.getValue()));
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return this.getValue();
	}

	/**
	 * This method allows the deserialization of an instance of this enumeration
	 * type to return the actual instance that will be the singleton for the JVM
	 * in which the current thread is running.
	 * <p>
	 * Doing this will allow users to safely use the equality operator
	 * <code>==</code> for enumerations because a regular deserialized object is
	 * always a newly constructed instance and will therefore never be an
	 * existing reference; it is this <code>readResolve()</code> method which
	 * will intercept the deserialization process in order to return the proper
	 * singleton reference.
	 * <p>
	 * This method is documented here: <a href=
	 * "http://java.sun.com/j2se/1.3/docs/guide/serialization/spec/input.doc6.html"
	 * >Java Object Serialization Specification</a>
	 */
	private java.lang.Object readResolve() throws java.io.ObjectStreamException {
		return ExceptionEventEnum.fromInt(this.value);
	}

	private static final java.util.Map VALUES = new java.util.HashMap(5, 1);
	private static java.util.List literals = new java.util.ArrayList(5);
	private static java.util.List names = new java.util.ArrayList(5);

	/**
	 * Initializes the values.
	 */
	static {
		VALUES.put(new java.lang.Integer(CREATED.value), CREATED);
		literals.add(new java.lang.Integer(CREATED.value));
		names.add("CREATED");
		VALUES.put(new java.lang.Integer(RETHROWN.value), RETHROWN);
		literals.add(new java.lang.Integer(RETHROWN.value));
		names.add("RETHROWN");
		VALUES.put(new java.lang.Integer(PASSED.value), PASSED);
		literals.add(new java.lang.Integer(PASSED.value));
		names.add("PASSED");
		VALUES.put(new java.lang.Integer(HANDLED.value), HANDLED);
		literals.add(new java.lang.Integer(HANDLED.value));
		names.add("HANDLED");
		VALUES.put(new java.lang.Integer(UNREGISTERED_PASSED.value), UNREGISTERED_PASSED);
		literals.add(new java.lang.Integer(UNREGISTERED_PASSED.value));
		names.add("UNREGISTERED_PASSED");
		literals = java.util.Collections.unmodifiableList(literals);
		names = java.util.Collections.unmodifiableList(names);
	}

}
