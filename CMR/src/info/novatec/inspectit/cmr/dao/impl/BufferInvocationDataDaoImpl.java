package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link InvocationDataDao} that works with the data from the buffer indexing
 * tree.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferInvocationDataDaoImpl implements InvocationDataDao {

	/**
	 * Tree to look for data.
	 */
	@Autowired
	private ITreeComponent<InvocationSequenceData> indexingTree;

	/**
	 * Index query provider.
	 */
	@Autowired
	private IndexQueryProvider indexQueryProvider;
	
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
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, fromDate, toDate);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(platformId);
		query.setMethodIdent(methodId);
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(InvocationSequenceData.class);
		query.setObjectClasses(searchedClasses);
		if (fromDate != null) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (toDate != null) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		List<InvocationSequenceData> results = indexingTree.query(query);
		Collections.sort(results, new Comparator<DefaultData>() {
			public int compare(DefaultData o1, DefaultData o2) {
				return o2.getTimeStamp().compareTo(o1.getTimeStamp());
			}
		});
		List<InvocationSequenceData> returnList = new ArrayList<InvocationSequenceData>();
		int i = 0;
		for (InvocationSequenceData data : results) {
			if (i < limit || -1 == limit) {
				returnList.add(data.getClonedInvocationSequence());
			} else {
				break;
			}
			i++;
		}
		return returnList;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(platformId);
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(InvocationSequenceData.class);
		query.setObjectClasses(searchedClasses);
		query.addIndexingRestriction(IndexQueryRestrictionFactory.isInCollection("id", invocationIdCollection));
		List<InvocationSequenceData> results = indexingTree.query(query);
		Collections.sort(results, new Comparator<DefaultData>() {
			public int compare(DefaultData o1, DefaultData o2) {
				return o2.getTimeStamp().compareTo(o1.getTimeStamp());
			}
		});
		List<InvocationSequenceData> returnList = new ArrayList<InvocationSequenceData>();
		int i = 0;
		for (InvocationSequenceData data : results) {
			if (i < limit || -1 == limit) {
				returnList.add(data.getClonedInvocationSequence());
			} else {
				break;
			}
			i++;
		}
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		return indexingTree.get(template);
	}

}
