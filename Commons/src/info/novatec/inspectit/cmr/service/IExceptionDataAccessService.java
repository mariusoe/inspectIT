package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.List;

/**
 * Service interface which defines the methods to retrieve data objects based on
 * the exception tracer recordings.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface IExceptionDataAccessService {

	/**
	 * Returns a list of {@link ExceptionSensorData} objects. This list can be
	 * used to get an overview over recorded Exceptions in a target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @param limit
	 *            The limit/size of the list.
	 * @return A list of {@link ExceptionSensorData} objects. This list can be
	 *         used to get an overview over recorded Exceptions in a target
	 *         application.
	 */
	List getExceptionTreeOverview(ExceptionSensorData template, int limit);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects. This list can be
	 * used to get an overview over recorded Exceptions in a target application.
	 * 
	 * @param template
	 *            The template data object.
	 * @return A list of {@link ExceptionSensorData} objects. This list can be
	 *         used to get an overview over recorded Exceptions in a target
	 *         application.
	 */
	List getExceptionTreeOverview(ExceptionSensorData template);

	/**
	 * Returns a list of {@link ExceptionSensorData} objects containing all
	 * details of a specific Exception class.
	 * 
	 * @param template
	 *            The template object.
	 * @return List of {@link ExceptionSensorData} objects containing all
	 *         details of a specific Exception class.
	 */
	List getExceptionTreeDetails(ExceptionSensorData template);

}
