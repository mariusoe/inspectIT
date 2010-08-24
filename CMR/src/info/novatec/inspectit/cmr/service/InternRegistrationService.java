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
import info.novatec.inspectit.cmr.service.IRegistrationService;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 * The default implementation of the {@link IRegistrationService} interface. Uses different DAO's to
 * save the objects to the database.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InternRegistrationService {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(InternRegistrationService.class);

	/**
	 * The platform ident DAO.
	 */
	private PlatformIdentDao platformIdentDao;

	/**
	 * The method ident DAO.
	 */
	private MethodIdentDao methodIdentDao;

	/**
	 * The method sensor type ident DAO.
	 */
	private MethodSensorTypeIdentDao methodSensorTypeIdentDao;

	/**
	 * The platform sensor type ident DAO.
	 */
	private PlatformSensorTypeIdentDao platformSensorTypeIdentDao;

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public synchronized long registerPlatformIdent(List definedIPs, String agentName, String version) {
		PlatformIdent platformIdent = new PlatformIdent();
		platformIdent.setDefinedIPs(definedIPs);
		platformIdent.setAgentName(agentName);

		// need to reset the version number, otherwise it will be used for the
		// query
		platformIdent.setVersion(null);

		// we will not set the version for the platformIdent object here as we use this object for a
		// QBE (Query by example) and this query should not be performed based on the version
		// information.

		List<PlatformIdent> platformIdentResults = platformIdentDao.findByExample(platformIdent);
		if (1 == platformIdentResults.size()) {
			platformIdent = platformIdentResults.get(0);
		} else if (platformIdentResults.size() > 1) {
			// this cannot occur anymore, if it occurs, then there is something
			// totally wrong!
			LOGGER.fatal("More than one platform ident has been retrieved! Please send your Database to the NovaTec inspectIT support!");
		}

		// always update the time stamp, no matter if this is an old or new
		// record.
		platformIdent.setTimeStamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));

		// also always update the version information of the agent
		platformIdent.setVersion(version);

		platformIdentDao.saveOrUpdate(platformIdent);

		return platformIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public long registerMethodIdent(long platformId, String packageName, String className, String methodName, List parameterTypes, String returnType, int modifiers) {
		PlatformIdent platformIdent = platformIdentDao.load(platformId);

		MethodIdent methodIdent = new MethodIdent();
		methodIdent.setPackageName(packageName);
		methodIdent.setClassName(className);
		methodIdent.setMethodName(methodName);
		if (null == parameterTypes) {
			parameterTypes = Collections.EMPTY_LIST;
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
	@SuppressWarnings("unchecked")
	@Transactional
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
	@SuppressWarnings("unchecked")
	@Transactional
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
	@SuppressWarnings("unchecked")
	@Transactional
	public long registerPlatformSensorTypeIdent(long platformId, String fullyQualifiedClassName) {
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

	public void setPlatformIdentDao(PlatformIdentDao platformIdentDao) {
		this.platformIdentDao = platformIdentDao;
	}

	public void setMethodIdentDao(MethodIdentDao methodIdentDao) {
		this.methodIdentDao = methodIdentDao;
	}

	public void setMethodSensorTypeIdentDao(MethodSensorTypeIdentDao methodSensorTypeIdentDao) {
		this.methodSensorTypeIdentDao = methodSensorTypeIdentDao;
	}

	public void setPlatformSensorTypeIdentDao(PlatformSensorTypeIdentDao platformSensorTypeIdentDao) {
		this.platformSensorTypeIdentDao = platformSensorTypeIdentDao;
	}

}
