package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.MethodSensorData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The invocation sequence data object which is used to store the path of method invocations from
 * instrumented methods.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocationSequenceData extends MethodSensorData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1388734093735447105L;

	/**
	 * The nested invocation traces are stored in this list.
	 */
	private List nestedSequences = new ArrayList(0);

	/**
	 * The parent sequence of this sequence if there is any.
	 */
	private InvocationSequenceData parentSequence;

	/**
	 * The associated timer data object. Can be <code>null</code>.
	 */
	private TimerData timerData;

	/**
	 * The associated sql statement data object. Can be <code>null</code>.
	 */
	private SqlStatementData sqlStatementData;

	/**
	 * The associated exception sensor data object. Can be <code>null</code>.
	 */
	private List exceptionSensorDataObjects;

	/**
	 * The position if parent sequence is not <code>null</code>.
	 */
	private long position;

	/**
	 * The duration of this invocation sequence.
	 */
	private double duration;

	/**
	 * The starttime of this invocation sequence
	 */
	private double start;

	/**
	 * The endtime of this invocation sequence
	 */
	private double end;

	/**
	 * The count of the nested sequences (all levels).
	 */
	private long childCount = 0;

	/**
	 * Default no-args constructor.
	 */
	public InvocationSequenceData() {
	}

	public InvocationSequenceData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	public List getNestedSequences() {
		return nestedSequences;
	}

	public void setNestedSequences(List nestedSequences) {
		this.nestedSequences = nestedSequences;
	}

	public InvocationSequenceData getParentSequence() {
		return parentSequence;
	}

	public void setParentSequence(InvocationSequenceData parentSequence) {
		this.parentSequence = parentSequence;
	}

	public TimerData getTimerData() {
		return timerData;
	}

	public void setTimerData(TimerData timerData) {
		this.timerData = timerData;
	}

	public void setSqlStatementData(SqlStatementData sqlStatementData) {
		this.sqlStatementData = sqlStatementData;
	}

	public SqlStatementData getSqlStatementData() {
		return sqlStatementData;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(long position) {
		this.position = position;
	}

	/**
	 * @return the position
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * @return the duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * @return the start time of the invocation sequence
	 */
	public double getStart() {
		return start;
	}

	/**
	 * 
	 * @param start
	 *            the start time of the invocation sequence
	 */
	public void setStart(double start) {
		this.start = start;
	}

	/**
	 * 
	 * @return the end time of the invocation sequence
	 */
	public double getEnd() {
		return end;
	}

	public List getExceptionSensorDataObjects() {
		return exceptionSensorDataObjects;
	}

	public void setExceptionSensorDataObjects(List exceptionSensorDataObjects) {
		this.exceptionSensorDataObjects = exceptionSensorDataObjects;
	}

	public void addExceptionSensorData(ExceptionSensorData data) {
		if (null == exceptionSensorDataObjects) {
			exceptionSensorDataObjects = new ArrayList();
		}
		exceptionSensorDataObjects.add(data);
	}

	/**
	 * 
	 * @param end
	 *            the end time of the invocation sequence
	 */
	public void setEnd(double end) {
		this.end = end;
	}

	/**
	 * @param childCount
	 *            the childCount to set
	 */
	public void setChildCount(long childCount) {
		this.childCount = childCount;
	}

	/**
	 * @return the childCount
	 */
	public long getChildCount() {
		return childCount;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((exceptionSensorDataObjects == null) ? 0 : exceptionSensorDataObjects.hashCode());
		result = prime * result + ((sqlStatementData == null) ? 0 : sqlStatementData.hashCode());
		result = prime * result + ((timerData == null) ? 0 : timerData.hashCode());
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
		InvocationSequenceData other = (InvocationSequenceData) obj;
		if (exceptionSensorDataObjects == null) {
			if (other.exceptionSensorDataObjects != null) {
				return false;
			}
		} else if (!exceptionSensorDataObjects.equals(other.exceptionSensorDataObjects)) {
			return false;
		}
		if (sqlStatementData == null) {
			if (other.sqlStatementData != null) {
				return false;
			}
		} else if (!sqlStatementData.equals(other.sqlStatementData)) {
			return false;
		}
		if (timerData == null) {
			if (other.timerData != null) {
				return false;
			}
		} else if (!timerData.equals(other.timerData)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size = super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(4, 0, 0, 0, 3, 2);
		if (null != timerData) {
			size += timerData.getObjectSize(objectSizes);
		}
		if (null != sqlStatementData) {
			size += sqlStatementData.getObjectSize(objectSizes);
		}
		if (null != nestedSequences && nestedSequences instanceof ArrayList) {
			size += objectSizes.getSizeOf((ArrayList) nestedSequences);
			Iterator iterator = nestedSequences.iterator();
			while (iterator.hasNext()) {
				try {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) iterator.next();
					size += invocationSequenceData.getObjectSize(objectSizes);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		if (null != exceptionSensorDataObjects) {
			size += objectSizes.getSizeOf(exceptionSensorDataObjects);
			Iterator iterator = exceptionSensorDataObjects.iterator();
			while (iterator.hasNext()) {
				try {
					ExceptionSensorData exceptionSensorData = (ExceptionSensorData) iterator.next();
					size += exceptionSensorData.getObjectSize(objectSizes);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		return objectSizes.alignTo8Bytes(size);
	}

}
