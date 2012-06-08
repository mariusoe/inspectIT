package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.ExceptionEvent;

import java.sql.Timestamp;

/**
 * The exception sensor data object used to store all information collected on an instrumented
 * exception path.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionSensorData extends InvocationAwareData {

	/**
	 * The serial version UIDs.
	 */
	private static final long serialVersionUID = -8064862129447036553L;

	/**
	 * The detailed error message string of this {@link Throwable} object.
	 */
	private String errorMessage;

	/**
	 * The name of the throwable that caused this throwable to get thrown, or null if this throwable
	 * was not caused by another throwable, or if the causative throwable is unknown.
	 */
	private String cause;

	/**
	 * The stack trace stored as a string.
	 */
	private String stackTrace;

	/**
	 * The {@link ExceptionEvent} indicating by which event this object was created.
	 */
	private ExceptionEvent exceptionEvent;

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

	public ExceptionEvent getExceptionEvent() {
		return exceptionEvent;
	}

	public void setExceptionEvent(ExceptionEvent exceptionEvent) {
		this.exceptionEvent = exceptionEvent;
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

	public double getInvocationAffiliationPercentage() {
		return (double) getObjectsInInvocationsCount() / 1d;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, false);
		size += objectSizes.getPrimitiveTypesSize(6, 0, 0, 0, 1, 0);
		size += objectSizes.getSizeOf(errorMessage);
		size += objectSizes.getSizeOf(cause);
		size += objectSizes.getSizeOf(stackTrace);
		size += objectSizes.getSizeOf(throwableType);
		if (null != exceptionEvent) {
			size += exceptionEvent.getObjectSize(objectSizes);
		}
		if (null != child) {
			size += child.getObjectSize(objectSizes);
		}
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cause == null) ? 0 : cause.hashCode());
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((exceptionEvent == null) ? 0 : exceptionEvent.hashCode());
		result = prime * result + ((stackTrace == null) ? 0 : stackTrace.hashCode());
		result = prime * result + (int) (throwableIdentityHashCode ^ (throwableIdentityHashCode >>> 32));
		result = prime * result + ((throwableType == null) ? 0 : throwableType.hashCode());
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
		if (exceptionEvent == null) {
			if (other.exceptionEvent != null) {
				return false;
			}
		} else if (!exceptionEvent.equals(other.exceptionEvent)) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return throwableType + "@" + throwableIdentityHashCode;
	}

}
