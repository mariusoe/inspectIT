package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.util.DataRetriever;
import info.novatec.inspectit.storage.LocalStorageData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract class for all storage services.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of data provided by the service.
 */
public abstract class AbstractStorageService<E extends DefaultData> {

	/**
	 * Default amount of data that will be requested by one HTTP request. 10MB.
	 */
	private static final int MAX_QUERY_SIZE = 1024 * 1024 * 10;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link LocalStorageData}.
	 */
	private LocalStorageData localStorageData;

	/**
	 * {@link DataRetriever}.
	 */
	private DataRetriever dataRetriever;

	/**
	 * Returns the indexing tree that can be used for querying.
	 * 
	 * @return Returns the indexing tree that can be used for querying.
	 */
	protected abstract IStorageTreeComponent<E> getIndexingTree();

	/**
	 * Executes the query on the indexing tree.
	 * 
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * 
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery) {
		return this.executeQuery(storageIndexQuery, null, null, -1);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}.
	 * 
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator) {
		return this.executeQuery(storageIndexQuery, aggregator, null, -1);
	}

	/**
	 * Executes the query on the indexing tree. Results can be sorted by comparator.
	 * 
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, Comparator<E> comparator) {
		return this.executeQuery(storageIndexQuery, null, comparator, -1);
	}

	/**
	 * Executes the query on the indexing tree. Furthermore the result list can be limited.
	 * 
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, int limit) {
		return this.executeQuery(storageIndexQuery, null, null, limit);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}.
	 * 
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator, Comparator<E> comparator) {
		return this.executeQuery(storageIndexQuery, aggregator, comparator, -1);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}. Furthermore the
	 * result list can be limited.
	 * 
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator, int limit) {
		return this.executeQuery(storageIndexQuery, aggregator, null, limit);
	}

	/**
	 * Executes the query on the indexing tree. Results can be sorted by comparator. Furthermore the
	 * result list can be limited.
	 * 
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * 
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, Comparator<E> comparator, int limit) {
		return this.executeQuery(storageIndexQuery, null, comparator, limit);
	}

	/**
	 * This method has the ability to load the data via the HTTP and aggregate the data if the
	 * {@link IAggregator} is provided. If the {@link IAggregator} is not provided, the data will be
	 * returned not aggregated.
	 * <P>
	 * This method should be used by all subclasses, because it guards against massive data loading
	 * that can make out of memory exceptions on the UI.
	 * 
	 * @param storageIndexQuery
	 *            Query.
	 * @param aggregator
	 *            {@link IAggregator}
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Return results of a query.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator, Comparator<E> comparator, int limit) {
		List<IStorageDescriptor> descriptors = getIndexingTree().query(storageIndexQuery);
		AggregationPerformer<E> aggregationPerformer = null;
		if (null != aggregator) {
			aggregationPerformer = new AggregationPerformer<E>(aggregator);
		}
		List<E> returnList = new ArrayList<E>();

		int size = 0;
		int count = 0;
		List<IStorageDescriptor> limitedDescriptors = new ArrayList<IStorageDescriptor>();
		for (IStorageDescriptor storageDescriptor : descriptors) {
			// increase count, add descriptor size and update current list
			count++;
			size += storageDescriptor.getSize();
			limitedDescriptors.add(storageDescriptor);

			// if the size is already to big, or we reached end do query
			if (size > MAX_QUERY_SIZE || count == descriptors.size()) {
				// load data and filter with restrictions
				List<E> allData;
				if (localStorageData.isFullyDownloaded()) {
					try {
						allData = dataRetriever.getDataLocally(localStorageData, descriptors);
					} catch (Exception e) {
						InspectIT.getDefault().createErrorDialog("Exception occured trying to load the data.", e, -1);
						return Collections.emptyList();
					}
				} else {
					try {
						allData = dataRetriever.getDataViaHttp(cmrRepositoryDefinition, localStorageData, limitedDescriptors);
					} catch (Exception e) {
						InspectIT.getDefault().createErrorDialog("Exception occured trying to load the data.", e, -1);
						return Collections.emptyList();
					}
				}
				List<E> passedData = getRestrictionsPassedList(allData, storageIndexQuery);

				// if we need to aggregate then do so, otherwise just add to result list
				if (null != aggregationPerformer) {
					aggregationPerformer.processCollection(passedData);
				} else {
					returnList.addAll(passedData);
				}

				// reset the size and current list
				size = 0;
				limitedDescriptors.clear();
			}
		}

		// aggregate if needed
		if (null != aggregator) {
			returnList = aggregationPerformer.getResultList();
		}

		// sort if needed
		if (null != comparator) {
			Collections.sort(returnList, comparator);
		}

		// limit the size if needed
		if (limit > -1 && returnList.size() > limit) {
			returnList = returnList.subList(0, limit);
		}

		return returnList;
	}

	/**
	 * This utility method is used to create a list of elements that pass all the restrictions in
	 * the {@link StorageIndexQuery}.
	 * 
	 * @param notPassedList
	 *            List of all elements.
	 * @param storageIndexQuery
	 *            {@link StorageIndexQuery}.
	 * @return New list only with elements that are passing all restrictions.
	 */
	private List<E> getRestrictionsPassedList(List<E> notPassedList, StorageIndexQuery storageIndexQuery) {
		List<E> passedList = new ArrayList<E>();
		for (E element : notPassedList) {
			if (null != element && element.isQueryComplied(storageIndexQuery)) {
				passedList.add(element);
			}
		}
		return passedList;
	}

	/**
	 * @return the cmrRepositoryDefinition
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * @param cmrRepositoryDefinition
	 *            the cmrRepositoryDefinition to set
	 */
	public void setCmrRepositoryDefinition(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * Gets {@link #localStorageData}.
	 * 
	 * @return {@link #localStorageData}
	 */
	public LocalStorageData getLocalStorageData() {
		return localStorageData;
	}

	/**
	 * Sets {@link #localStorageData}.
	 * 
	 * @param localStorageData
	 *            New value for {@link #localStorageData}
	 */
	public void setLocalStorageData(LocalStorageData localStorageData) {
		this.localStorageData = localStorageData;
	}

	/**
	 * @param dataRetriever
	 *            the httpDataRetriever to set
	 */
	public void setDataRetriever(DataRetriever dataRetriever) {
		this.dataRetriever = dataRetriever;
	}

}
