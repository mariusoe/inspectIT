package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.query.factory.impl.InvocationSequenceDataQueryFactory;

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
	private IBufferTreeComponent<InvocationSequenceData> indexingTree;

	/**
	 * Index query provider.
	 */
	@Autowired
	private InvocationSequenceDataQueryFactory<IIndexQuery> invocationDataQueryFactory;

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
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate);
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
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, invocationIdCollection, limit);
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
