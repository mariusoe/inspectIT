package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.cmr.service.LicenseException;
import info.novatec.inspectit.cmr.util.LicenseUtil;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import de.schlichtherle.license.LicenseContentException;

/**
 * This class is used as a delegator to the real registration service. It is needed because Spring
 * weaves a proxy around the real registration service which cannot be used in an RMI context with
 * Java 1.4 (as no pre-generated stub is available).
 * 
 * @author Patrice Bouillet
 * 
 */
public class RegistrationService implements IRegistrationService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(RegistrationService.class);

	/**
	 * The 'real' registration service.
	 */
	private InternRegistrationService internRegistrationService;

	/**
	 * The license utility to check for a valid license and abort the registration of the agent if
	 * necessary.
	 */
	private LicenseUtil licenseUtil;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public long registerPlatformIdent(List definedIPs, String agentName, String version) throws RemoteException {
		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Trying to register Agent '" + agentName + "'");
			}

			licenseUtil.validateLicense(definedIPs, agentName);
			long id = internRegistrationService.registerPlatformIdent(definedIPs, agentName, version);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Successfully registered Agent '" + agentName + "' with id: " + id + " and version " + version);
			}

			return id;
		} catch (LicenseContentException e) {
			LOGGER.error("Could not register the Agent, due to a license problem: " + e.getMessage());
			throw new LicenseException(e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public long registerMethodIdent(long platformId, String packageName, String className, String methodName, List parameterTypes, String returnType, int modifiers) {
		return internRegistrationService.registerMethodIdent(platformId, packageName, className, methodName, parameterTypes, returnType, modifiers);
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethodSensorTypeIdent(long platformId, String fullyQualifiedClassName) throws RemoteException {
		return internRegistrationService.registerMethodSensorTypeIdent(platformId, fullyQualifiedClassName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensorTypeToMethod(long methodSensorTypeId, long methodId) throws RemoteException {
		internRegistrationService.addSensorTypeToMethod(methodSensorTypeId, methodId);
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatformSensorTypeIdent(long platformId, String fullyQualifiedClassName) {
		return internRegistrationService.registerPlatformSensorTypeIdent(platformId, fullyQualifiedClassName);
	}

	public void setInternRegistrationService(InternRegistrationService internRegistrationService) {
		this.internRegistrationService = internRegistrationService;
	}

	public void setLicenseUtil(LicenseUtil licenseUtil) {
		this.licenseUtil = licenseUtil;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Registration Service active...");
		}
	}

}
