package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.query.factory.impl.TimerDataQueryFactory;

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
	private IBufferTreeComponent<TimerData> indexingTree;

	/**
	 * Index query factory.
	 */
	@Autowired
	private TimerDataQueryFactory<IIndexQuery> timerDataQueryFactory;

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
		IIndexQuery query = timerDataQueryFactory.getAggregatedTimerDataQuery(timerData, fromDate, toDate);
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
