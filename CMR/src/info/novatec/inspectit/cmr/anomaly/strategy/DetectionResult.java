/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.strategy;

/**
 * Holds the result details of the detection.
 *
 * @author Marius Oehler
 *
 */
public final class DetectionResult {

	/**
	 * Creates a new {@link DetectionResult} instance containing the given values.
	 *
	 * @param status
	 *            the status of the result
	 * @return created {@link DetectionResult} instance
	 */
	public static DetectionResult make(Status status) {
		return make(status, null);
	}

	/**
	 * Creates a new {@link DetectionResult} instance containing the given values.
	 *
	 * @param status
	 *            the status of the result
	 * @param message
	 *            the message of the result
	 * @return created {@link DetectionResult} instance
	 */
	public static DetectionResult make(Status status, String message) {
		return new DetectionResult(status, message);
	}

	/**
	 * Represents the result status.
	 *
	 * @author Marius Oehler
	 *
	 */
	public enum Status {

		/**
		 * This status doesn't provide any information about the current state.
		 */
		UNKNOWN,

		/**
		 * This status indicates that everything is ok.
		 */
		NORMAL,

		/**
		 * This status indicates that something is not ok.
		 */
		CRITICAL;
	}

	/**
	 * The status.
	 */
	private final Status status;

	/**
	 * The result message.
	 */
	private final String message;

	/**
	 * Hidden constructor.
	 *
	 * @param status
	 *            the result status
	 * @param message
	 *            the result message
	 */
	private DetectionResult(Status status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	/**
	 * Gets {@link #status}.
	 *
	 * @return {@link #status}
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Gets {@link #message}.
	 *
	 * @return {@link #message}
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DetectionResult [status=" + status + ", message=" + message + "]";
	}
}
