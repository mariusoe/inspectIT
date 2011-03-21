package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator.ExceptionAggregationType;
import info.novatec.inspectit.indexing.query.factory.impl.ExceptionSensorDataQueryFactory;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * {@link IExceptionDataAccessService} for storage purposes.
 *
 * @author Ivan Senic
 *
 */
public class StorageExceptionDataAccessService extends AbstractStorageService<ExceptionSensorData> implements IExceptionDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<ExceptionSensorData> indexingTree;

	/**
	 * Index query provider.
	 */
	private ExceptionSensorDataQueryFactory<StorageIndexQuery> exceptionSensorDataQueryFactory;

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
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getUngroupedExceptionOverviewQuery(template, limit, fromDate, toDate);
		List<ExceptionSensorData> results = super.executeQuery(query, null);

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
		// here we have a problem because we have to de-serialize every exception to find the right
		// one, we need to check if we can change this method
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getExceptionTreeQuery(template);
		List<ExceptionSensorData> results = super.executeQuery(query, null);
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
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getDataForGroupedExceptionOverviewQuery(template, fromDate, toDate);
		List<ExceptionSensorData> resultList = super.executeQuery(query, new ExceptionDataAggregator(ExceptionAggregationType.GROUP_EXCEPTION_OVERVIEW));
		List<AggregatedExceptionSensorData> filterList = new ArrayList<AggregatedExceptionSensorData>(resultList.size());
		for (ExceptionSensorData data : resultList) {
			if (data instanceof AggregatedExceptionSensorData) {
				filterList.add((AggregatedExceptionSensorData) data);
			}
		}
		return filterList;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		// same problem again, we need to de-serialize all exceptions
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getStackTraceMessagesForThrowableTypeQuery(template);
		return super.executeQuery(query, new ExceptionDataAggregator(ExceptionAggregationType.DISTINCT_STACK_TRACES));
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<ExceptionSensorData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<ExceptionSensorData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param exceptionSensorDataQueryFactory the exceptionSensorDataQueryFactory to set
	 */
	public void setExceptionSensorDataQueryFactory(ExceptionSensorDataQueryFactory<StorageIndexQuery> exceptionSensorDataQueryFactory) {
		this.exceptionSensorDataQueryFactory = exceptionSensorDataQueryFactory;
	}

}
