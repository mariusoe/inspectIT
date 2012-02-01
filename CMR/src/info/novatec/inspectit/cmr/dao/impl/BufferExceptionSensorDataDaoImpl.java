package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator.ExceptionAggregationType;
import info.novatec.inspectit.indexing.query.factory.impl.ExceptionSensorDataQueryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {@link ExceptionSensorDataDao} that works woth the data from the buffer.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferExceptionSensorDataDaoImpl extends AbstractBufferDataDao<ExceptionSensorData> implements ExceptionSensorDataDao {

	/**
	 * {@link IAggregator} for {@link ExceptionSensorData} for the grouped overview.
	 */
	private static final ExceptionDataAggregator GROUP_EXCEPTION_OVERVIEW_AGGREGATOR = new ExceptionDataAggregator(ExceptionAggregationType.GROUP_EXCEPTION_OVERVIEW);

	/**
	 * {@link IAggregator} for {@link ExceptionSensorData} for the distinct stack traces.
	 */
	private static final ExceptionDataAggregator DISTINCT_STACK_TRACES_AGGREGATOR = new ExceptionDataAggregator(ExceptionAggregationType.DISTINCT_STACK_TRACES);

	/**
	 * Comparator used for comparing the time stamps of {@link ExceptionSensorData}.
	 */
	private static final Comparator<ExceptionSensorData> TIMESTAMP_COMPARATOR = new Comparator<ExceptionSensorData>() {
		public int compare(ExceptionSensorData o1, ExceptionSensorData o2) {
			return o2.getTimeStamp().compareTo(o1.getTimeStamp());
		}
	};

	/**
	 * Index query provider.
	 */
	@Autowired
	private ExceptionSensorDataQueryFactory<IIndexQuery> exceptionSensorDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit) {
		return this.getUngroupedExceptionOverview(template, limit, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getUngroupedExceptionOverviewQuery(template, limit, fromDate, toDate);
		return super.executeQuery(query, TIMESTAMP_COMPARATOR, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template) {
		return this.getUngroupedExceptionOverview(template, -1, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		return this.getUngroupedExceptionOverview(template, -1, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getExceptionTreeQuery(template);
		List<ExceptionSensorData> results = super.executeQuery(query);
		Collections.reverse(results);
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		return this.getDataForGroupedExceptionOverview(template, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getDataForGroupedExceptionOverviewQuery(template, fromDate, toDate);
		List<ExceptionSensorData> results = super.executeQuery(query, GROUP_EXCEPTION_OVERVIEW_AGGREGATOR);
		List<AggregatedExceptionSensorData> aggResults = new ArrayList<AggregatedExceptionSensorData>();
		for (ExceptionSensorData exData : results) {
			if (exData instanceof AggregatedExceptionSensorData) {
				aggResults.add((AggregatedExceptionSensorData) exData);
			}
		}
		return aggResults;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getStackTraceMessagesForThrowableTypeQuery(template);
		return super.executeQuery(query, DISTINCT_STACK_TRACES_AGGREGATOR);
	}

}
