package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.Comparator;
import java.util.List;

/**
 * Comparators for {@link SqlStatementData}.
 * 
 * @author Ivan Senic
 * 
 */
public enum SqlStatementDataComparatorEnum implements IDataComparator<SqlStatementData>, Comparator<SqlStatementData> {

	/**
	 * Sort by if the statement is prepared or not.
	 */
	IS_PREPARED_STATEMENT,

	/**
	 * Sort by SQL string.
	 */
	SQL,

	/**
	 * Sort by parameter values.
	 */
	PARAMETERS,

	/**
	 * Sort by both SQL string and parameter values.
	 */
	SQL_AND_PARAMETERS;

	/**
	 * {@inheritDoc}
	 */
	public int compare(SqlStatementData o1, SqlStatementData o2, CachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(SqlStatementData o1, SqlStatementData o2) {
		switch (this) {
		case IS_PREPARED_STATEMENT:
			return Boolean.valueOf(o1.isPreparedStatement()).compareTo(Boolean.valueOf(o2.isPreparedStatement()));
		case SQL:
			return ObjectUtils.compare(o1.getSql(), o2.getSql());
		case PARAMETERS:
			List<String> parameterList1 = o1.getParameterValues();
			List<String> parameterList2 = o2.getParameterValues();
			return ObjectUtils.compare(parameterList1, parameterList2);
		case SQL_AND_PARAMETERS:
			int result = SQL.compare(o1, o2);
			if (0 != result) {
				return result;
			} else {
				return PARAMETERS.compare(o1, o2);
			}
		default:
			return 0;
		}
	}

}
