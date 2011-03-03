package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.DefaultData;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Patrice Bouillet
 * 
 */
@Service
public class GlobalDataAccessService implements IGlobalDataAccessService {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(GlobalDataAccessService.class);

	/**
	 * The platform ident DAO.
	 */
	@Autowired
	private PlatformIdentDao platformIdentDao;

	/**
	 * The default data DAO.
	 */
	@Autowired
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

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Global Data Access Service active...");
		}
	}

}
