package info.novatec.inspectit.storage.processor.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class aggregates data and writes only aggregated objects to the writer.
 * 
 * @param <E>
 *            Type of data to aggregate.
 * 
 * @author Ivan Senic
 * 
 */
public class DataAggregatorProcessor<E extends TimerData> extends AbstractDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -883029940157040641L;

	/**
	 * Max elements by default.
	 */
	private static final int DEFAULT_MAX_ELEMENTS = 200;

	/**
	 * Period of time in which all timer data should be aggregated. In milliseconds.
	 */
	private long aggregationPeriod;

	/**
	 * Max elements in the cache.
	 */
	private int maxElements;

	/**
	 * Current element count in cache.
	 */
	private AtomicInteger elementCount = new AtomicInteger(0);

	/**
	 * Map for caching.
	 */
	private ConcurrentHashMap<Integer, E> map = new ConcurrentHashMap<Integer, E>(64, 0.75f, 4);

	/**
	 * Queue for knowing the order.
	 */
	private ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<E>();

	/**
	 * List of classes that should be aggregated by this processor. Only instances of
	 * {@link TimerData} can be aggregated.
	 */
	private Class<E> clazz;

	/**
	 * Timer data aggregator.
	 */
	private IAggregator<E> dataAggregator;

	/**
	 * No-arg constructor.
	 */
	public DataAggregatorProcessor() {
	}

	/**
	 * Default constructor. Sets max elements to {@value #DEFAULT_MAX_ELEMENTS}.
	 * 
	 * @param clazz
	 *            List of classes to be saved to storage by this {@link AbstractDataProcessor}.
	 * @param aggregationPeriod
	 *            Period of time in which data should be aggregated. In milliseconds.
	 * @param dataAggregator
	 *            {@link IAggregator} used to aggregate data. Must not be null.
	 */
	public DataAggregatorProcessor(Class<E> clazz, long aggregationPeriod, IAggregator<E> dataAggregator) {
		this(clazz, aggregationPeriod, DEFAULT_MAX_ELEMENTS, dataAggregator);
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param clazz
	 *            List of classes to be saved to storage by this {@link AbstractDataProcessor}.
	 * @param aggregationPeriod
	 *            Period of time in which data should be aggregated. In milliseconds.
	 * @param maxElements
	 *            Max elements in the cache of the processor.
	 * @param dataAggregator
	 *            {@link IAggregator} used to aggregate data. Must not be null.
	 */
	public DataAggregatorProcessor(Class<E> clazz, long aggregationPeriod, int maxElements, IAggregator<E> dataAggregator) {
		if (null == clazz) {
			throw new IllegalArgumentException("Aggregation class can not be null.");
		}
		if (aggregationPeriod <= 0) {
			throw new IllegalArgumentException("Aggregation period must be a positive number greater than zero.");
		}
		if (maxElements <= 0) {
			throw new IllegalArgumentException("Max elements must be a positive number greater than zero.");
		}
		if (null == dataAggregator) {
			throw new IllegalArgumentException("Aggregator can not be null.");
		}
		this.clazz = clazz;
		this.aggregationPeriod = aggregationPeriod;
		this.maxElements = maxElements;
		this.dataAggregator = dataAggregator;
		this.clazz = clazz;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected void processData(DefaultData defaultData) {
		E timerData = (E) defaultData;
		long alteredTimestamp = getAlteredTimestamp(timerData);
		int cacheHash = getCacheHash(timerData, alteredTimestamp);

		E aggData = map.get(cacheHash);
		if (null == aggData) {
			aggData = clone(timerData, alteredTimestamp);
			map.put(cacheHash, aggData);
			queue.add(aggData);
			int count = elementCount.incrementAndGet();
			if (maxElements < count) {
				this.writeOldest();
			}
		}
		aggData.aggregateTimerData(timerData);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBeProcessed(DefaultData defaultData) {
		if (null != defaultData) {
			return clazz.equals(defaultData.getClass());
		}
		return false;
	}

	/**
	 * Writes the oldest data to the storage.
	 */
	private void writeOldest() {
		E oldest = queue.poll();
		map.remove(getCacheHash(oldest, oldest.getTimeStamp().getTime()));
		oldest.finalizeData();
		elementCount.decrementAndGet();
		getStorageWriter().write(oldest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() {
		E oldest = queue.poll();
		while (null != oldest) {
			map.remove(getCacheHash(oldest, oldest.getTimeStamp().getTime()));
			oldest.finalizeData();
			elementCount.decrementAndGet();
			getStorageWriter().write(oldest);

			oldest = queue.poll();
		}
	}

	/**
	 * Returns the cache hash code.
	 * 
	 * @param timerData
	 *            Object to calculate cache hash for.
	 * @param timestampValue
	 *            Time stamp value as long.
	 * @return Cache hash for the given set of values.
	 */
	private int getCacheHash(E timerData, long timestampValue) {
		final int prime = 31;
		Object key = dataAggregator.getAggregationKey(timerData);
		int result = key.hashCode();
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
	 * Creates new {@link TimerData} object for aggregation purposes.
	 * 
	 * @param timerData
	 *            {@link TimerData} to clone.
	 * @param alteredTimestamp
	 *            New altered time stamp clone to have.
	 * @return Cloned object ready for aggregation.
	 */
	private E clone(E timerData, long alteredTimestamp) {
		E clone = dataAggregator.getClone(timerData);
		clone.setId(timerData.getId());
		clone.setTimeStamp(new Timestamp(alteredTimestamp));
		return clone;
	}

}
