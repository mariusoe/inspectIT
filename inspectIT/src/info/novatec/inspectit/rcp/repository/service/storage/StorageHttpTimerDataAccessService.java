package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.communication.data.HttpTimerData;
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
		StorageIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, null, null);
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, null, null);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod));
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
	 * @param httpDataQueryFactory
	 *            the httpDataQueryFactory to set
	 */
	public void setHttpDataQueryFactory(HttpTimerDataQueryFactory<StorageIndexQuery> httpDataQueryFactory) {
		this.httpDataQueryFactory = httpDataQueryFactory;
	}

}
