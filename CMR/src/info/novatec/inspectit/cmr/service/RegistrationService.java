package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.util.LicenseUtil;

import java.rmi.RemoteException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.schlichtherle.license.LicenseContentException;

/**
 * This class is used as a delegator to the real registration service. It is needed because Spring
 * weaves a proxy around the real registration service which cannot be used in an RMI context with
 * Java 1.4 (as no pre-generated stub is available).
 * 
 * @author Patrice Bouillet
 * 
 */
@Service
public class RegistrationService implements IRegistrationService {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(RegistrationService.class);

	/**
	 * The 'real' registration service.
	 */
	@Autowired
	private IInternRegistrationService internRegistrationService;

	/**
	 * The license utility to check for a valid license and abort the registration of the agent if
	 * necessary.
	 */
	@Autowired
	private LicenseUtil licenseUtil;

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatformIdent(List<String> definedIPs, String agentName, String version) throws RemoteException {
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
	public long registerMethodIdent(long platformId, String packageName, String className, String methodName, List<String> parameterTypes, String returnType, int modifiers) {
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

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Registration Service active...");
		}
	}

}
