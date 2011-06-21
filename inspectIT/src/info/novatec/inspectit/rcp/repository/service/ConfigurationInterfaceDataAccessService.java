package info.novatec.inspectit.rcp.repository.service;

import info.novatec.inspectit.cmr.service.IConfigurationInterfaceDataAccessService;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Collections;
import java.util.List;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * 
 * @author Matthias Huber
 * 
 */
public class ConfigurationInterfaceDataAccessService implements IConfigurationInterfaceDataAccessService {

	/**
	 * The configuration data access service name.
	 */
	private static final String CONFIGURATION_INTERFACE_DATA_ACCESS_SERVICE = "ConfigurationInterfaceDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the data access service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The configuration interface data access service exposed by the CMR and initialized by Spring.
	 */
	private final IConfigurationInterfaceDataAccessService configurationInterfaceDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public ConfigurationInterfaceDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IConfigurationInterfaceDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + CONFIGURATION_INTERFACE_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		configurationInterfaceDataAccessService = (IConfigurationInterfaceDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addEnvironment(EnvironmentData)
	 */
	public long addEnvironment(EnvironmentData environmentData) {
		try {
			return configurationInterfaceDataAccessService.addEnvironment(environmentData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error adding the environment", e, -1);
			// TODO MH: exception handling
			return -1;
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addExceptionSensorDefinition(ExceptionSensorDefinitionData)
	 */
	public long addExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		try {
			return configurationInterfaceDataAccessService.addExceptionSensorDefinition(exceptionSensorDefinitionData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error adding of the exception sensor definition", e, -1);
			// TODO MH: exception handling
			return -1;
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addMethodSensorDefinition(MethodSensorDefinitionData)
	 */
	public long addMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		try {
			return configurationInterfaceDataAccessService.addMethodSensorDefinition(methodSensorDefinitionData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error adding of the method sensor definition", e, -1);
			// TODO MH: exception handling
			return -1;
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addPlatformSensorDefinition(PlatformSensorDefinitionData)
	 */
	public long addPlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		try {
			return configurationInterfaceDataAccessService.addPlatformSensorDefinition(platformSensorDefinitionData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an during adding of the platform sensor definitions", e, -1);
			// TODO MH: exception handling
			return -1;
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#addProfile(ProfileData)
	 */
	public long addProfile(ProfileData profileData) {
		try {
			return configurationInterfaceDataAccessService.addProfile(profileData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error adding the profile", e, -1);
			// TODO MH: exception handling
			return -1;
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteEnvironment(long)
	 */
	public void deleteEnvironment(long environmentId) {
		try {
			configurationInterfaceDataAccessService.deleteEnvironment(environmentId);
		} catch (EntityNotFoundException e) {
			InspectIT.getDefault().createErrorDialog("The environment you want to delete could not be found", e, -1);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error deleting the environment", e, -1);
		}
		// TODO MH: handle EntityNotFoundException
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteExceptionSensorDefinition(long)
	 */
	public void deleteExceptionSensorDefinition(long exceptionSensorDefinitionId) {
		try {
			configurationInterfaceDataAccessService.deleteExceptionSensorDefinition(exceptionSensorDefinitionId);
		} catch (EntityNotFoundException e) {
			InspectIT.getDefault().createErrorDialog("The exception sensor definition you want to delete could not be found", e, -1);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error deleting the exception sensor definition", e, -1);
		}
		// TODO MH: handle EntityNotFoundException
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteMethodSensorDefinition(long)
	 */
	public void deleteMethodSensorDefinition(long methodSensorDefinitionId) {
		try {
			configurationInterfaceDataAccessService.deleteMethodSensorDefinition(methodSensorDefinitionId);
		} catch (EntityNotFoundException e) {
			InspectIT.getDefault().createErrorDialog("The method sensor definition you want to delete could not be found", e, -1);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error deleting the method sensor definition", e, -1);
		}
		// TODO MH: handle EntityNotFoundException
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deletePlatformSensorDefinition(long)
	 */
	public void deletePlatformSensorDefinition(long platformSensorDefinitionId) {
		try {
			configurationInterfaceDataAccessService.deletePlatformSensorDefinition(platformSensorDefinitionId);
		} catch (EntityNotFoundException e) {
			InspectIT.getDefault().createErrorDialog("The platform sensor definition you want to delete could not be found", e, -1);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error deleting the platform sensor definition", e, -1);
		}
		// TODO MH: handle EntityNotFoundException
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteProfile(long)
	 */
	public void deleteProfile(long profileId) {
		try {
			configurationInterfaceDataAccessService.deleteProfile(profileId);
		} catch (EntityNotFoundException e) {
			InspectIT.getDefault().createErrorDialog("The profile you want to delete could not be found", e, -1);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error deleting the profile!", e, -1);
		}
		// TODO MH: handle EntityNotFoundException
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#deleteSensorType(long)
	 */
	public void deleteSensorType(long sensorTypeId) throws EntityNotFoundException {
		try {
			configurationInterfaceDataAccessService.deleteSensorType(sensorTypeId);
		} catch (EntityNotFoundException e) {
			InspectIT.getDefault().createErrorDialog("The sensor type you want to delete could not be found", e, -1);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error deleting the sensor type", e, -1);
		}
		// TODO MH: handle EntityNotFoundException
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#getEnvironments()
	 */
	public List<EnvironmentData> getEnvironments() {
		try {
			return configurationInterfaceDataAccessService.getEnvironments();
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the environments from the CMR!", e, -1);
			return Collections.emptyList();
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#getProfile(long)
	 */
	public ProfileData getProfile(long profileId) {
		try {
			return configurationInterfaceDataAccessService.getProfile(profileId);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the profile from the CMR!", e, -1);
			return null;
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateEnvironmentSettings(EnvironmentData)
	 */
	public void updateEnvironmentSettings(EnvironmentData environmentData) {
		try {
			configurationInterfaceDataAccessService.updateEnvironmentSettings(environmentData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error updating the Environment Settings!", e, -1);
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateExceptionSensorDefinition(ExceptionSensorDefinitionData)
	 */
	public void updateExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		try {
			configurationInterfaceDataAccessService.updateExceptionSensorDefinition(exceptionSensorDefinitionData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error updating the Exception Sensor Definition!", e, -1);
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateMethodSensorDefinition(MethodSensorDefinitionData)
	 */
	public void updateMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		try {
			configurationInterfaceDataAccessService.updateMethodSensorDefinition(methodSensorDefinitionData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error updating the Method Sensor Definition!", e, -1);
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updatePlatformSensorDefinition(PlatformSensorDefinitionData)
	 */
	public void updatePlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		try {
			configurationInterfaceDataAccessService.updatePlatformSensorDefinition(platformSensorDefinitionData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error updating the Platform Sensor Definition!", e, -1);
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateProfileSettings(ProfileData)
	 */
	public void updateProfileSettings(ProfileData profileData) {
		try {
			configurationInterfaceDataAccessService.updateProfileSettings(profileData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error updating the Profile Settings!", e, -1);
		}
	}

	/**
	 * @see IConfigurationInterfaceDataAccessService#updateSensorType(SensorTypeData)
	 */
	public void updateSensorType(SensorTypeData sensorTypeData) {
		try {
			configurationInterfaceDataAccessService.updateSensorType(sensorTypeData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error updating the Sensor Type", e, -1);
		}
	}
}
