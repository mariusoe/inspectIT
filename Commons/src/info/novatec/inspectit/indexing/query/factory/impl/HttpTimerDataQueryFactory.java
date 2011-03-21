package info.novatec.inspectit.indexing.query.factory.impl;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.AbstractQueryFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Factory for all queries for the {@link HttpTimerData}.
 *
 * @author Ivan Senic
 *
 * @param <E>
 */
@Component
public class HttpTimerDataQueryFactory<E extends IIndexQuery> extends AbstractQueryFactory<E> {

	/**
	 * Return query for all <code>HttpTimerData</code> objects.
	 *
	 * @param httpData
	 *            <code>HttpTimerData</code> object used to retrieve the platformId
	 * @param fromDate
	 *            the fromDate or <code>null</code> if not applicable
	 * @param toDate
	 *            the toDate or <code>null</code> if not applicable
	 * @return Query for all <code>HttpTimerData</code> objects in the buffer.
	 */
	public E getFindAllHttpTimersQuery(HttpTimerData httpData, Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
		query.setPlatformIdent(httpData.getPlatformIdent());
		ArrayList<Class<?>> classesToSearch = new ArrayList<Class<?>>();
		classesToSearch.add(HttpTimerData.class);
		query.setObjectClasses(classesToSearch);
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		return query;
	}
}
