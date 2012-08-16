package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * Class for {@link ExceptionSensorData} aggregation.
 * 
 * @author Ivan Senic
 * 
 */
public class ExceptionDataAggregator implements IAggregator<ExceptionSensorData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 3605008873889674596L;

	/**
	 * Definition of aggregation type.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum ExceptionAggregationType {
		/**
		 * Aggregation based on the throw-able type and error message.
		 */
		GROUP_EXCEPTION_OVERVIEW,

		/**
		 * Aggregation based on the stack trace and error message.
		 */
		DISTINCT_STACK_TRACES;
	}

	/**
	 * Exception aggregation type.
	 */
	private ExceptionAggregationType exceptionAggregationType;

	/**
	 * Default constructor.
	 * 
	 * @param exceptionAggregationType
	 *            Exception aggregation type.
	 */
	public ExceptionDataAggregator(ExceptionAggregationType exceptionAggregationType) {
		this.exceptionAggregationType = exceptionAggregationType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(ExceptionSensorData aggregatedObject, ExceptionSensorData objectToAdd) {
		if (aggregatedObject instanceof AggregatedExceptionSensorData) {
			AggregatedExceptionSensorData aggregatedExceptionSensorData = (AggregatedExceptionSensorData) aggregatedObject;
			aggregatedExceptionSensorData.aggregateExceptionData(objectToAdd);
			if (null != objectToAdd.getChild()) {
				aggregate(aggregatedExceptionSensorData, objectToAdd.getChild());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ExceptionSensorData getClone(ExceptionSensorData exceptionData) {
		AggregatedExceptionSensorData clone = new AggregatedExceptionSensorData();
		clone.setPlatformIdent(exceptionData.getPlatformIdent());
		clone.setCause(exceptionData.getCause());
		clone.setErrorMessage(exceptionData.getErrorMessage());
		clone.setExceptionEvent(exceptionData.getExceptionEvent());
		clone.setParameterContentData(exceptionData.getParameterContentData());
		clone.setThrowableType(exceptionData.getThrowableType());
		clone.setStackTrace(exceptionData.getStackTrace());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCloning() {
		return true;
	}

	public Object getAggregationKey(ExceptionSensorData exceptionSensorData) {
		final int prime = 31;
		if (exceptionAggregationType == ExceptionAggregationType.GROUP_EXCEPTION_OVERVIEW) {
			int result = 0;
			result = prime * result + ((exceptionSensorData.getThrowableType() == null) ? 0 : exceptionSensorData.getThrowableType().hashCode());
			result = prime * result + ((exceptionSensorData.getErrorMessage() == null) ? 0 : exceptionSensorData.getErrorMessage().hashCode());
			return result;
		} else if (exceptionAggregationType == ExceptionAggregationType.DISTINCT_STACK_TRACES) {
			int result = 0;
			result = prime * result + ((exceptionSensorData.getErrorMessage() == null) ? 0 : exceptionSensorData.getErrorMessage().hashCode());
			result = prime * result + ((exceptionSensorData.getStackTrace() == null) ? 0 : exceptionSensorData.getStackTrace().hashCode());
			return result;
		}
		return 0;
	}

}
