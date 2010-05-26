package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.MethodSensorData;

import java.sql.Timestamp;

/**
 * The exception tracer data object used to store all information collected on
 * an instrumented exception path.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionSensorData extends MethodSensorData {

	/**
	 * The serial version UIDs.
	 */
	private static final long serialVersionUID = -8064862129447036553L;

	/**
	 * The detailed error message string of this {@link Throwable} object.
	 */
	private String errorMessage;

	/**
	 * The name of the throwable that caused this throwable to get thrown, or null if this
	 * throwable was not caused by another throwable, or if the causative
	 * throwable is unknown.
	 */
	private String cause;

	/**
	 * The stack trace stored as a string.
	 */
	private String stackTrace;

	/**
	 * The {@link ExceptionEventEnum} indicating by which event this object was
	 * created.
	 */
	private ExceptionEventEnum exceptionEvent;

	/**
	 * The string representation of the {@link ExceptionEventEnum} object. This
	 * object is only used in the database.
	 */
	private String exceptionEventString;

	/**
	 * The detailed name of the {@link Throwable} object.
	 */
	private String throwableType;

	/**
	 * The child of this {@link ExceptionSensorData} object.
	 */
	private ExceptionSensorData child;

	/**
	 * The identity hash code of the thrown {@link Throwable} object.
	 */
	private long throwableIdentityHashCode;

	/**
	 * Default no-args constructor.
	 */
	public ExceptionSensorData() {
	}

	public ExceptionSensorData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public ExceptionSensorData getChild() {
		return child;
	}

	public void setChild(ExceptionSensorData child) {
		this.child = child;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getExceptionEventString() {
		return exceptionEventString;
	}

	public void setExceptionEventString(String exceptionEventString) {
		this.exceptionEventString = exceptionEventString;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public ExceptionEventEnum getExceptionEvent() {
		return exceptionEvent;
	}

	public void setExceptionEvent(ExceptionEventEnum exceptionEvent) {
		this.exceptionEvent = exceptionEvent;
		exceptionEventString = this.exceptionEvent.toString();
	}

	public String getThrowableType() {
		return throwableType;
	}

	public void setThrowableType(String throwableType) {
		this.throwableType = throwableType;
	}

	public long getThrowableIdentityHashCode() {
		return throwableIdentityHashCode;
	}

	public void setThrowableIdentityHashCode(long throwableIdentityHashCode) {
		this.throwableIdentityHashCode = throwableIdentityHashCode;
	}

	public String toString() {
		return throwableType + "@" + throwableIdentityHashCode + " :: " + exceptionEventString;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cause == null) ? 0 : cause.hashCode());
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((exceptionEvent == null) ? 0 : exceptionEvent.hashCode());
		result = prime * result + ((exceptionEventString == null) ? 0 : exceptionEventString.hashCode());
		result = prime * result + ((stackTrace == null) ? 0 : stackTrace.hashCode());
		result = prime * result + (int) (throwableIdentityHashCode ^ (throwableIdentityHashCode >>> 32));
		result = prime * result + ((throwableType == null) ? 0 : throwableType.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExceptionSensorData other = (ExceptionSensorData) obj;
		if (cause == null) {
			if (other.cause != null) {
				return false;
			}
		} else if (!cause.equals(other.cause)) {
			return false;
		}
		if (child == null) {
			if (other.child != null) {
				return false;
			}
		} else if (!child.equals(other.child)) {
			return false;
		}
		if (errorMessage == null) {
			if (other.errorMessage != null) {
				return false;
			}
		} else if (!errorMessage.equals(other.errorMessage)) {
			return false;
		}
		if (exceptionEvent == null) {
			if (other.exceptionEvent != null) {
				return false;
			}
		} else if (!exceptionEvent.equals(other.exceptionEvent)) {
			return false;
		}
		if (exceptionEventString == null) {
			if (other.exceptionEventString != null) {
				return false;
			}
		} else if (!exceptionEventString.equals(other.exceptionEventString)) {
			return false;
		}
		if (stackTrace == null) {
			if (other.stackTrace != null) {
				return false;
			}
		} else if (!stackTrace.equals(other.stackTrace)) {
			return false;
		}
		if (throwableIdentityHashCode != other.throwableIdentityHashCode) {
			return false;
		}
		if (throwableType == null) {
			if (other.throwableType != null) {
				return false;
			}
		} else if (!throwableType.equals(other.throwableType)) {
			return false;
		}
		return true;
	}

}
