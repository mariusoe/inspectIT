package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.communication.data.SqlStatementData;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Patrice Bouillet
 * 
 */
public class SqlDataAccessService implements ISqlDataAccessService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(SqlDataAccessService.class);

	/**
	 * The sql DAO.
	 */
	private SqlDataDao sqlDataDao;

	/**
	 * {@inheritDoc}
	 */
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> SqlDataAccessService.getAggregatedSqlStatements()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<SqlStatementData> result = sqlDataDao.getAggregatedSqlStatements(sqlStatementData);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get aggregated sql statements duration: " + ((System.nanoTime() - time) / 1000000));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- SqlDataAccessService.getAggregatedSqlStatements()");
		}

		return result;
	}

	/**
	 * @param sqlDataDao
	 *            the sqlDataDao to set
	 */
	public void setSqlDataDao(SqlDataDao sqlDataDao) {
		this.sqlDataDao = sqlDataDao;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-SQL Data Access Service active...");
		}
	}

}
