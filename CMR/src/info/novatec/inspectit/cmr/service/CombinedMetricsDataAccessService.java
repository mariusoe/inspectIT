package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.CombinedMetricsDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CombinedMetricsDataAccessService implements ICombinedMetricsDataAccessService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CombinedMetricsDataAccessService.class);

	/**
	 * The exception sensor DAO.
	 */
	private CombinedMetricsDataDao combinedMetricsDataDao;

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<TimerData> getCombinedMetrics(TimerData template, String workflowName, String activityName) {
		List<TimerData> result = combinedMetricsDataDao.getCombinedMetrics(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<TimerData> getCombinedMetricsIgnoreMethodId(TimerData template, String workflowName, String activityName) {
		List<TimerData> result = combinedMetricsDataDao.getCombinedMetricsIgnoreMethodId(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Log
	public List<TimerData> getCombinedMetricsFromToDate(TimerData template, Date fromDate, Date toDate, String workflowName, String activityName) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<TimerData> result = combinedMetricsDataDao.getCombinedMetricsFromToDate(template, workflowName, activityName, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Log
	public List<ParameterContentData> getWorkflows(TimerData template) {
		List<ParameterContentData> result = combinedMetricsDataDao.getWorkflows(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Log
	public List<ParameterContentData> getActivities(TimerData template, String workflow) {
		List<ParameterContentData> result = combinedMetricsDataDao.getActivities(template, workflow);
		return result;
	}

	/**
	 * @param combinedMetricsDataDao
	 */
	public void setCombinedMetricsDataDao(CombinedMetricsDataDao combinedMetricsDataDao) {
		this.combinedMetricsDataDao = combinedMetricsDataDao;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Combined Metrics Data Access Service active...");
		}
	}

}
