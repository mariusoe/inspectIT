package info.novatec.inspectit.cmr.dao.ci;

import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

/**
 * This DAO is used to handle all {@link ExceptionSensorDefinitionData} objects.
 * 
 * @author Matthias Huber
 * 
 */
public interface ExceptionSensorDefinitionDataDao {

	/**
	 * Adds an {@link ExceptionSensorDefinitionData} object into the database.
	 * 
	 * @param exceptionSensorDefinitionData
	 *            The {@link ExceptionSensorDefinitionData} object to add.
	 * @return The assigned id of the stored {@link ExceptionSensorDefinitionData} object.
	 */
	long addExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData);

	/**
	 * Deletes the {@link ExceptionSensorDefinitionData} object with the given id.
	 * 
	 * @param exceptionSensorDefinitionId
	 *            The id of the {@link ExceptionSensorDefinitionData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link ExceptionSensorDefinitionData} object matching the given id could be
	 *             found.
	 */
	void deleteExceptionSensorDefinition(long exceptionSensorDefinitionId) throws EntityNotFoundException;

	/**
	 * Updates this {@link ExceptionSensorDefinitionData} object.
	 * 
	 * @param exceptionSensorDefinitionData
	 *            The {@link ExceptionSensorDefinitionData} object to update.
	 */
	void updateExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData);

}
