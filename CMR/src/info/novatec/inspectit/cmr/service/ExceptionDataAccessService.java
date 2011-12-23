package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.spring.logger.Logger;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for retrieving {@link ExceptionSensorData} objects from the CMR.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Service
public class ExceptionDataAccessService implements IExceptionDataAccessService {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The exception sensor DAO.
	 */
	@Autowired
	private ExceptionSensorDataDao exceptionSensorDataDao;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getExceptionTree(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		List<AggregatedExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		List<AggregatedExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		List<ExceptionSensorData> result = exceptionSensorDataDao.getStackTraceMessagesForThrowableType(template);
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
			log.info("|-Exception Sensor Data Access Service active...");
		}
	}
}
