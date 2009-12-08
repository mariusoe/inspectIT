package info.novatec.novaspy.cmr.dao;

import info.novatec.novaspy.communication.data.ExceptionSensorData;

import java.util.List;

/**
 * This layer is used to access the exception tracer information.
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
	List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template, int limit);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects. This list can be
	 * used to get an overview over recorded Exceptions in a target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @return List of {@link ExceptionSensorData} objects to get an overview of
	 *         recorded Exceptions.
	 */
	List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects containing all
	 * details of a specific Exception class.
	 * 
	 * @param template
	 *            The template data object.
	 * @return List of {@link ExceptionSensorData} objects containing all
	 *         details of a specific Exception class.
	 */
	List<ExceptionSensorData> getExceptionTreeDetails(ExceptionSensorData template);
}
