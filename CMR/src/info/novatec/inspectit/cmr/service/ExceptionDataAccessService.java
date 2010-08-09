package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Service class for retrieving {@link ExceptionSensorData} objects from the
 * CMR.
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
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get ungrouped exception overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, limit, fromDate, toDate);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get ungrouped exception overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get ungrouped exception overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getUngroupedExceptionOverview(template, fromDate, toDate);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get ungrouped exception overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getUngroupedExceptionOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getExceptionTree()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getExceptionTree(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get exception tree duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getExceptionTree()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getDataForGroupedExceptionOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get data for grouped exception overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getDataForGroupedExceptionOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getDataForGroupedExceptionOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getDataForGroupedExceptionOverview(template, fromDate, toDate);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get data for grouped exception overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getDataForGroupedExceptionOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getStackTracesForErrorMessage(ExceptionSensorData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getStackTracesForErrorMessage()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getStackTracesForErrorMessage(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get stack traces for error message duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getStackTracesForErrorMessage()");
		}

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
