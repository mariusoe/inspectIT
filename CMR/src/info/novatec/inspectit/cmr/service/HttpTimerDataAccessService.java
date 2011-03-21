package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.HttpTimerDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.spring.logger.Logger;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class provides access to the http related data in the CMR.
 * 
 * @author Stefan Siegl
 */
@Service
public class HttpTimerDataAccessService implements IHttpTimerDataAccessService, InitializingBean {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The Dao.
	 */
	@Autowired
	private HttpTimerDataDao dao;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<HttpTimerData> getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return dao.getAggregatedHttpTimerData(httpData, includeRequestMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<HttpTimerData> getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return dao.getAggregatedHttpTimerData(httpData, includeRequestMethod, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return dao.getTaggedAggregatedHttpTimerData(httpData, includeRequestMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return dao.getTaggedAggregatedHttpTimerData(httpData, includeRequestMethod, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Http Timer Data Access Service active...");
		}
	}

}
