package info.novatec.inspectit.cmr.dao.ci;

import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

/**
 * This DAO is used to handle all {@link MethodSensorDefinitionData} objects.
 * 
 * @author Matthias Huber
 * 
 */
public interface MethodSensorDefinitionDataDao {

	/**
	 * Adds an {@link MethodSensorDefinitionData} object into the database.
	 * 
	 * @param methodSensorDefinitionData
	 *            The {@link MethodSensorDefinitionData} object to add.
	 * @return The assigned id of the stored {@link MethodSensorDefinitionData} object.
	 */
	long addMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData);

	/**
	 * Deletes the {@link MethodSensorDefinitionData} object with the given id.
	 * 
	 * @param methodSensorDefinitionId
	 *            The id of the {@link MethodSensorDefinitionData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link MethodSensorDefinitionData} object matching the given id could be
	 *             found.
	 */
	void deleteMethodSensorDefinition(long methodSensorDefinitionId) throws EntityNotFoundException;

	/**
	 * Updates this {@link MethodSensorDefinitionData} object.
	 * 
	 * @param methodSensorDefinitionData
	 *            The {@link MethodSensorDefinitionData} object to update.
	 */
	void updateMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData);

}
