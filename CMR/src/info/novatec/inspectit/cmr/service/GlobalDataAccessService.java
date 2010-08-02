package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.DefaultData;

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
	@Log
	public List<PlatformIdent> getConnectedAgents() {
		// only load the ones which are currently connected
		List<PlatformIdent> result = platformIdentDao.findAllInitialized();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		List<DefaultData> result = defaultDataDao.findByExampleWithLastInterval(template, timeInterval);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public DefaultData getLastDataObject(DefaultData template) {
		DefaultData result = defaultDataDao.findByExampleLastData(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<DefaultData> result = defaultDataDao.findByExampleFromToDate(template, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<DefaultData> getDataObjectsSinceId(DefaultData template) {
		List<DefaultData> result = defaultDataDao.findByExampleSinceId(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		List<DefaultData> result = defaultDataDao.findByExampleSinceIdIgnoreMethodId(template);
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
