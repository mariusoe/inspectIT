package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.SqlStatementDataAggregator;
import info.novatec.inspectit.indexing.query.factory.impl.SqlStatementDataQueryFactory;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the {@link SqlDataDao} that searches for the SQL statements in the indexing
 * tree.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferSqlDataDaoImpl extends AbstractBufferDataDao<SqlStatementData> implements SqlDataDao {

	/**
	 * {@link IAggregator} used for {@link SqlStatementData}.
	 */
	private static final SqlStatementDataAggregator SQL_STATEMENT_DATA_AGGREGATOR = new SqlStatementDataAggregator(true);

	/**
	 * Index query provider.
	 */
	@Autowired
	private SqlStatementDataQueryFactory<IIndexQuery> sqlDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		return this.getAggregatedSqlStatements(sqlStatementData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		IIndexQuery query = sqlDataQueryFactory.getAggregatedSqlStatementsQuery(sqlStatementData, fromDate, toDate);
		return super.executeQuery(query, SQL_STATEMENT_DATA_AGGREGATOR);
	}

}
