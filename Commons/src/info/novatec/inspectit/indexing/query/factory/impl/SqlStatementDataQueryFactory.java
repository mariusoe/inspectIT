package info.novatec.inspectit.indexing.query.factory.impl;

import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.AbstractQueryFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Factory for all queries for the {@link SqlStatementData}.
 *
 * @author Ivan Senic
 *
 * @param <E>
 */
@Component
public class SqlStatementDataQueryFactory<E extends IIndexQuery> extends AbstractQueryFactory<E> {

	/**
	 * Returns the query for aggregating the {@link TimerData}.
	 *
	 * @param sqlStatementData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return Query object.
	 */
	public E getAggregatedSqlStatementsQuery(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
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
		return query;
	}
}
