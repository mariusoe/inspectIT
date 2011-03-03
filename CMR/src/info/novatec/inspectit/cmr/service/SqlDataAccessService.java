package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.data.SqlStatementData;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Patrice Bouillet
 * 
 */
@Service
public class SqlDataAccessService implements ISqlDataAccessService {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(SqlDataAccessService.class);

	/**
	 * The sql DAO.
	 */
	@Autowired
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
	 * {@inheritDoc}
	 */
	@Log
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		List<SqlStatementData> result = sqlDataDao.getAggregatedSqlStatements(sqlStatementData, fromDate, toDate);
		return result;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-SQL Data Access Service active...");
		}
	}

}
