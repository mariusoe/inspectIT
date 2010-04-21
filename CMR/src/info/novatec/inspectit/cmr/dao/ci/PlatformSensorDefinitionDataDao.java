package info.novatec.inspectit.cmr.dao.ci;

import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

/**
 * This DAO is used to handle all {@link PlatformSensorDefinitionData} objects.
 * 
 * @author Matthias Huber
 * 
 */
public interface PlatformSensorDefinitionDataDao {

	/**
	 * Adds an {@link PlatformSensorDefinitionData} object into the database.
	 * 
	 * @param platformSensorDefinitionData
	 *            The {@link PlatformSensorDefinitionData} object to add.
	 * @return The assigned id of the stored
	 *         {@link PlatformSensorDefinitionData} object.
	 */
	long addPlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData);

	/**
	 * Deletes the {@link PlatformSensorDefinitionData} object with the given
	 * id.
	 * 
	 * @param platformSensorDefinitionId
	 *            The id of the {@link PlatformSensorDefinitionData} object to
	 *            delete.
	 * @throws EntityNotFoundException
	 *             The {@link PlatformSensorDefinitionData} object to update.
	 */
	void deletePlatformSensorDefinition(long platformSensorDefinitionId) throws EntityNotFoundException;

	/**
	 * Updates this {@link PlatformSensorDefinitionData} object.
	 * 
	 * @param platformSensorDefinitionData
	 *            Updates this {@link PlatformSensorDefinitionData} object.
	 */
	void updatePlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData);

}
