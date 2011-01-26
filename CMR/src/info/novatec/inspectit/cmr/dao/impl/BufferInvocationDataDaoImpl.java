package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of {@link InvocationDataDao} that works with the data from the buffer indexing
 * tree.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferInvocationDataDaoImpl implements InvocationDataDao {

	/**
	 * Tree to look for data.
	 */
	private ITreeComponent<InvocationSequenceData> indexingTree;

	/**
	 * Index query provider.
	 */
	private IndexQueryProvider indexQueryProvider;

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(platformId);
		query.setMethodIdent(methodId);
		query.setObjectClass(InvocationSequenceData.class);
		List<InvocationSequenceData> results = indexingTree.query(query);
		Collections.sort(results, new Comparator<DefaultData>() {
			public int compare(DefaultData o1, DefaultData o2) {
				return o2.getTimeStamp().compareTo(o1.getTimeStamp());
			}
		});
		List<InvocationSequenceData> returnList = new ArrayList<InvocationSequenceData>();
		int i = 0;
		for (DefaultData data : results) {
			if (i < limit || -1 == limit) {
				returnList.add(cloneInvocationSequence((InvocationSequenceData) data));
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
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit) {
		return this.getInvocationSequenceOverview(platformId, 0, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		return indexingTree.get(template);
	}

	/**
	 * Clone invocation sequence. This method returns new object exacly same as the input object,
	 * but with out nested sequences set.
	 * 
	 * @param invData
	 *            Invocation sequence to be cloned.
	 * @return Cloned invocation sequence.
	 */
	private InvocationSequenceData cloneInvocationSequence(InvocationSequenceData invData) {
		InvocationSequenceData clone = new InvocationSequenceData(invData.getTimeStamp(), invData.getPlatformIdent(), invData.getSensorTypeIdent(), invData.getMethodIdent());
		clone.setId(invData.getId());
		clone.setChildCount(invData.getChildCount());
		clone.setDuration(invData.getDuration());
		clone.setEnd(invData.getEnd());
		clone.setNestedSequences(Collections.EMPTY_LIST);
		clone.setParameterContentData(invData.getParameterContentData());
		clone.setParentSequence(invData.getParentSequence());
		clone.setPosition(invData.getPosition());
		clone.setSqlStatementData(invData.getSqlStatementData());
		clone.setTimerData(invData.getTimerData());
		clone.setStart(invData.getStart());
		return clone;
	}

	/**
	 * 
	 * @param indexingTree
	 *            Root branch of indexing tree to query.
	 */
	public void setIndexingTree(ITreeComponent<InvocationSequenceData> indexingTree) {
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
