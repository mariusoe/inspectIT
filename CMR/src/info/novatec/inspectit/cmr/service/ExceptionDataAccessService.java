package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Service class for retrieving {@link ExceptionSensorData} objects from the CMR.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionDataAccessService implements IExceptionDataAccessService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ExceptionDataAccessService.class);

	/**
	 * The exception sensor DAO.
	 */
	private ExceptionSensorDataDao exceptionSensorDataDao;

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getExceptionTree(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		List<AggregatedExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		List<AggregatedExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template, fromDate, toDate);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getStackTraceMessagesForThrowableType(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Exception Sensor Data Access Service active...");
		}
	}

	/**
	 * 
	 * @param exceptionSensorDataDao
	 *            The {@link ExceptionSensorDataDao} to set.
	 */
	public void setExceptionSensorDataDao(ExceptionSensorDataDao exceptionSensorDataDao) {
		this.exceptionSensorDataDao = exceptionSensorDataDao;
	}
}
