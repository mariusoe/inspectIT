package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.indexing.query.factory.impl.HttpTimerDataQueryFactory;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.util.Date;
import java.util.List;

/**
 * {@link IHttpTimerDataAccessService} for storage purposes.
 *
 * @author Ivan Senic
 *
 */
public class StorageHttpTimerDataAccessService extends AbstractStorageService<HttpTimerData> implements IHttpTimerDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<HttpTimerData> indexingTree;

	/**
	 * Index query provider.
	 */
	private HttpTimerDataQueryFactory<StorageIndexQuery> httpDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return this.findAllHttpTimers(httpData, null, null, new HttpTimerDataAggregator(false, true, includeRequestMethod));

	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return this.findAllHttpTimers(httpData, fromDate, toDate, new HttpTimerDataAggregator(false, true, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return this.findAllHttpTimers(httpData, null, null, new HttpTimerDataAggregator(false, false, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return this.findAllHttpTimers(httpData, fromDate, toDate, new HttpTimerDataAggregator(false, false, includeRequestMethod));
	}

	/**
	 * Return all <code>HttpTimerData</code> objects in the buffer. Currently this is the best
	 * approach as the querying does not feature a better way of specifying elements.
	 *
	 * @param httpData
	 *            <code>HttpTimerData</code> object used to retrieve the platformId
	 * @param fromDate
	 *            the fromDate or <code>null</code> if not applicable
	 * @param toDate
	 *            the toDate or <code>null</code> if not applicable
	 * @param aggregator
	 *            {@link IAggregator} to include. Null for no aggregation.
	 * @return all <code>HttpTimerData</code> objects in the buffer.
	 */
	private List<HttpTimerData> findAllHttpTimers(HttpTimerData httpData, Date fromDate, Date toDate, IAggregator<HttpTimerData> aggregator) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, aggregator);
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<HttpTimerData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<HttpTimerData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param httpDataQueryFactory the httpDataQueryFactory to set
	 */
	public void setHttpDataQueryFactory(HttpTimerDataQueryFactory<StorageIndexQuery> httpDataQueryFactory) {
		this.httpDataQueryFactory = httpDataQueryFactory;
	}

}
