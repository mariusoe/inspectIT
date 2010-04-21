package info.novatec.inspectit.cmr.dao.ci;

import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

/**
 * This DAO is used to handle all {@link SensorTypeData} objects.
 * 
 * @author Matthias Huber
 * 
 */
public interface SensorTypeDataDao {

	/**
	 * Adds an {@link SensorTypeData} object into the database.
	 * 
	 * @param sensorTypeData
	 *            The {@link SensorTypeData} object to add.
	 * @return The assigned id of the stored {@link SensorTypeData} object.
	 */
	long addSensorType(SensorTypeData sensorTypeData);

	/**
	 * Deletes the {@link SensorTypeData} object wth the given id.
	 * 
	 * @param sensorTypeId
	 *            The id of the {@link SensorTypeData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link SensorTypeData} object matching the given id could
	 *             be found.
	 */
	void deleteSensorType(long sensorTypeId) throws EntityNotFoundException;

	/**
	 * Updates this {@link SensorTypeData} object.
	 * 
	 * @param sensorTypeData
	 *            The {@link SensorTypeData} object to update.
	 */
	void updateSensorType(SensorTypeData sensorTypeData);

}
