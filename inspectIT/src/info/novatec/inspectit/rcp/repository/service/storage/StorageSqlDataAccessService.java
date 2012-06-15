package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.aggregation.impl.SqlStatementDataAggregator;
import info.novatec.inspectit.indexing.query.factory.impl.SqlStatementDataQueryFactory;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.util.Date;
import java.util.List;

/**
 * {@link ISqlDataAccessService} for storage purposes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageSqlDataAccessService extends AbstractStorageService<SqlStatementData> implements ISqlDataAccessService {

	/**
	 * {@link info.novatec.inspectit.indexing.aggregation.IAggregator} used for
	 * {@link SqlStatementData}.
	 */
	private static final SqlStatementDataAggregator SQL_STATEMENT_DATA_AGGREGATOR = new SqlStatementDataAggregator(false);

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<SqlStatementData> indexingTree;

	/**
	 * Index query provider.
	 */
	private SqlStatementDataQueryFactory<StorageIndexQuery> sqlDataQueryFactory;

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
		StorageIndexQuery query = sqlDataQueryFactory.getAggregatedSqlStatementsQuery(sqlStatementData, fromDate, toDate);
		return super.executeQuery(query, SQL_STATEMENT_DATA_AGGREGATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<SqlStatementData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<SqlStatementData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param sqlDataQueryFactory
	 *            the sqlDataQueryFactory to set
	 */
	public void setSqlDataQueryFactory(SqlStatementDataQueryFactory<StorageIndexQuery> sqlDataQueryFactory) {
		this.sqlDataQueryFactory = sqlDataQueryFactory;
	}

}
