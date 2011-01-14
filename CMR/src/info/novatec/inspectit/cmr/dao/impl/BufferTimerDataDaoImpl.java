package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TimerData} that searches for timer data in buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferTimerDataDaoImpl implements TimerDataDao {

	/**
	 * Indexing tree to search for data.
	 */
	private ITreeComponent<TimerData> indexingTree;

	/**
	 * Index query provider.
	 */
	private IndexQueryProvider indexQueryProvider;

	/**
	 * {@inheritDoc}
	 */
	public List<TimerData> getAggregatedTimerData(TimerData timerData) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(timerData.getPlatformIdent());
		query.setObjectClass(TimerData.class);
		List<TimerData> allTimerData = indexingTree.query(query);
		Map<Long, TimerData> aggregatedMap = new HashMap<Long, TimerData>();
		List<TimerData> aggregatedResults = new ArrayList<TimerData>();
		for (TimerData tData : allTimerData) {
			long key = tData.getMethodIdent();
			TimerData aggregatedTimerData = aggregatedMap.get(key);
			if (null != aggregatedTimerData) {
				aggregateTimerData(aggregatedTimerData, tData);
			} else {
				TimerData clone = cloneTimerData(tData);
				aggregateTimerData(clone, tData);
				aggregatedMap.put(key, clone);
				aggregatedResults.add(clone);
			}
		}
		return aggregatedResults;
	}

	/**
	 * Returns cloned timer data, with copied platform ident, sensor type ident and method ident
	 * from a given timer data.
	 * 
	 * @param timerData
	 *            timer data to copy values to
	 * @return cloned object
	 */
	private TimerData cloneTimerData(TimerData timerData) {
		TimerData clone = new TimerData();
		clone.setPlatformIdent(timerData.getPlatformIdent());
		clone.setSensorTypeIdent(timerData.getSensorTypeIdent());
		clone.setMethodIdent(timerData.getMethodIdent());
		return clone;
	}

	/**
	 * Aggregates timer data.
	 * 
	 * @param aggregatedTimerData
	 *            Timer data that values will be aggregated to
	 * @param timerData
	 *            Other timer data
	 */
	private void aggregateTimerData(TimerData aggregatedTimerData, TimerData timerData) {
		aggregatedTimerData.setCount(aggregatedTimerData.getCount() + timerData.getCount());
		aggregatedTimerData.setDuration(aggregatedTimerData.getDuration() + timerData.getDuration());
		aggregatedTimerData.setAverage(aggregatedTimerData.getDuration() / aggregatedTimerData.getCount());
		if (aggregatedTimerData.getMax() < timerData.getMax()) {
			aggregatedTimerData.setMax(timerData.getMax());
		}
		if (aggregatedTimerData.getMin() > timerData.getMin()) {
			aggregatedTimerData.setMin(timerData.getMin());
		}
		aggregatedTimerData.setCpuDuration(aggregatedTimerData.getCpuDuration() + timerData.getCpuDuration());
		aggregatedTimerData.setCpuAverage(aggregatedTimerData.getCpuDuration() / aggregatedTimerData.getCount());
		if (aggregatedTimerData.getCpuMax() < timerData.getCpuMax()) {
			aggregatedTimerData.setCpuMax(timerData.getCpuMax());
		}
		if (aggregatedTimerData.getCpuMin() > timerData.getCpuMin()) {
			aggregatedTimerData.setCpuMin(timerData.getCpuMin());
		}
		if (null != timerData.getInvocationParentsIdSet()) {
			for (Object invocationId : timerData.getInvocationParentsIdSet()) {
				aggregatedTimerData.addInvocationParentId((Long) invocationId);
			}
		}
	}

	/**
	 * 
	 * @param indexingTree
	 *            Indexing tree to be set.
	 */
	public void setBuffer(ITreeComponent<TimerData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * 
	 * @param indexQueryProvider
	 *            Index query provider to be set.
	 */
	public void setIndexQueryProvider(IndexQueryProvider indexQueryProvider) {
		this.indexQueryProvider = indexQueryProvider;
	}

}
