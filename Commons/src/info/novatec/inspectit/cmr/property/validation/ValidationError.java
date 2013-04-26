package info.novatec.inspectit.cmr.property.validation;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Single validation error containing the message describing the validation.
 * 
 * @author Ivan Senic
 * 
 */
public class ValidationError {

	/**
	 * Validation error message.
	 */
	private String message;

	/**
	 * No-arg constructor.
	 */
	public ValidationError() {
	}

	/**
	 * @param message
	 *            Validation error message.
	 */
	public ValidationError(String message) {
		this.message = message;
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
	 * Sets {@link #message}.
	 * 
	 * @param message
	 *            New value for {@link #message}
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ValidationError other = (ValidationError) obj;
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("message", message).toString();
	}

}
