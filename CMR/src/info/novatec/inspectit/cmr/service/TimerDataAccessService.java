package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.log4j.Logger;

/**
 * Timer data service.
 * 
 * @author Ivan Senic
 * 
 */
@Service
public class TimerDataAccessService implements ITimerDataAccessService {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TimerDataAccessService.class);
	
	/**
	 * Timer data dao.
	 */
	@Autowired
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

}
