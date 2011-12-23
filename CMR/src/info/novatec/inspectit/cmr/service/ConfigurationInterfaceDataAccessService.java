package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.ExceptionSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.MethodSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.PlatformSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.cmr.dao.ci.SensorTypeDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Matthias Huber
 * 
 */
@Service
public class ConfigurationInterfaceDataAccessService implements IConfigurationInterfaceDataAccessService {

	/**
	 * The environment data DAO.
	 */
	@Autowired
	private EnvironmentDataDao environmentDataDao;

	/**
	 * The profile data DAO.
	 */
	@Autowired
	private ProfileDataDao profileDataDao;

	/**
	 * The sensor type data DAO.
	 */
	@Autowired
	private SensorTypeDataDao sensorTypeDataDao;

	/**
	 * The exception sensor definition data DAO.
	 */
	@Autowired
	private ExceptionSensorDefinitionDataDao exceptionSensorDefinitionDataDao;

	/**
	 * The method sensor definition data DAO.
	 */
	@Autowired
	private MethodSensorDefinitionDataDao methodSensorDefinitionDataDao;

	/**
	 * The platform sensor definition data DAO.
	 */
	@Autowired
	private PlatformSensorDefinitionDataDao platformSensorDefinitionDataDao;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public long addEnvironment(EnvironmentData environmentData) {
		long result = environmentDataDao.addEnvironment(environmentData);

		environmentData.setSensorTypes(generateStandardSensorTypes());
		Set<SensorTypeData> sensorTypes = environmentData.getSensorTypes();
		for (SensorTypeData sensorType : sensorTypes) {
			sensorType.setEnvironmentData(environmentData);
			sensorTypeDataDao.addSensorType(sensorType);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public long addExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		long result = exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public long addMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		long result = methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public long addPlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		long result = platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public long addProfile(ProfileData profileData) {
		long result = profileDataDao.addProfile(profileData);

		profileData.setPlatformSensorDefinitions(generatePlatformSensors());
		Set<PlatformSensorDefinitionData> platformSensors = profileData.getPlatformSensorDefinitions();
		for (PlatformSensorDefinitionData platformSensor : platformSensors) {
			platformSensor.setProfileData(profileData);
			platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensor);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteEnvironment(long environmentId) throws EntityNotFoundException {
		environmentDataDao.deleteEnvironment(environmentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteExceptionSensorDefinition(long exceptionSensorDefinitionId) throws EntityNotFoundException {
		exceptionSensorDefinitionDataDao.deleteExceptionSensorDefinition(exceptionSensorDefinitionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteMethodSensorDefinition(long methodSensorDefinitionId) throws EntityNotFoundException {
		methodSensorDefinitionDataDao.deleteMethodSensorDefinition(methodSensorDefinitionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deletePlatformSensorDefinition(long platformSensorDefinitionId) throws EntityNotFoundException {
		platformSensorDefinitionDataDao.deletePlatformSensorDefinition(platformSensorDefinitionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteProfile(long profileId) throws EntityNotFoundException {
		profileDataDao.deleteProfile(profileId);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void deleteSensorType(long sensorTypeId) throws EntityNotFoundException {
		sensorTypeDataDao.deleteSensorType(sensorTypeId);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<EnvironmentData> getEnvironments() {
		List<EnvironmentData> result = environmentDataDao.getEnvironments();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public ProfileData getProfile(long profileId) {
		ProfileData result = profileDataDao.getProfile(profileId);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void updateEnvironmentSettings(EnvironmentData environmentData) {
		environmentDataDao.updateEnvironmentSettings(environmentData);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void updateExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		exceptionSensorDefinitionDataDao.updateExceptionSensorDefinition(exceptionSensorDefinitionData);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void updateMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		methodSensorDefinitionDataDao.updateMethodSensorDefinition(methodSensorDefinitionData);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void updatePlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		platformSensorDefinitionDataDao.updatePlatformSensorDefinition(platformSensorDefinitionData);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void updateProfileSettings(ProfileData profileData) {
		profileDataDao.updateProfile(profileData);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void updateSensorType(SensorTypeData sensorTypeData) {
		sensorTypeDataDao.updateSensorType(sensorTypeData);
	}

	private Set<SensorTypeData> generateStandardSensorTypes() {
		Set<SensorTypeData> sensorTypes = new HashSet<SensorTypeData>();

		// Timer sensor
		SensorTypeData timerSensor = new SensorTypeData();
		timerSensor.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.method.timer.TimerSensor");
		timerSensor.setName("Timer");
		timerSensor.setPriority(5);
		sensorTypes.add(timerSensor);

		// Invocation Sequence Sensor
		SensorTypeData invocationSensor = new SensorTypeData();
		invocationSensor.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceSensor");
		invocationSensor.setName("Invocation Sequence");
		invocationSensor.setPriority(0);
		sensorTypes.add(invocationSensor);

		return sensorTypes;
	}

	private Set<PlatformSensorDefinitionData> generatePlatformSensors() {
		Set<PlatformSensorDefinitionData> platformSensors = new HashSet<PlatformSensorDefinitionData>();

		// Class loading
		PlatformSensorDefinitionData platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(false);
		platformSensorDefinitionData.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation");
		platformSensors.add(platformSensorDefinitionData);

		// CPU
		platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(false);
		platformSensorDefinitionData.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.platform.CpuInformation");
		platformSensors.add(platformSensorDefinitionData);

		// Memory
		platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(false);
		platformSensorDefinitionData.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.platform.MemoryInformation");
		platformSensors.add(platformSensorDefinitionData);

		// Runtime
		platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(false);
		platformSensorDefinitionData.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.platform.RuntimeInformation");
		platformSensors.add(platformSensorDefinitionData);

		// System
		platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(false);
		platformSensorDefinitionData.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.platform.SystemInformation");
		platformSensors.add(platformSensorDefinitionData);

		// Thread
		platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(false);
		platformSensorDefinitionData.setFullyQualifiedName("info.novatec.inspectit.agent.sensor.platform.ThreadInformation");
		platformSensors.add(platformSensorDefinitionData);

		return platformSensors;
	}

}
