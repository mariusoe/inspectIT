package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.CombinedMetricsDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.spring.logger.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Service
public class CombinedMetricsDataAccessService implements ICombinedMetricsDataAccessService {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The exception sensor DAO.
	 */
	@Autowired
	private CombinedMetricsDataDao combinedMetricsDataDao;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<TimerData> getCombinedMetrics(TimerData template, String workflowName, String activityName) {
		List<TimerData> result = combinedMetricsDataDao.getCombinedMetrics(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<TimerData> getCombinedMetricsIgnoreMethodId(TimerData template, String workflowName, String activityName) {
		List<TimerData> result = combinedMetricsDataDao.getCombinedMetricsIgnoreMethodId(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
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
	@MethodLog
	public List<ParameterContentData> getWorkflows(TimerData template) {
		List<ParameterContentData> result = combinedMetricsDataDao.getWorkflows(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<ParameterContentData> getActivities(TimerData template, String workflow) {
		List<ParameterContentData> result = combinedMetricsDataDao.getActivities(template, workflow);
		return result;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Combined Metrics Data Access Service active...");
		}
	}

}
