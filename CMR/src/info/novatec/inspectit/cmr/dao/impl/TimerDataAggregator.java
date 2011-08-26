package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.communication.data.DatabaseAggregatedTimerData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Aggregator for the {@link TimerData} obejcts that need to be persisted to the DB.
 * 
 * @author Ivan Senic
 * @see https://confluence.novatec-gmbh.de/display/INSPECTIT/TimerData+Aggregator
 * 
 */
public class TimerDataAggregator extends HibernateDaoSupport {

	/**
	 * Period of time in which all timer data should be aggregated. In milliseconds.
	 */
	private long aggregationPeriod;

	/**
	 * Max elements in the cache.
	 */
	private int maxElements;

	/**
	 * Sleeping period for thread that is cleaning the cache (persisting the objects). In
	 * milliseconds.
	 */
	private long cacheCleanSleepingPeriod;

	/**
	 * Current element count in cache.
	 */
	private AtomicInteger elementCount;

	/**
	 * Map for caching.
	 */
	private ConcurrentHashMap<Integer, TimerData> map;

	/**
	 * Queue for knowing the order.
	 */
	private ConcurrentLinkedQueue<TimerData> queue;

	/**
	 * List of objects that are out of the cache and need to be persisted.
	 */
	private ConcurrentLinkedQueue<TimerData> persistList;

	/**
	 * Lock for persist all.
	 */
	private ReentrantLock persistAllLock;

	/**
	 * Pointer to the most recently added {@link TimerData} to the cache.
	 */
	private TimerData mostRecentlyAdded;

	/**
	 * Default constructor.
	 */
	public TimerDataAggregator() {
		super();

		elementCount = new AtomicInteger(0);
		map = new ConcurrentHashMap<Integer, TimerData>();
		queue = new ConcurrentLinkedQueue<TimerData>();
		persistList = new ConcurrentLinkedQueue<TimerData>();
		persistAllLock = new ReentrantLock();

		CacheCleaner cacheCleaner = new CacheCleaner();
		cacheCleaner.start();
	}

	/**
	 * @return the aggregationPeriod
	 */
	public long getAggregationPeriod() {
		return aggregationPeriod;
	}

	/**
	 * @param aggregationPeriod
	 *            the aggregationPeriod to set
	 */
	public void setAggregationPeriod(long aggregationPeriod) {
		this.aggregationPeriod = aggregationPeriod;
	}

	/**
	 * @return the maxElements
	 */
	public int getMaxElements() {
		return maxElements;
	}

	/**
	 * @param maxElements
	 *            the maxElements to set
	 */
	public void setMaxElements(int maxElements) {
		this.maxElements = maxElements;
	}

	/**
	 * @return the cacheCleanSleepingPeriod
	 */
	public long getCacheCleanSleepingPeriod() {
		return cacheCleanSleepingPeriod;
	}

	/**
	 * @param cacheCleanSleepingPeriod
	 *            the cacheCleanSleepingPeriod to set
	 */
	public void setCacheCleanSleepingPeriod(long cacheCleanSleepingPeriod) {
		this.cacheCleanSleepingPeriod = cacheCleanSleepingPeriod;
	}

	/**
	 * Aggregates the {@link TimerData} object and updates the cache. Note that the given object
	 * will not be modified by this method.
	 * 
	 * @param timerData
	 *            {@link TimerData} that holds values to be aggregated.
	 */
	public void processTimerData(TimerData timerData) {
		long aggregationTimestamp = getAlteredTimestamp(timerData);
		int cacheHash = getCacheHash(timerData.getPlatformIdent(), timerData.getMethodIdent(), aggregationTimestamp);

		persistAllLock.lock();
		try {
			TimerData aggTimerData = map.get(cacheHash);
			if (aggTimerData == null) {
				// we create a DB aggregated timer data because we don't want to alter objects that
				// are in the memory
				aggTimerData = new DatabaseAggregatedTimerData(new Timestamp(aggregationTimestamp), timerData.getPlatformIdent(), timerData.getSensorTypeIdent(), timerData.getMethodIdent());
				map.put(cacheHash, aggTimerData);
				queue.add(aggTimerData);
				mostRecentlyAdded = aggTimerData;

				int count = elementCount.incrementAndGet();
				if (maxElements < count) {
					this.removeOldest();
				}
			}
			aggTimerData.aggregateTimerData(timerData);
		} finally {
			persistAllLock.unlock();
		}
	}

