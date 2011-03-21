package info.novatec.inspectit.indexing.aggregation.impl;

import java.io.Serializable;

import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

/**
 * {@link IAggregator} for {@link SqlStatementData}.
 *
 * @author Ivan Senic
 *
 */
public class SqlStatementDataAggregator implements IAggregator<SqlStatementData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -2226935151962665996L;

	/**
	 * Is cloning active.
	 */
	private boolean cloning;

	/**
	 * Default constructor.
	 *
	 * @param cloning
	 *            Should cloning be used or not.
	 */
	public SqlStatementDataAggregator(boolean cloning) {
		this.cloning = cloning;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(SqlStatementData aggregatedObject, SqlStatementData objectToAdd) {
		aggregatedObject.aggregateTimerData(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public SqlStatementData getClone(SqlStatementData sqlStatementData) {
		SqlStatementData clone = new SqlStatementData();
		clone.setPlatformIdent(sqlStatementData.getPlatformIdent());
		clone.setMethodIdent(sqlStatementData.getMethodIdent());
		clone.setSql(sqlStatementData.getSql());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCloning() {
		return cloning;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(SqlStatementData sqlStatementData) {
		final int prime = 31;
		int result = 0;
		result = prime * result + (int) (sqlStatementData.getMethodIdent() ^ (sqlStatementData.getMethodIdent() >>> 32));
		result = prime * result + ((sqlStatementData.getSql() == null) ? 0 : sqlStatementData.getSql().hashCode());
		return result;
	}

}
