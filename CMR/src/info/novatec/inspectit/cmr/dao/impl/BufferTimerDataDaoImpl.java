package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
		return this.getAggregatedTimerData(timerData, null, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<TimerData> getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(timerData.getPlatformIdent());
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(TimerData.class);
		query.setObjectClasses(searchedClasses);
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		
		List<TimerData> allTimerData = indexingTree.query(query);
		Map<Long, TimerData> aggregatedMap = new HashMap<Long, TimerData>();
		List<TimerData> aggregatedResults = new ArrayList<TimerData>();
		for (TimerData tData : allTimerData) {
			long key = tData.getMethodIdent();
			TimerData aggregatedTimerData = aggregatedMap.get(key);
			if (null != aggregatedTimerData) {
				aggregatedTimerData.aggregateTimerData(tData);
			} else {
				TimerData clone = cloneTimerData(tData);
				clone.aggregateTimerData(tData);
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
