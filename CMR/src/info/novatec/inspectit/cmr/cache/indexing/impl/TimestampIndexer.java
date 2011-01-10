package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IndexQuery;
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
	 * Default constructor. No child indexer is set.
	 */
	public TimestampIndexer() {
		super();
	}

	/**
	 * Constructor that defines child indexer and child branch type. See
	 * {@link AbstractIndexer#AbstractIndexer(ChildBranchType, IBranchIndexer)}
	 * 
	 * @param childBrunchType 
	 * @param branchIndexer 
	 * 
	 */
	public TimestampIndexer(ChildBranchType childBrunchType, IBranchIndexer<E> branchIndexer) {
		super(childBrunchType, branchIndexer);
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
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IndexQuery query) {
		if (!query.isIntervalSet()) {
			return null;
		}
		long startKey = getKey(query.getFromDate());
		long endKey = getKey(query.getToDate());
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
