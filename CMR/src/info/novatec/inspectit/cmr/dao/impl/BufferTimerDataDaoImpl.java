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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link TimerData} that searches for timer data in buffer.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferTimerDataDaoImpl implements TimerDataDao {

	/**
	 * Indexing tree to search for data.
	 */
	@Autowired
	private ITreeComponent<TimerData> indexingTree;

	/**
	 * Index query provider.
	 */
	@Autowired
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
		// we need to add the subclasses that are timers manually as the search will not include
		// subclasses by default
		searchedClasses.add(TimerData.class);
		// HttpTimerData will not be shown in the timer data view (we also do not show SQL data).
		// searchedClasses.add(HttpTimerData.class);
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

}
