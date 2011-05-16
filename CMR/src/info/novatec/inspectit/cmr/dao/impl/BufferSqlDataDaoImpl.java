package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.data.SqlStatementData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link SqlDataDao} that searches for the SQL statements in the indexing
 * tree.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferSqlDataDaoImpl implements SqlDataDao {

	/**
	 * Indexing tree to search for data.
	 */
	private ITreeComponent<SqlStatementData> indexingTree;

	/**
	 * Index query provider.
	 */
	private IndexQueryProvider indexQueryProvider;

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
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(sqlStatementData.getPlatformIdent());
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(SqlStatementData.class);
		query.setObjectClasses(searchedClasses);
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		
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

	/**
	 * 
	 * @param indexingTree
	 *            Indexing tree to be set.
	 */
	public void setBuffer(ITreeComponent<SqlStatementData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * 
	 * @param indexQueryProvider
	 *            Index query provider to be set.
	 */
	public void setIndexQueryProvider(IndexQueryProvider indexQueryProvider) {
		this.indexQueryProvider = indexQueryProvider;
	}

}
