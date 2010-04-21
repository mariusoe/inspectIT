package info.novatec.inspectit.communication.exception;

public class EntityNotFoundException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8807874190947190043L;

	/**
	 * Constructs a new exception with the specified detail message. The cause
	 * is not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 * 
	 * @param message
	 *            The detail message. The detail message is saved for later
	 *            retrieval by the {@link #getMessage()} method.
	 */
	public EntityNotFoundException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is
	 * <i>not</i> automatically incorporated in this exception's detail message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