	/**
	 * Clears the cache and persists all the data inside.
	 */
	public void removeAndPersistAll() {
		if (!queue.isEmpty()) {
			persistAllLock.lock();
			try {
				StatelessSession session = getHibernateTemplate().getSessionFactory().openStatelessSession();
				Transaction tx = session.beginTransaction();

				TimerData oldest = queue.poll();
				while (oldest != null) {
					map.remove(getCacheHash(oldest.getPlatformIdent(), oldest.getMethodIdent(), oldest.getTimeStamp().getTime()));
					session.insert(oldest);
					elementCount.decrementAndGet();

					oldest = queue.poll();
				}

				tx.commit();
				session.close();
			} finally {
				persistAllLock.unlock();
			}
		}
	}

	/**
	 * Removes the oldest element from the cache and puts it to the persistence list, so that
	 * element can be persisted next time persistence is done.
	 */
	private void removeOldest() {
		TimerData oldest = queue.poll();
		map.remove(getCacheHash(oldest.getPlatformIdent(), oldest.getMethodIdent(), oldest.getTimeStamp().getTime()));
		persistList.add(oldest);
		elementCount.decrementAndGet();
	}

	/**
	 * Persists all objects in the persistence list.
	 */
	private void saveAllInPersistList() {
		if (!persistList.isEmpty()) {
			StatelessSession session = getHibernateTemplate().getSessionFactory().openStatelessSession();
			Transaction tx = session.beginTransaction();

			TimerData last = persistList.poll();
			while (last != null) {
				last.finalizeData();
				session.insert(last);
				last = persistList.poll();
			}

			tx.commit();
			session.close();
		}
	}

	/**
	 * Returns the cache hash code.
	 * 
	 * @param platformIdent
	 *            Platform ident.
	 * @param methodIdent
	 *            Method ident.
	 * @param timestampValue
	 *            Time stamp value as long.
	 * @return Cache hash for the given set of values.
	 */
	private int getCacheHash(long platformIdent, long methodIdent, long timestampValue) {
		final int prime = 31;
		int result = 0;
		result = prime * result + (int) (platformIdent ^ (platformIdent >>> 32));
		result = prime * result + (int) (methodIdent ^ (methodIdent >>> 32));
		result = prime * result + (int) (timestampValue ^ (timestampValue >>> 32));
		return result;
	}

	/**
	 * Returns the value of the time stamp based on a aggregation period.
	 * 
	 * @param timerData
	 *            {@link TimerData} to get aggregation time stamp.
	 * @return Aggregation time stamp.
	 */
	private long getAlteredTimestamp(TimerData timerData) {
		long timestampValue = timerData.getTimeStamp().getTime();
		long newTimestampValue = timestampValue - timestampValue % aggregationPeriod;
		return newTimestampValue;
	}

	/**
	 * Cache cleaner, or thread that is constantly checking uf there is something to be persisted.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CacheCleaner extends Thread {

		/**
		 * Element that is last checked by thread. If this element is same as most recently added
		 * element, all elements in the cache will be persisted.
		 */
		private TimerData lastChecked;

		/**
		 * Constructor. Set thread as daemon and gives it minimum priority.
		 */
		public CacheCleaner() {
			setDaemon(true);
			setPriority(MIN_PRIORITY);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			while (true) {
				TimerData timerData = mostRecentlyAdded;
				if (timerData != null) {
					if (timerData.equals(lastChecked)) {
						removeAndPersistAll();
					}
					lastChecked = timerData;
				}
				saveAllInPersistList();
				try {
					Thread.sleep(cacheCleanSleepingPeriod);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
		}
	}

}
