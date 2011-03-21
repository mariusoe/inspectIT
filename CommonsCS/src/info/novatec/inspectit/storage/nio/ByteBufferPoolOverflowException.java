package info.novatec.inspectit.storage.nio;

/**
 * @author Ivan Senic
 * 
 */
public class ByteBufferPoolOverflowException extends Exception {

	/**
	 * Generate UID.
	 */
	private static final long serialVersionUID = 6410725021003577982L;

	/**
	 * Default constructor.
	 */
	public ByteBufferPoolOverflowException() {
	}

	/**
	 * @param message Message.
	 * @see Exception#Exception(String)
	 */
	public ByteBufferPoolOverflowException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            Throwable.
	 * @see Exception#Exception(Throwable)
	 */
	public ByteBufferPoolOverflowException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            Exception message.
	 * @param cause
	 *            Throwable.
	 * @see Exception#Exception(String, Throwable)
	 */
	public ByteBufferPoolOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

}
