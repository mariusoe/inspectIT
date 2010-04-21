package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.ExceptionSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.MethodSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.PlatformSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.cmr.dao.ci.SensorTypeDataDao;
import info.novatec.inspectit.cmr.util.Converter;
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Matthias Huber
 * 
 */
public class ConfigurationInterfaceDataAccessService implements IConfigurationInterfaceDataAccessService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConfigurationInterfaceDataAccessService.class);

	/**
	 * The environment data DAO.
	 */
	private EnvironmentDataDao environmentDataDao;

	/**
	 * The profile data DAO.
	 */
	private ProfileDataDao profileDataDao;

	/**
	 * The sensor type data DAO.
	 */
	private SensorTypeDataDao sensorTypeDataDao;

	/**
	 * The exception sensor definition data DAO.
	 */
	private ExceptionSensorDefinitionDataDao exceptionSensorDefinitionDataDao;

	/**
	 * The method sensor definition data DAO.
	 */
	private MethodSensorDefinitionDataDao methodSensorDefinitionDataDao;

	/**
	 * The platform sensor definition data DAO.
	 */
	private PlatformSensorDefinitionDataDao platformSensorDefinitionDataDao;

	/**
	 * @see IConfigurationInterfaceDataAccessService#addEnvironment(EnvironmentData)
	 */
	public long addEnvironment(EnvironmentData environmentData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.addEnvironment()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		long result = environmentDataDao.addEnvironment(environmentData);

		environmentData.setSensorTypes(generateStandardSensorTypes());
		Set<SensorTypeData> sensorTypes = environmentData.getSensorTypes();
		for (SensorTypeData sensorType : sensorTypes) {
			sensorType.setEnvironmentData(environmentData);
			sensorTypeDataDao.addSensorType(sensorType);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Add environment duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.addEnvironment()");
		}

		return result;
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addExceptionSensorDefinition(ExceptionSensorDefinitionData)
	 */
	public long addExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.addExceptionSensorDefinitionData()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		long result = exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Add exception sensor definition duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.addExceptionSensorDefinitionData()");
		}

		return result;
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addMethodSensorDefinition(MethodSensorDefinitionData)
	 */
	public long addMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.addMethodSensorDefinitionData()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		long result = methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Add method sensor definition duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.addMethodSensorDefinitionData()");
		}

		return result;
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addPlatformSensorDefinition(PlatformSensorDefinitionData)
	 */
	public long addPlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.addPlatformSensorDefinitionData()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		long result = platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Add platform sensor definition duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.addPlatformSensorDefinitionData()");
		}

		return result;
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addProfile(ProfileData)
	 */
	public long addProfile(ProfileData profileData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.addprofile()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		long result = profileDataDao.addProfile(profileData);

		profileData.setPlatformSensorDefinitions(generatePlatformSensors());
		Set<PlatformSensorDefinitionData> platformSensors = profileData.getPlatformSensorDefinitions();
		for (PlatformSensorDefinitionData platformSensor : platformSensors) {
			platformSensor.setProfileData(profileData);
			platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensor);
		}

		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Add profile duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.addProfile()");
		}

		return result;
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteProfile(long)
	 */
	public void deleteEnvironment(long environmentId) throws EntityNotFoundException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.deleteEnvironment()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		environmentDataDao.deleteEnvironment(environmentId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete environment duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.deleteEnvironment()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteExceptionSensorDefinition(long)
	 */
	public void deleteExceptionSensorDefinition(long exceptionSensorDefinitionId) throws EntityNotFoundException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.deleteExceptionSensorDefinitionData()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		exceptionSensorDefinitionDataDao.deleteExceptionSensorDefinition(exceptionSensorDefinitionId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete exception sensor duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.deleteExceptionSensorDefinitionData()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteMethodSensorDefinition(long)
	 */
	public void deleteMethodSensorDefinition(long methodSensorDefinitionId) throws EntityNotFoundException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.deleteMethodSensorDefinition()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		methodSensorDefinitionDataDao.deleteMethodSensorDefinition(methodSensorDefinitionId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete method sensor duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.deleteMethodSensorDefinition()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deletePlatformSensorDefinition(long)
	 */
	public void deletePlatformSensorDefinition(long platformSensorDefinitionId) throws EntityNotFoundException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.deletePlatformSensorDefinition()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		platformSensorDefinitionDataDao.deletePlatformSensorDefinition(platformSensorDefinitionId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete platform sensor duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.deletePlatformSensorDefinition()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteProfile(long)
	 */
	public void deleteProfile(long profileId) throws EntityNotFoundException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.deleteProfile()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		profileDataDao.deleteProfile(profileId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete profile duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.deleteProfile()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteSensorType(long)
	 */
	public void deleteSensorType(long sensorTypeId) throws EntityNotFoundException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.deleteSensorType()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		sensorTypeDataDao.deleteSensorType(sensorTypeId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete sensor type duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.deleteSensorType()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#getEnvironments(EnvironmentData)
	 */
	public List<EnvironmentData> getEnvironments() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.getEnvironments()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<EnvironmentData> result = environmentDataDao.getEnvironments();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get environments duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.getEnvironments()");
		}

		return result;
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#getProfile(long)
	 */
	public ProfileData getProfile(long profileId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.getProfile()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		ProfileData result = profileDataDao.getProfile(profileId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get profile duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.getProfile()");
		}

		return result;
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateEnvironmentSettings(EnvironmentData)
	 */
	public void updateEnvironmentSettings(EnvironmentData environmentData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.updateEnvironmentSettings()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		environmentDataDao.updateEnvironmentSettings(environmentData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update environment settings duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.updateEnvironmentSettings()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateExceptionSensorDefinition(ExceptionSensorDefinitionData)
	 */
	public void updateExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.updateExceptionSensorDefinition()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		exceptionSensorDefinitionDataDao.updateExceptionSensorDefinition(exceptionSensorDefinitionData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update Exception Sensor Definition duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.updateExceptionSensorDefinition()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateMethodSensorDefinition(MethodSensorDefinitionData)
	 */
	public void updateMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.updateMethodSensorDefinition()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		methodSensorDefinitionDataDao.updateMethodSensorDefinition(methodSensorDefinitionData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update Method Sensor Definition duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.updateMethodSensorDefinition()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updatePlatformSensorDefinition(PlatformSensorDefinitionData)
	 */
	public void updatePlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.updatePlatformSensorDefinition()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		platformSensorDefinitionDataDao.updatePlatformSensorDefinition(platformSensorDefinitionData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update Platform Sensor Definition duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.updatePlatformSensorDefinition()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateProfileSettings(ProfileData)
	 */
	public void updateProfileSettings(ProfileData profileData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.updateProfileSettings()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		profileDataDao.updateProfile(profileData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update Platform Settings duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.updateProfileSettings()");
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateSensorType(SensorTypeData)
	 */
	public void updateSensorType(SensorTypeData sensorTypeData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ConfigurationInterfaceDataAccessService.updateSensorType()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		sensorTypeDataDao.updateSensorType(sensorTypeData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update Sensor type duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ConfigurationInterfaceDataAccessService.updateSensorType()");
		}
	}

	public void setEnvironmentDataDao(EnvironmentDataDao environmentDataDao) {
		this.environmentDataDao = environmentDataDao;
	}

	public void setProfileDataDao(ProfileDataDao profileDataDao) {
		this.profileDataDao = profileDataDao;
	}

	public void setSensorTypeDataDao(SensorTypeDataDao sensorTypeDataDao) {
		this.sensorTypeDataDao = sensorTypeDataDao;
	}

	public void setExceptionSensorDefinitionDataDao(ExceptionSensorDefinitionDataDao exceptionSensorDefinitionDataDao) {
		this.exceptionSensorDefinitionDataDao = exceptionSensorDefinitionDataDao;
	}

	public void setMethodSensorDefinitionDataDao(MethodSensorDefinitionDataDao methodSensorDefinitionDataDao) {
		this.methodSensorDefinitionDataDao = methodSensorDefinitionDataDao;
	}

	public void setPlatformSensorDefinitionDataDao(PlatformSensorDefinitionDataDao platformSensorDefinitionDataDao) {
		this.platformSensorDefinitionDataDao = platformSensorDefinitionDataDao;
	}

	/**
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
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
