package info.novatec.novaspy.cmr.service;

import info.novatec.novaspy.cmr.dao.CombinedMetricsDataDao;
import info.novatec.novaspy.cmr.util.Converter;
import info.novatec.novaspy.communication.data.ParameterContentData;
import info.novatec.novaspy.communication.data.TimerData;

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
	public List<TimerData> getCombinedMetrics(TimerData template, String workflowName, String activityName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> CombinedMetricsDataAccessService.getCombinedMetrics()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<TimerData> result = combinedMetricsDataDao.getCombinedMetrics(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get combined metrics duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- CombinedMetricsDataAccessService.getCombinedMetrics()");
		}

		return result;
	}

	public List<TimerData> getCombinedMetricsIgnoreMethodId(TimerData template, String workflowName, String activityName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> CombinedMetricsDataAccessService.getCombinedMetricsIgnoreMethodId()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<TimerData> result = combinedMetricsDataDao.getCombinedMetricsIgnoreMethodId(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get combined metrics duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- CombinedMetricsDataAccessService.getCombinedMetricsIgnoreMethodId()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<TimerData> getCombinedMetricsFromToDate(TimerData template, Date fromDate, Date toDate, String workflowName, String activityName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> CombinedMetricsDataAccessService.getCombinedMetricsFromToDate()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<TimerData> result = combinedMetricsDataDao.getCombinedMetricsFromToDate(template, workflowName, activityName, fromDate, toDate);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get combined metrics from to date duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- CombinedMetricsDataAccessService.getCombinedMetricsFromToDate()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ParameterContentData> getWorkflows(TimerData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> CombinedMetricsDataAccessService.getWorkflows()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ParameterContentData> result = combinedMetricsDataDao.getWorkflows(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get combined metrics from to date duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- CombinedMetricsDataAccessService.getWorkflows()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ParameterContentData> getActivities(TimerData template, String workflow) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> CombinedMetricsDataAccessService.getActivities()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ParameterContentData> result = combinedMetricsDataDao.getActivities(template, workflow);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get combined metrics from to date duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- CombinedMetricsDataAccessService.getActivities()");
		}

		return result;
	}

	/**
	 * @param combinedMetricsDataDao
	 */
	public void setCombinedMetricsDataDao(CombinedMetricsDataDao combinedMetricsDataDao) {
		this.combinedMetricsDataDao = combinedMetricsDataDao;
	}

	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Combined Metrics Data Access Service active...");
		}
	}
}
