package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Timer data service.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataAccessService implements ITimerDataAccessService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TimerDataAccessService.class);
	
	/**
	 * Timer data dao.
	 */
	private TimerDataDao timerDataDao;

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<TimerData> getAggregatedTimerData(TimerData timerData) {
		List<TimerData> result = timerDataDao.getAggregatedTimerData(timerData);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<TimerData> getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate) {
		List<TimerData> result = timerDataDao.getAggregatedTimerData(timerData, fromDate, toDate);
		return result;
	}

	/**
	 * 
	 * @param timerDataDao
	 *            timer data dao to set
	 */
	public void setTimerDataDao(TimerDataDao timerDataDao) {
		this.timerDataDao = timerDataDao;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Timer Data Access Service active...");
		}
	}

}
