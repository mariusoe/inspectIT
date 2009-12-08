package info.novatec.novaspy.cmr.service;

import info.novatec.novaspy.cmr.dao.ExceptionSensorDataDao;
import info.novatec.novaspy.cmr.util.Converter;
import info.novatec.novaspy.communication.data.ExceptionSensorData;

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
	public List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template, int limit) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getExceptionTreeOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getExceptionTreeOverview(template, limit);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get exception tree overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getExceptionTreeOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getExceptionTreeOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getExceptionTreeOverview(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get exception tree overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getExceptionTreeOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getExceptionTreeDetails(ExceptionSensorData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> ExceptionDataAccessService.getExceptionTreeDetails()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<ExceptionSensorData> result = exceptionSensorDataDao.getExceptionTreeDetails(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get exception tree overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- ExceptionDataAccessService.getExceptionTreeDetails()");
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
