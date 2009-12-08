package info.novatec.novaspy.cmr.service;

import info.novatec.novaspy.cmr.dao.DefaultDataDao;
import info.novatec.novaspy.cmr.dao.PlatformIdentDao;
import info.novatec.novaspy.cmr.model.PlatformIdent;
import info.novatec.novaspy.cmr.util.Converter;
import info.novatec.novaspy.communication.DefaultData;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Patrice Bouillet
 * 
 */
public class GlobalDataAccessService implements IGlobalDataAccessService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(GlobalDataAccessService.class);

	/**
	 * The platform ident DAO.
	 */
	private PlatformIdentDao platformIdentDao;

	/**
	 * The default data DAO.
	 */
	private DefaultDataDao defaultDataDao;

	/**
	 * {@inheritDoc}
	 */
	public List<PlatformIdent> getConnectedAgents() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getConnectedAgents()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		// only load the ones which are currently connected
		List<PlatformIdent> result = platformIdentDao.findAllInitialized();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Load connected agents duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getConnectedAgents()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getLastDataObjects()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<DefaultData> result = defaultDataDao.findByExampleWithLastInterval(template, timeInterval);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get last data objects duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getLastDataObjects()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData getLastDataObject(DefaultData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getLastDataObject()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		DefaultData result = defaultDataDao.findByExampleLastData(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get last data object duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getLastDataObject()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getDataObjectsFromToDate()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<DefaultData> result = defaultDataDao.findByExampleFromToDate(template, fromDate, toDate);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get data objects from to date duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getDataObjectsFromToDate()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> getDataObjectsSinceId(DefaultData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getDataObjectsSinceId()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<DefaultData> result = defaultDataDao.findByExampleSinceId(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get data objects since id duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getDataObjectsSinceId()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getDataObjectsSinceIdIgnoreMethodId()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<DefaultData> result = defaultDataDao.findByExampleSinceIdIgnoreMethodId(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get data objects since id ignore method id duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getDataObjectsSinceIdIgnoreMethodId()");
		}

		return result;
	}

	public void setDefaultDataDao(DefaultDataDao defaultDataDao) {
		this.defaultDataDao = defaultDataDao;
	}

	public void setPlatformIdentDao(PlatformIdentDao platformIdentDao) {
		this.platformIdentDao = platformIdentDao;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Global Data Access Service active...");
		}
	}
}
