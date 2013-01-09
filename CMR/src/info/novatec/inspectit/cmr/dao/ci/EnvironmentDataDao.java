package info.novatec.inspectit.cmr.dao.ci;

import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.List;

/**
 * This DAO is used to handle all {@link EnvironmentData} objects.
 * 
 * @author Matthias Huber
 * 
 */
public interface EnvironmentDataDao {

	/**
	 * Adds an {@link EnvironmentData} object into the database. Attached {@link SensorTypeData}
	 * objects are not included. They will be stored separately with following method
	 * 
	 * @see SensorTypeDataDao#addSensorType(SensorTypeData)
	 * 
	 * @param environmentData
	 *            The {@link EnvironmentData} object to add.
	 * @return The assigned id of the stored {@link EnvironmentData} object.
	 */
	long addEnvironment(EnvironmentData environmentData);

	/**
	 * Deletes the {@link EnvironmentData} object with the given id.
	 * 
	 * @param environmentId
	 *            The id of the {@link EnvironmentData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link EnvironmentData} object matching the given id could be found.
	 */
	void deleteEnvironment(long environmentId) throws EntityNotFoundException;

	/**
	 * Returns a list containing all stored {@link EnvironmentData} objects. Attached
	 * {@link ProfileDataDao}, {@link SensorTypeDataDao} and {@link TypeOptionData} objects are
	 * returned too.
	 * 
	 * @return A list containing all stored {@link EnvironmentData}.
	 */
	List<EnvironmentData> getEnvironments();

	/**
	 * Updates this {@link EnvironmentData} object.
	 * 
	 * @param environmentData
	 *            The {@link EnvironmentData} object to update.
	 */
	void updateEnvironmentSettings(EnvironmentData environmentData);
}