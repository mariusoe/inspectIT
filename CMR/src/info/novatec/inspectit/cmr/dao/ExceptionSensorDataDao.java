package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.List;

/**
 * This layer is used to access the exception sensor information.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface ExceptionSensorDataDao {

	/**
	 * Returns a list of {@link ExceptionSensorData} objects. This list can be
	 * used to get an overview over recorded Exceptions in a target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @param limit
	 *            The limit/size of the list.
	 * @return List of {@link ExceptionSensorData} objects to get an overview of
	 *         recorded Exceptions.
	 */
	List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects. This list can be
	 * used to get an overview over recorded Exceptions in a target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @return List of {@link ExceptionSensorData} objects to get an overview of
	 *         recorded Exceptions.
	 */
	List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects containing all
	 * details of a specific Exception class.
	 * 
	 * @param template
	 *            The template data object.
	 * @return List of {@link ExceptionSensorData} objects containing all
	 *         details of a specific Exception class.
	 */
	List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects that is used to
	 * show an overview over Exceptions with specific information about the
	 * number of caused event types.
	 * 
	 * @param template
	 *            The template object to be used for the query.
	 * @return A list of {@link ExceptionSensorData} objects with additional
	 *         information about how often a specific eventType was caused.
	 */
	List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template);

	/**
	 * Returns a list of {@link ExceptionSensorData} object where all fields are
	 * <code>null</code>, except the stack trace.
	 * 
	 * @param template
	 *            The template object to be used for the query.
	 * @return A list of {@link ExceptionSensorData} object where all fields are
	 *         <code>null</code>, except the stack trace.
	 */
	List<ExceptionSensorData> getStackTracesForErrorMessage(ExceptionSensorData template);
}
