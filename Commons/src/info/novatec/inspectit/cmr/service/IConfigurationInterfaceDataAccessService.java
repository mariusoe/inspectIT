package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.List;

/**
 * The configuration interface data access service is used and called by all configuration
 * interfaces. It provides methods to retrieve or send data objects.
 * 
 * @author Matthias Huber
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IConfigurationInterfaceDataAccessService {

	/**
	 * This method adds an {@link EnvironmentData} object. The {@link EnvironmentData} object is the
	 * root of the data model.
	 * 
	 * @param environmentData
	 *            The {@link EnvironmentData} object to add.
	 * 
	 * @return the id of the added {@link EnvironmentData} object.
	 */
	long addEnvironment(EnvironmentData environmentData);

	/**
	 * This method adds an {@link ExceptionSensorDefinitionData} object to a profile.
	 * 
	 * @param exceptionSensorDefinitionData
	 *            The {@link ExceptionSensorDefinitionData} object to add.
	 * @return the id of the added {@link ExceptionSensorDefinitionData} object.
	 */
	long addExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData);

	/**
	 * This method adds a {@link MethodSensorDefinitionData} object to a profile.
	 * 
	 * @param methodSensorDefinitionData
	 *            The {@link MethodSensorDefinitionData} object to add.
	 * @return the id of the added {@link MethodSensorDefinitionData} object.
	 */
	long addMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData);

	/**
	 * This method adds a {@link PlatformSensorDefinitionData} object to a profile.
	 * 
	 * @param platformSensorDefinitionData
	 *            The {@link PlatformSensorDefinitionData} object to add.
	 * @return the id of the added or updated {@link PlatformSensorDefinitionData} object.
	 */
	long addPlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData);

	/**
	 * This method adds a {@link ProfileData} object to an environment.
	 * 
	 * @param profileData
	 *            The {@link ProfileData} object to add.
	 * @return the id of the added {@link ProfileData} object.
	 */
	long addProfile(ProfileData profileData);

	/**
	 * Deletes an Environment.
	 * 
	 * @param environmentId
	 *            The id of the {@link EnvironmentData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link EnvironmentData} according to the id can be found.
	 */
	void deleteEnvironment(long environmentId) throws EntityNotFoundException;

	/**
	 * Deletes an Exception Sensor Definition.
	 * 
	 * @param exceptionSensorDefinitionId
	 *            The id of the {@link ExceptionSensorDefinitionData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link ExceptionSensorDefinitionData} object according to the id can be
	 *             found.
	 */
	void deleteExceptionSensorDefinition(long exceptionSensorDefinitionId) throws EntityNotFoundException;

	/**
	 * Deletes a Method Sensor Definition.
	 * 
	 * @param methodSensorDefinitionId
	 *            The id of the {@link MethodSensorDefinitionData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link MethodSensorDefinitionData} object according to the id can be found.
	 */
	void deleteMethodSensorDefinition(long methodSensorDefinitionId) throws EntityNotFoundException;

	/**
	 * Deletes a Platform Sensor Definition.
	 * 
	 * @param platformSensorDefinitionId
	 *            The id of the {@link PlatformSensorDefinitionData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link PlatformSensorDefinitionData} object according to the id can be
	 *             found.
	 */
	void deletePlatformSensorDefinition(long platformSensorDefinitionId) throws EntityNotFoundException;

	/**
	 * Deletes a Profile.
	 * 
	 * @param profileId
	 *            The id of the {@link ProfileData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link ProfileData} object according to the id can be found.
	 */
	void deleteProfile(long profileId) throws EntityNotFoundException;

	/**
	 * Deletes a sensor type.
	 * 
	 * @param sensorTypeId
	 *            The id of the {@link SensorTypeData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link SensorTypeData} object according to the id can be found.
	 */
	void deleteSensorType(long sensorTypeId) throws EntityNotFoundException;

	/**
	 * Returns a list of {@link EnvironmentData} objects which also have details about profiles
	 * belonging to the environments. This list can be used to get an overview over defined
	 * environments and profiles within the configuration.
	 * 
	 * @return A list of {@link EnvironmentData] objects. This list can be used to get an overview
	 *         over defined environments and profiles within the configuration.
	 */
	List<EnvironmentData> getEnvironments();

	/**
	 * Returns a {@link ProfileData} object with all sensor definitions included (
	 * {@link MethodSensorDefinitionData}, {@link PlatformSensorDefinitionData} and
	 * {@link ExceptionSensorDefinitionData}).
	 * 
	 * @param profileId
	 *            The id of the {@link ProfileData} object.
	 * @return The {@link ProfileData} object or null if no object could be found.
	 */
	ProfileData getProfile(long profileId);

	/**
	 * This method updates an existing {@link EnvironmentData} object.
	 * 
	 * @param environmentData
	 *            The {@link EnvironmentData} object to update.
	 */
	void updateEnvironmentSettings(EnvironmentData environmentData);

	/**
	 * This method updates an existing {@link ExceptionSensorDefinitionData} object.
	 * 
	 * @param exceptionSensorDefinitionData
	 *            The {@link ExceptionSensorDefinitionData} object to update.
	 */
	void updateExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData);

	/**
	 * This method updates an existing {@link MethodSensorDefinitionData} object.
	 * 
	 * @param methodSensorDefinitionData
	 *            The {@link MethodSensorDefinitionData} object to update.
	 */
	void updateMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData);

	/**
	 * This method updates an existing {@link PlatformSensorDefinitionData} object.
	 * 
	 * @param platformSensorDefinitionData
	 *            The {@link PlatformSensorDefinitionData} object to update.
	 */
	void updatePlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData);

	/**
	 * This method updates an existing {@link ProfileData} object, more precise only the two
	 * attributes name and description.
	 * 
	 * @param profileData
	 *            The {@link ProfileData} object to update.
	 */
	void updateProfileSettings(ProfileData profileData);

	/**
	 * This method updates an existing {@link SensorTypeData} object.
	 * 
	 * @param sensorTypeData
	 *            The {@link SensorTypeData·} object to update.
	 */
	void updateSensorType(SensorTypeData sensorTypeData);
}
