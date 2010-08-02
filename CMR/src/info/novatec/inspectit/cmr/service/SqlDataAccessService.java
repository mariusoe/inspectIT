package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
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
	@Log
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		List<SqlStatementData> result = sqlDataDao.getAggregatedSqlStatements(sqlStatementData);
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
