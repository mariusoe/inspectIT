package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.query.factory.impl.InvocationSequenceDataQueryFactory;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * {@link IInvocationDataAccessService} for storage purposes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageInvocationDataAccessService extends AbstractStorageService<InvocationSequenceData> implements IInvocationDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<InvocationSequenceData> indexingTree;

	/**
	 * Index query provider.
	 */
	private InvocationSequenceDataQueryFactory<StorageIndexQuery> invocationDataQueryFactory;

	/**
	 * Comparator used for comparing the time stamps of {@link ExceptionSensorData}.
	 */
	private static final Comparator<InvocationSequenceData> TIMESTAMP_COMPARATOR = new Comparator<InvocationSequenceData>() {
		public int compare(InvocationSequenceData o1, InvocationSequenceData o2) {
			return o2.getTimeStamp().compareTo(o1.getTimeStamp());
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		return this.getInvocationSequenceOverview(platformId, methodId, limit, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit) {
		return this.getInvocationSequenceOverview(platformId, 0, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate) {
		StorageIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate);
		query.setOnlyInvocationsWithoutChildren(true);
		return super.executeQuery(query, TIMESTAMP_COMPARATOR, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit) {
		StorageIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, invocationIdCollection, limit);
		query.setOnlyInvocationsWithoutChildren(true);
		return super.executeQuery(query, TIMESTAMP_COMPARATOR, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		// here we need to create new query since this one does not exist in factory
		StorageIndexQuery query = invocationDataQueryFactory.getIndexQueryProvider().getIndexQuery();
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(InvocationSequenceData.class);
		query.setObjectClasses(searchedClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		query.setMethodIdent(template.getMethodIdent());
		query.setSensorTypeIdent(template.getSensorTypeIdent());
		query.setOnlyInvocationsWithoutChildren(false);
		ArrayList<Long> includeIds = new ArrayList<Long>();
		includeIds.add(template.getId());
		query.setIncludeIds(includeIds);
		List<IStorageDescriptor> descriptors = indexingTree.query(query);
		List<InvocationSequenceData> results = getHttpDataRetriever().getDataViaHttp(getCmrRepositoryDefinition(), getStorageIdProvider(), descriptors);
		if (results.size() == 1) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<InvocationSequenceData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<InvocationSequenceData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param invocationDataQueryFactory
	 *            the invocationDataQueryFactory to set
	 */
	public void setInvocationDataQueryFactory(InvocationSequenceDataQueryFactory<StorageIndexQuery> invocationDataQueryFactory) {
		this.invocationDataQueryFactory = invocationDataQueryFactory;
	}

}
