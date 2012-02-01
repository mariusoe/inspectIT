package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.impl.InvocationSequenceDataQueryFactory;

import java.util.ArrayList;
import java.util.Collection;
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
public class BufferInvocationDataDaoImpl extends AbstractBufferDataDao<InvocationSequenceData> implements InvocationDataDao {

	/**
	 * Index query provider.
	 */
	@Autowired
	private InvocationSequenceDataQueryFactory<IIndexQuery> invocationDataQueryFactory;

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
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate);
		List<InvocationSequenceData> resultWithChildren = super.executeQuery(query, TIMESTAMP_COMPARATOR, limit);
		List<InvocationSequenceData> realResults = new ArrayList<InvocationSequenceData>();
		for (InvocationSequenceData invocationSequenceData : resultWithChildren) {
			realResults.add(invocationSequenceData.getClonedInvocationSequence());
		}
		return realResults;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, invocationIdCollection, limit);
		List<InvocationSequenceData> resultWithChildren = super.executeQuery(query, TIMESTAMP_COMPARATOR, limit);
		List<InvocationSequenceData> realResults = new ArrayList<InvocationSequenceData>();
		for (InvocationSequenceData invocationSequenceData : resultWithChildren) {
			realResults.add(invocationSequenceData.getClonedInvocationSequence());
		}
		return realResults;
	}

	/**
	 * {@inheritDoc}
	 */
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		return super.getIndexingTree().get(template);
	}

}
