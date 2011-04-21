package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.communication.DefaultData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link IBranchIndexer} that indexes on the timestamp of the {@link DefaultData}. The index is
 * calculated in the way that a key in made for each {@link #indexingPeriod/1000} seconds of time.2
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class TimestampIndexer<E extends DefaultData> extends AbstractIndexer<E> {

	/**
	 * Indexing period.
	 */
	private static final long INDEXING_PERIOD = 60 * 1000;

	/**
	 * Set of created time stamp keys.
	 */
	private Set<Long> createdKeysSet = new HashSet<Long>();
	
	/**
	 * Min created key. Used for providing keys for queries.
	 */
	private long minCreatedKey = Long.MAX_VALUE;
	
	/**
	 * Max created key. Used for providing keys for queries.
	 */
	private long maxCreatedKey = 0;

	/**
	 * Default constructor. No child indexer is set.
	 */
	public TimestampIndexer() {
		super();
	}

	/**
	 * Constructor that defines child indexer and child branch type. See
	 * {@link AbstractIndexer#AbstractIndexer(ChildBranchType, IBranchIndexer)}
	 * 
	 * @param childBranchType 
	 * @param branchIndexer 
	 * 
	 */
	public TimestampIndexer(ChildBranchType childBranchType, IBranchIndexer<E> branchIndexer) {
		super(childBranchType, branchIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (null == element.getTimeStamp()) {
			return null;
		}
		long key = getKey(element.getTimeStamp());
		createdKeysSet.add(key);
		if (key < minCreatedKey) {
			minCreatedKey = key;
		}
		if (key > maxCreatedKey) {
			maxCreatedKey = key;
		}
		return key;
	}

	/**
	 * {@inheritDoc}
	 */

	public Object[] getKeys(IIndexQuery query) {
		if (!query.isIntervalSet()) {
			return null;
		}
		
		long startKey = 0;
		if (null != query.getFromDate()) {
			startKey = getKey(query.getFromDate());
		} 
		if (startKey < minCreatedKey) {
			startKey = minCreatedKey;
		}
		
		long endKey = Long.MAX_VALUE;
		if (null != query.getToDate()) {
			endKey = getKey(query.getToDate());
		} 
		if (endKey > maxCreatedKey) {
			endKey = maxCreatedKey;
		}
		
		int size = (int) ((endKey - startKey) / INDEXING_PERIOD + 1);
		ArrayList<Object> keysList = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			long key = startKey + i * INDEXING_PERIOD;
			if (createdKeysSet.contains(key)) {
				keysList.add(key);
			}
		}
		return keysList.toArray(new Object[keysList.size()]);
	}

	/**
	 * Returns proper key for given timestamp.
	 * 
	 * @param timestamp
	 *            Timestamp to map.
	 * @return Mapping key.
	 */
	private long getKey(Timestamp timestamp) {
		return timestamp.getTime() - timestamp.getTime() % INDEXING_PERIOD;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean sharedInstance() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBranchIndexer<E> getNewInstance() {
		return new TimestampIndexer<E>(super.getChildBranchType(), super.getChildIndexer());
	}

}
