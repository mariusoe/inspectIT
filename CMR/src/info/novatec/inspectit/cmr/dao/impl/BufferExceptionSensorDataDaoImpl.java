package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ExceptionSensorDataDao} that works woth the data from the buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferExceptionSensorDataDaoImpl implements ExceptionSensorDataDao {

	/**
	 * Root branch to search in.
	 */
	private ITreeComponent<ExceptionSensorData> indexingTree;

	/**
	 * Index query provider.
	 */
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
		query.setObjectClass(ExceptionSensorData.class);
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("exceptionEvent", ExceptionEventEnum.CREATED));
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
		query.setObjectClass(ExceptionSensorData.class);
		query.setPlatformIdent(template.getPlatformIdent());
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("throwableIdentityHashCode", template.getThrowableIdentityHashCode()));
		List<ExceptionSensorData> results = indexingTree.query(query);
		Collections.reverse(results);
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		return this.getDataForGroupedExceptionOverview(template, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setObjectClass(ExceptionSensorData.class);
		query.setPlatformIdent(template.getPlatformIdent());
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		List<ExceptionSensorData> results = indexingTree.query(query);
		Map<Integer, ExceptionSensorData> aggregatedMap = new HashMap<Integer, ExceptionSensorData>();
		List<ExceptionSensorData> aggregatedResults = new ArrayList<ExceptionSensorData>();
		for (ExceptionSensorData exceptionData : results) {
			aggregateExceptionData(exceptionData, aggregatedMap, aggregatedResults);
		}
		return aggregatedResults;
	}

	/**
	 * Aggregated exception data for the purpose of
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
	private void aggregateExceptionData(ExceptionSensorData exceptionData, Map<Integer, ExceptionSensorData> aggregatedMap, List<ExceptionSensorData> aggregatedResults) {
		int key = getGroupExceptionOverviewMapKey(exceptionData);
		ExceptionSensorData aggregatedExceptionData = aggregatedMap.get(key);
		if (null != aggregatedExceptionData) {
			aggregatedExceptionData.setThrowableIdentityHashCode(aggregatedExceptionData.getThrowableIdentityHashCode() + 1);
		} else {
			ExceptionSensorData clone = cloneExceptionSensorData(exceptionData);
			clone.setThrowableIdentityHashCode(1);
			aggregatedMap.put(key, clone);
			aggregatedResults.add(clone);
		}
		if (null != exceptionData.getChild()) {
			aggregateExceptionData(exceptionData.getChild(), aggregatedMap, aggregatedResults);
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
		result = prime * result + ((exceptionSensorData.getExceptionEvent() == null) ? 0 : exceptionSensorData.getExceptionEvent().hashCode());
		result = prime * result + ((exceptionSensorData.getErrorMessage() == null) ? 0 : exceptionSensorData.getErrorMessage().hashCode());
		result = prime * result + ((exceptionSensorData.getStackTrace() == null) ? 0 : exceptionSensorData.getStackTrace().hashCode());
		result = prime * result + ((exceptionSensorData.getCause() == null) ? 0 : exceptionSensorData.getCause().hashCode());
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
	private ExceptionSensorData cloneExceptionSensorData(ExceptionSensorData exceptionData) {
		ExceptionSensorData clone = new ExceptionSensorData();
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
	public List<ExceptionSensorData> getStackTracesForErrorMessage(ExceptionSensorData template) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setObjectClass(ExceptionSensorData.class);
		query.setPlatformIdent(template.getPlatformIdent());
		query.setMinId(template.getId());
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("errorMessage", template.getErrorMessage()));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.isNotNull("stackTrace"));
		List<ExceptionSensorData> results = indexingTree.query(query);
		Map<Integer, ExceptionSensorData> distinctStackTrace = new HashMap<Integer, ExceptionSensorData>();
		List<ExceptionSensorData> returnList = new ArrayList<ExceptionSensorData>();
		for (ExceptionSensorData exceptionData : results) {
			if (null == distinctStackTrace.get(exceptionData.getStackTrace().hashCode())) {
				distinctStackTrace.put(exceptionData.getStackTrace().hashCode(), exceptionData);
				returnList.add(exceptionData);
			}
		}
		return returnList;

	}

	/**
	 * 
	 * @param indexingTree
	 *            Root branch to search in.
	 */
	public void setIndexingTree(ITreeComponent<ExceptionSensorData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * 
	 * @param indexQueryProvider
	 *            Index query provider.
	 */
	public void setIndexQueryProvider(IndexQueryProvider indexQueryProvider) {
		this.indexQueryProvider = indexQueryProvider;
	}

}
