package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.query.factory.impl.SqlStatementDataQueryFactory;
import info.novatec.inspectit.indexing.query.provider.impl.IndexQueryProvider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class BufferSqlDataDaoImpl implements SqlDataDao {

	/**
	 * Indexing tree to search for data.
	 */
	@Autowired
	private IBufferTreeComponent<SqlStatementData> indexingTree;

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

		List<SqlStatementData> allSqlStatements = indexingTree.query(query);
		Map<Integer, SqlStatementData> aggregatedStatementsMap = new HashMap<Integer, SqlStatementData>();
		List<SqlStatementData> aggregatedSqlStatements = new ArrayList<SqlStatementData>();
		for (SqlStatementData sqlStatement : allSqlStatements) {
			int key = getMapKey(sqlStatement);
			SqlStatementData aggregatedStatement = aggregatedStatementsMap.get(key);
			if (null != aggregatedStatement) {
				aggregatedStatement.aggregateTimerData(sqlStatement);
			} else {
				SqlStatementData clone = cloneSqlStatementData(sqlStatement);
				clone.aggregateTimerData(sqlStatement);
				aggregatedStatementsMap.put(key, clone);
				aggregatedSqlStatements.add(clone);
			}
		}
		return aggregatedSqlStatements;
	}

	/**
	 * Returns map key based on the grouping of the SQL statements for
	 * {@link #getAggregatedSqlStatements(SqlStatementData)}.
	 *
	 * @param sqlStatementData
	 *            SQL data that map key is needed for.
	 * @return Map key.
	 */
	private int getMapKey(SqlStatementData sqlStatementData) {
		final int prime = 31;
		int result = 0;
		result = prime * result + (int) (sqlStatementData.getMethodIdent() ^ (sqlStatementData.getMethodIdent() >>> 32));
		result = prime * result + ((sqlStatementData.getSql() == null) ? 0 : sqlStatementData.getSql().hashCode());
		return result;
	}

	/**
	 * Clones the SQL statement data, so that aggregated values are put in the new object, thus
	 * keeping the original objects in the buffer.
	 *
	 * @param sqlStatement
	 *            Statement to be cloned.
	 * @return Cloned SQL statement.
	 */
	private SqlStatementData cloneSqlStatementData(SqlStatementData sqlStatement) {
		SqlStatementData clone = new SqlStatementData();
		clone.setPlatformIdent(sqlStatement.getPlatformIdent());
		clone.setMethodIdent(sqlStatement.getMethodIdent());
		clone.setSql(sqlStatement.getSql());
		return clone;
	}

}
