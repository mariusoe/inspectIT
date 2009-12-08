package info.novatec.novaspy.agent.config;

/**
 * As Enumerations aren't directly supported in java 1.4, we have to create some
 * methods which will behave like an enumeration in 1.5.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PriorityEnum {

	/**
	 * The priority used by the invocation tracing system.
	 */
	public static final PriorityEnum INVOC = new PriorityEnum(0);

	/**
	 * Minimum priority.
	 */
	public static final PriorityEnum MIN = new PriorityEnum(1);

	/**
	 * Low priority.
	 */
	public static final PriorityEnum LOW = new PriorityEnum(2);

	/**
	 * Normal priority.
	 */
	public static final PriorityEnum NORMAL = new PriorityEnum(3);

	/**
	 * High priority.
	 */
	public static final PriorityEnum HIGH = new PriorityEnum(4);

	/**
	 * Maximum priority.
	 */
	public static final PriorityEnum MAX = new PriorityEnum(5);

	/**
	 * Defines the current priority.
	 */
	private int value;

	/**
	 * The constructor takes one argument, an integer which defines the current
	 * priority.
	 * 
	 * @param value
	 *            The int value of this priority.
	 */
	public PriorityEnum(int value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String toString() {
		return java.lang.String.valueOf(value);
	}

	/**
	 * Creates an instance of PriorityEnum from <code>value</code>.
	 * 
	 * @param value
	 *            the value to create the PriorityEnum from.
	 * @return The PriorityEnum instance.
	 */
	public static PriorityEnum fromInt(final int value) {
		final PriorityEnum typeValue = (PriorityEnum) VALUES.get(new java.lang.Integer(value));
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
	 * Compares an instance of PriorityEnum to this instance.
	 * 
	 * @param that
	 *            The other instance of PriorityEnum.
	 * @return Returns -1 if the instances aren't equal, 0 for equality.
	 */
	public final int compareTo(final Object that) {
		if (this.getValue() < ((PriorityEnum) that).getValue()) {
			return -1;
		} else {
			if (this.getValue() == ((PriorityEnum) that).getValue()) {
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
		return (this == object) || (object instanceof PriorityEnum && ((PriorityEnum) object).getValue() == this.getValue());
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
		return PriorityEnum.fromInt(this.value);
	}

	private static final java.util.Map VALUES = new java.util.HashMap(5, 1);
	private static java.util.List literals = new java.util.ArrayList(5);
	private static java.util.List names = new java.util.ArrayList(5);

	/**
	 * Initializes the values.
	 */
	static {
		VALUES.put(new java.lang.Integer(INVOC.value), INVOC);
		literals.add(new java.lang.Integer(INVOC.value));
		names.add("INVOC");
		VALUES.put(new java.lang.Integer(MIN.value), MIN);
		literals.add(new java.lang.Integer(MIN.value));
		names.add("MIN");
		VALUES.put(new java.lang.Integer(LOW.value), LOW);
		literals.add(new java.lang.Integer(LOW.value));
		names.add("LOW");
		VALUES.put(new java.lang.Integer(NORMAL.value), NORMAL);
		literals.add(new java.lang.Integer(NORMAL.value));
		names.add("NORMAL");
		VALUES.put(new java.lang.Integer(HIGH.value), HIGH);
		literals.add(new java.lang.Integer(HIGH.value));
		names.add("HIGH");
		VALUES.put(new java.lang.Integer(MAX.value), MAX);
		literals.add(new java.lang.Integer(MAX.value));
		names.add("MAX");
		literals = java.util.Collections.unmodifiableList(literals);
		names = java.util.Collections.unmodifiableList(names);
	}

}
