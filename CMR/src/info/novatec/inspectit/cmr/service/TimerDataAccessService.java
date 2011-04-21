package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Date;
import java.util.List;

/**
 * Timer data service.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataAccessService implements ITimerDataAccessService {

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

}
