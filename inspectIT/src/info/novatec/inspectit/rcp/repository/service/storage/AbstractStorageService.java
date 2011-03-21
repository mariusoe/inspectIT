package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.util.HttpDataRetriever;
import info.novatec.inspectit.storage.IStorageIdProvider;

import java.util.ArrayList;
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
	 * {@link IStorageIdProvider}.
	 */
	private IStorageIdProvider storageIdProvider;

	/**
	 * {@link HttpDataRetriever}.
	 */
	private HttpDataRetriever httpDataRetriever;

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
	 * @return the storageData
	 */
	public IStorageIdProvider getStorageIdProvider() {
		return storageIdProvider;
	}

	/**
	 * @param storageIdProvider
	 *            the storageIdProvider to set
	 */
	public void setStorageIdProvider(IStorageIdProvider storageIdProvider) {
		this.storageIdProvider = storageIdProvider;
	}

	/**
	 * @return the httpDataRetriever
	 */
	public HttpDataRetriever getHttpDataRetriever() {
		return httpDataRetriever;
	}

	/**
	 * @param httpDataRetriever
	 *            the httpDataRetriever to set
	 */
	public void setHttpDataRetriever(HttpDataRetriever httpDataRetriever) {
		this.httpDataRetriever = httpDataRetriever;
	}

	/**
	 * Returns the indexing tree that can be used for querying.
	 * 
	 * @return Returns the indexing tree that can be used for querying.
	 */
	protected abstract IStorageTreeComponent<E> getIndexingTree();

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
	 * @return Return results of a query.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator) {
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
				List<E> allData = httpDataRetriever.getDataViaHttp(cmrRepositoryDefinition, storageIdProvider, limitedDescriptors);
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

		if (null != aggregator) {
			return aggregationPerformer.getResultList();
		} else {
			return returnList;
		}
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
}
