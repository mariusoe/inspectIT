package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {@link ExceptionSensorDataDao} that works woth the data from the buffer.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferExceptionSensorDataDaoImpl implements ExceptionSensorDataDao {

	/**
	 * Root branch to search in.
	 */
	@Autowired
	private ITreeComponent<ExceptionSensorData> indexingTree;

	/**
	 * Index query provider.
	 */
	@Autowired
	private IndexQueryProvider indexQueryProvider;

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
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(template.getPlatformIdent());
		if (template.getSensorTypeIdent() != -1) {
			query.setSensorTypeIdent(template.getSensorTypeIdent());
		}
		if (template.getMethodIdent() != -1) {
			query.setMethodIdent(template.getMethodIdent());
		}
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(ExceptionSensorData.class);
		query.setObjectClasses(searchedClasses);
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("exceptionEvent", ExceptionEvent.CREATED));
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}

		List<ExceptionSensorData> results = indexingTree.query(query);
		Collections.sort(results, new Comparator<DefaultData>() {
			public int compare(DefaultData o1, DefaultData o2) {
				return o2.getTimeStamp().compareTo(o1.getTimeStamp());
			}
		});

		List<ExceptionSensorData> returnList;
		if (results.size() < limit || limit == -1) {
			returnList = results;
		} else {
			returnList = new ArrayList<ExceptionSensorData>();
			returnList.addAll(results.subList(0, limit));
		}
		return returnList;
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
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(ExceptionSensorData.class);
		query.setObjectClasses(searchedClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("throwableIdentityHashCode", template.getThrowableIdentityHashCode()));
		List<ExceptionSensorData> results = indexingTree.query(query);
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
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(ExceptionSensorData.class);
		query.setObjectClasses(searchedClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		List<ExceptionSensorData> results = indexingTree.query(query);
		Map<Integer, AggregatedExceptionSensorData> aggregatedMap = new HashMap<Integer, AggregatedExceptionSensorData>();
		List<AggregatedExceptionSensorData> aggregatedResults = new ArrayList<AggregatedExceptionSensorData>();
		for (ExceptionSensorData exceptionData : results) {
			performAggregation(exceptionData, aggregatedMap, aggregatedResults);
		}
		return aggregatedResults;
	}

	/**
	 * Aggregates set of exception data for the purpose of
	 * {@link #getDataForGroupedExceptionOverview(ExceptionSensorData, Date, Date)}.
	 * 
	 * @param exceptionData
	 *            Exception data to be aggregated.
	 * @param aggregatedMap
	 *            map where aggregated data resists.
	 * @param aggregatedResults
	 *            List of aggregated results.
	 * 
	 * @see #getDataForGroupedExceptionOverview(ExceptionSensorData)
	 * @see #getDataForGroupedExceptionOverview(ExceptionSensorData, Date, Date)
	 */
	private void performAggregation(ExceptionSensorData exceptionData, Map<Integer, AggregatedExceptionSensorData> aggregatedMap, List<AggregatedExceptionSensorData> aggregatedResults) {
		int key = getGroupExceptionOverviewMapKey(exceptionData);
		AggregatedExceptionSensorData aggregatedExceptionData = aggregatedMap.get(key);
		if (null != aggregatedExceptionData) {
			aggregatedExceptionData.aggregateExceptionData(exceptionData);
		} else {
			AggregatedExceptionSensorData clone = cloneExceptionSensorData(exceptionData);
			clone.aggregateExceptionData(exceptionData);
			aggregatedMap.put(key, clone);
			aggregatedResults.add(clone);
		}
		if (null != exceptionData.getChild()) {
			performAggregation(exceptionData.getChild(), aggregatedMap, aggregatedResults);
		}
	}

	/**
	 * Returns the mapping key for the purpose of aggregation in
	 * {@link #aggregateExceptionData(ExceptionSensorData, Map, List)}.
	 * 
	 * @param exceptionSensorData
	 *            Exception data key is needed for.
	 * @return Map key.
	 */
	private int getGroupExceptionOverviewMapKey(ExceptionSensorData exceptionSensorData) {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((exceptionSensorData.getThrowableType() == null) ? 0 : exceptionSensorData.getThrowableType().hashCode());
		result = prime * result + ((exceptionSensorData.getErrorMessage() == null) ? 0 : exceptionSensorData.getErrorMessage().hashCode());
		return result;
	}

	/**
	 * Clones the exception data, so that aggregation is done in a new object, thus not altering any
	 * of the objects in buffer.
	 * 
	 * @param exceptionData
	 *            Exception data to be cloned.
	 * @return New exception data object.
	 */
	private AggregatedExceptionSensorData cloneExceptionSensorData(ExceptionSensorData exceptionData) {
		AggregatedExceptionSensorData clone = new AggregatedExceptionSensorData();
		clone.setCause(exceptionData.getCause());
		clone.setErrorMessage(exceptionData.getErrorMessage());
		clone.setExceptionEvent(exceptionData.getExceptionEvent());
		clone.setParameterContentData(exceptionData.getParameterContentData());
		clone.setThrowableType(exceptionData.getThrowableType());
		clone.setStackTrace(exceptionData.getStackTrace());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(ExceptionSensorData.class);
		query.setObjectClasses(searchedClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("throwableType", template.getThrowableType()));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.isNotNull("stackTrace"));
		List<ExceptionSensorData> results = indexingTree.query(query);
		Map<Integer, AggregatedExceptionSensorData> distinctStackTraceErrorCombination = new HashMap<Integer, AggregatedExceptionSensorData>();
		List<ExceptionSensorData> returnList = new ArrayList<ExceptionSensorData>();
		for (ExceptionSensorData exceptionData : results) {
			ExceptionSensorData dataToAggregate = exceptionData;
			while (null != dataToAggregate) {
				int key = getDisctinctStackTraceErrorCombinatonKey(exceptionData);
				AggregatedExceptionSensorData aggregatedExceptionSensorData = distinctStackTraceErrorCombination.get(key);
				if (null == aggregatedExceptionSensorData) {
					aggregatedExceptionSensorData = cloneExceptionSensorData(dataToAggregate);
					distinctStackTraceErrorCombination.put(key, aggregatedExceptionSensorData);
					returnList.add(aggregatedExceptionSensorData);
				}
				aggregatedExceptionSensorData.aggregateExceptionData(dataToAggregate);
				dataToAggregate =  dataToAggregate.getChild();
			}
		}
		return returnList;
	}

	/**
	 * Map key for aggregation for purpose of
	 * {@link #getStackTraceMessagesForThrowableType(ExceptionSensorData)}.
	 * 
	 * @param exceptionSensorData
	 *            data
	 * @return map key
	 */
	private int getDisctinctStackTraceErrorCombinatonKey(ExceptionSensorData exceptionSensorData) {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((exceptionSensorData.getErrorMessage() == null) ? 0 : exceptionSensorData.getErrorMessage().hashCode());
		result = prime * result + ((exceptionSensorData.getStackTrace() == null) ? 0 : exceptionSensorData.getStackTrace().hashCode());
		return result;
	}

}
