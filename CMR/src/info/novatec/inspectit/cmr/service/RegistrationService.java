package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.MethodIdentDao;
import info.novatec.inspectit.cmr.dao.MethodSensorTypeIdentDao;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.dao.PlatformSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.util.LicenseUtil;
import info.novatec.inspectit.spring.logger.Logger;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The platform ident DAO.
	 */
	@Autowired
	PlatformIdentDao platformIdentDao;

	/**
	 * The method ident DAO.
	 */
	@Autowired
	MethodIdentDao methodIdentDao;

	/**
	 * The method sensor type ident DAO.
	 */
	@Autowired
	MethodSensorTypeIdentDao methodSensorTypeIdentDao;

	/**
	 * The platform sensor type ident DAO.
	 */
	@Autowired
	PlatformSensorTypeIdentDao platformSensorTypeIdentDao;

	/**
	 * The license utility to check for a valid license and abort the registration of the agent if
	 * necessary.
	 */
	@Autowired
	LicenseUtil licenseUtil;

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public synchronized long registerPlatformIdent(List<String> definedIPs, String agentName, String version) throws LicenseException, RemoteException {
		try {
			if (log.isInfoEnabled()) {
				log.info("Trying to register Agent '" + agentName + "'");
			}

			licenseUtil.validateLicense(definedIPs, agentName);
			PlatformIdent platformIdent = new PlatformIdent();
			platformIdent.setDefinedIPs(definedIPs);
			platformIdent.setAgentName(agentName);

			// need to reset the version number, otherwise it will be used for the query
			platformIdent.setVersion(null);

			// we will not set the version for the platformIdent object here as we use this object
			// for a QBE (Query by example) and this query should not be performed based on the
			// version information.

			List<PlatformIdent> platformIdentResults = platformIdentDao.findByExample(platformIdent);
			if (1 == platformIdentResults.size()) {
				platformIdent = platformIdentResults.get(0);
			} else if (platformIdentResults.size() > 1) {
				// this cannot occur anymore, if it occurs, then there is something totally wrong!
				log.fatal("More than one platform ident has been retrieved! Please send your Database to the NovaTec inspectIT support!");
			}

			// always update the time stamp, no matter if this is an old or new record.
			platformIdent.setTimeStamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));

			// also always update the version information of the agent
			platformIdent.setVersion(version);

			platformIdentDao.saveOrUpdate(platformIdent);

			if (log.isInfoEnabled()) {
				log.info("Successfully registered Agent '" + agentName + "' with id: " + platformIdent.getId() + " and version " + version);
			}

			return platformIdent.getId();
		} catch (LicenseContentException e) {
			log.error("Could not register the Agent, due to a license problem: " + e.getMessage());
			throw new LicenseException(e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public long registerMethodIdent(long platformId, String packageName, String className, String methodName, List<String> parameterTypes, String returnType, int modifiers) throws RemoteException {
		PlatformIdent platformIdent = platformIdentDao.load(platformId);

		MethodIdent methodIdent = new MethodIdent();
		methodIdent.setPackageName(packageName);
		methodIdent.setClassName(className);
		methodIdent.setMethodName(methodName);
		if (null == parameterTypes) {
			parameterTypes = Collections.emptyList();
		}
		methodIdent.setParameters(parameterTypes);
		methodIdent.setReturnType(returnType);
		methodIdent.setModifiers(modifiers);

		List<MethodIdent> methodIdents = methodIdentDao.findForPlatformIdent(platformIdent, methodIdent);
		if (1 == methodIdents.size()) {
			methodIdent = methodIdents.get(0);
		}

		// always update the time stamp, no matter if this is an old or new
		// record.
		methodIdent.setTimeStamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));

		methodIdent.setPlatformIdent(platformIdent);
		methodIdentDao.saveOrUpdate(methodIdent);

		return methodIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public long registerMethodSensorTypeIdent(long platformId, String fullyQualifiedClassName) throws RemoteException {
		PlatformIdent platformIdent = platformIdentDao.load(platformId);

		MethodSensorTypeIdent methodSensorTypeIdent = new MethodSensorTypeIdent();
		methodSensorTypeIdent.setFullyQualifiedClassName(fullyQualifiedClassName);

		List<MethodSensorTypeIdent> methodSensorTypeIdents = methodSensorTypeIdentDao.findByExample(methodSensorTypeIdent);
		if (1 == methodSensorTypeIdents.size()) {
			methodSensorTypeIdent = methodSensorTypeIdents.get(0);
		}

		Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
		sensorTypeIdents.add(methodSensorTypeIdent);

		methodSensorTypeIdentDao.saveOrUpdate(methodSensorTypeIdent);
		platformIdentDao.saveOrUpdate(platformIdent);

		return methodSensorTypeIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public void addSensorTypeToMethod(long methodSensorTypeId, long methodId) throws RemoteException {
		MethodIdent methodIdent = methodIdentDao.load(methodId);
		MethodSensorTypeIdent methodSensorTypeIdent = methodSensorTypeIdentDao.load(methodSensorTypeId);

		Set<MethodSensorTypeIdent> methodSensorTypeIdents = methodIdent.getMethodSensorTypeIdents();
		methodSensorTypeIdents.add(methodSensorTypeIdent);

		methodSensorTypeIdentDao.saveOrUpdate(methodSensorTypeIdent);
		methodIdentDao.saveOrUpdate(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public long registerPlatformSensorTypeIdent(long platformId, String fullyQualifiedClassName) throws RemoteException {
		PlatformIdent platformIdent = platformIdentDao.load(platformId);

		PlatformSensorTypeIdent platformSensorTypeIdent = new PlatformSensorTypeIdent();
		platformSensorTypeIdent.setFullyQualifiedClassName(fullyQualifiedClassName);

		List<PlatformSensorTypeIdent> platformSensorTypeIdents = platformSensorTypeIdentDao.findByExample(platformSensorTypeIdent);
		if (1 == platformSensorTypeIdents.size()) {
			platformSensorTypeIdent = platformSensorTypeIdents.get(0);
		}

		Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
		sensorTypeIdents.add(platformSensorTypeIdent);

		platformSensorTypeIdentDao.saveOrUpdate(platformSensorTypeIdent);
		platformIdentDao.saveOrUpdate(platformIdent);

		return platformSensorTypeIdent.getId();
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-Registration Service active...");
		}
	}

}
