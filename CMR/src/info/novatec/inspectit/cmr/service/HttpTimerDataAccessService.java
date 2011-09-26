package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.HttpTimerDataDao;
import info.novatec.inspectit.communication.data.HttpTimerData;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class provides access to the http related data in the CMR.
 * 
 * @author Stefan Siegl
 */
public class HttpTimerDataAccessService implements IHttpTimerDataAccessService, InitializingBean {
	
	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpTimerDataAccessService.class);

	/**
	 * The Dao.
	 */
	private HttpTimerDataDao dao;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return dao.getAggregatedHttpTimerData(httpData, includeRequestMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return dao.getAggregatedHttpTimerData(httpData, includeRequestMethod, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return dao.getTaggedAggregatedHttpTimerData(httpData, includeRequestMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return dao.getTaggedAggregatedHttpTimerData(httpData, includeRequestMethod, fromDate, toDate);
	}

	/**
	 * setter for injection.
	 * 
	 * @param dao
	 *            dao
	 */
	public void setDao(HttpTimerDataDao dao) {
		this.dao = dao;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Http Timer Data Access Service active...");
		}
	}

}
