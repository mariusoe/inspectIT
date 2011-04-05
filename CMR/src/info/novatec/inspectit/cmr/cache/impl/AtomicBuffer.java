package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.impl.IndexingException;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.communication.DefaultData;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Buffer uses atomic variables and references to handle the synchronization. Thus, non of its
 * methods is synchronized, nor synchronized block were used. However, the whole buffer is thread
 * safe.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Parameterized type of elements buffer can hold.
 */
public class AtomicBuffer<E extends DefaultData> implements IBuffer<E>, InitializingBean {

	/**
	 * Buffer properties.
	 */
	private BufferProperties bufferProperties;

	/**
	 * Max size of the buffer in atomic long.
	 */
	private AtomicLong maxSize;

	/**
	 * Eviction occupancy percentage. The value triggers the eviction when the occupancy of the
	 * buffer is greater than it. Although it is a float value, atomic integer is used via
	 * {@link Float#intBitsToFloat(int)} and {@link Float#floatToIntBits(float)} methods.
	 */
	private AtomicInteger evictionOccupancyPercentage;

	/**
	 * Current size of the buffer in atomic long.
	 */
	private AtomicLong currentSize = new AtomicLong();

	/**
	 * Number of elements added to the buffer.
	 */
	private AtomicLong elementsAdded = new AtomicLong();

	/**
	 * Number of elements evicted from the buffer.
	 */
	private AtomicLong elementsEvicted = new AtomicLong();

	/**
	 * Number of elements that where indexed into indexing tree.
	 */
	private AtomicLong elementsIndexed = new AtomicLong();

	/**
	 * Number of elements that where analyzed.
	 */
	private AtomicLong elementsAnalyzed = new AtomicLong();

	/**
	 * Atomic reference to the object that should be analyzed next.
	 */
	private AtomicReference<IBufferElement<E>> nextForAnalysis;

	/**
	 * Atomic reference to the first object.
	 */
	private AtomicReference<IBufferElement<E>> first;

	/**
	 * Atomic reference to the last object.
	 */
	private AtomicReference<IBufferElement<E>> last;

	/**
	 * Atomic reference to the object that should be indexed next.
	 */
	private AtomicReference<IBufferElement<E>> nextForIndexing;

	/**
	 * Eviction lock.
	 */
	private ReentrantLock evictLock = new ReentrantLock();

	/**
	 * Condition that states that there is nothing to evict currently.
	 */
	private Condition nothingToEvict = evictLock.newCondition();

	/**
	 * Analyze lock.
	 */
	private ReentrantLock analyzeLock = new ReentrantLock();

	/**
	 * Condition that states that there is nothing to analyze currently.
	 */
	private Condition nothingToAnalyze = analyzeLock.newCondition();

	/**
	 * Indexing lock.
	 */
	private ReentrantLock indexingLock = new ReentrantLock();

	/**
	 * Condition that states that there is nothing to index currently.
	 */
	private Condition nothingToIndex = indexingLock.newCondition();

	/**
	 * Indexing tree where the elements will be indexed.
	 */
	private ITreeComponent<E> indexingTree;

	/**
	 * Size of the indexing tree.
	 */
	private AtomicLong indexingTreeSize = new AtomicLong();

	/**
	 * Data added to the buffer in bytes.
	 */
	private AtomicLong dataAddedInBytes = new AtomicLong();

	/**
	 * Data removed from the buffer in bytes.
	 */
	private AtomicLong dataRemovedInBytes = new AtomicLong();

	/**
	 * Correct interface for calculating object sizes.
	 */
	private IObjectSizes objectSizes;

	/**
	 * Marker for empty buffer element.
	 */
	private EmptyBufferElement emptyBufferElement = new EmptyBufferElement();

	/**
	 * Logger for buffer.
	 */
	private static final Logger LOGGER = Logger.getLogger(AtomicBuffer.class);

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method also set the ID of the object that buffer element is holding, thus overwriting
	 * any earlier set ID.
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void put(IBufferElement<E> element) {
		// flag for notifying the sleeping thread that the new element is added
		boolean notifyThreads = false;
		
		// the element that is now first has to have a empty buffer element as next one
		element.setNextElement(emptyBufferElement);

		while (true) {
			// retrieving currently first element
			IBufferElement<E> currenltyFirst = first.get();

			// only thread that successfully execute compare and set will be able to perform changes
			if (first.compareAndSet(currenltyFirst, element)) {

				// increment number of added elements
				elementsAdded.incrementAndGet();

				// if currently first is not pointing to marker, it means that we already have
				// elements in the buffer, so connect elements
				if (!emptyBufferElement.equals(currenltyFirst)) {
					currenltyFirst.setNextElement(element);
				} else {
					// otherwise this is the first element in the buffer, so references are
					// connected
					// and notify flag is set
					nextForAnalysis.set(element);
					nextForIndexing.set(element);
					last.set(element);
					notifyThreads = true;
				}

				// break from while
				break;
			}
		}

		// if next for analyzing is pointing to the empty buffer elements, it means that there are
		// no more elements to analyze, thus after this insertion thread should be informed
		if (emptyBufferElement == nextForAnalysis.get() || notifyThreads) {
			analyzeLock.lock();
			try {
				nothingToAnalyze.signal();
			} finally {
				analyzeLock.unlock();
			}
		}

		// if next for indexing is pointing to the empty buffer elements, it means that there are
		// no more elements to analyze, thus after this insertion thread should be informed
		if (emptyBufferElement == nextForIndexing.get() || notifyThreads) {
			indexingLock.lock();
			try {
				nothingToIndex.signal();
			} finally {
				indexingLock.unlock();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The executing thread will wait until the current occupancy percentage of the buffer is
	 * smaller than eviction occupancy percentage. This method also sets the cleaning flag after
	 * every {@value #elementsCountForMaintenance}th element evicted.
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void evict() throws InterruptedException {
		// wait until there is need for eviction
		while (!shouldEvict()) {
			evictLock.lock();
			try {
				// check again for avoiding deadlocks
				if (!shouldEvict()) {
					nothingToEvict.await();
				}
			} finally {
				evictLock.unlock();
			}
		}

		while (true) {
			// get the currently last element
			IBufferElement<E> currentLastElement = last.get();

			// check if we really have concrete elements because clear buffer can happen anywhere
			if (emptyBufferElement.equals(currentLastElement)) {
				break;
			}

			// set up the values for evicting the fragment of elements
			IBufferElement<E> newLastElement = currentLastElement;
			long evictionFragmentMaxSize = (long) (this.getMaxSize() * bufferProperties.getEvictionFragmentSizePercentage());
			long fragmentSize = 0;
			int elementsInFragment = 0;

			// iterate until size of the eviction fragment is reached
			while (fragmentSize < evictionFragmentMaxSize) {
				fragmentSize += newLastElement.getBufferElementSize();
				elementsInFragment++;
				newLastElement = newLastElement.getNextElement();
			}

			// change the last element to the right one
			// only thread that execute compare and set successfully can perform changes
			if (last.compareAndSet(currentLastElement, newLastElement)) {
				// subtract the fragment size
				substractFromCurrentSize(fragmentSize);

				// add evicted elements to the total count
				elementsEvicted.addAndGet(elementsInFragment);

				// if the last is now pointing to the empty buffer element, it means that we have
				// evicted all elements, so first should also point to empty buffer element
				// this can only happen in theory
				if (emptyBufferElement == last.get()) {
					first.set(emptyBufferElement);
				}

				// break from while
				break;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void analyzeNext() throws InterruptedException {
		// wait until there are elements to analyze
		// pointing to the marker means that there is nothing to work on
		while (emptyBufferElement == nextForAnalysis.get()) {
			analyzeLock.lock();
			try {
				// check again for avoiding deadlocks
				if (emptyBufferElement == nextForAnalysis.get()) {
					nothingToAnalyze.await();
				}
			} finally {
				analyzeLock.unlock();
			}
		}

		while (true) {
			// get next for analysis
			IBufferElement<E> elementToAnalyze = nextForAnalysis.get();
			// check if we really have concrete element because clear buffer can happen anywhere
			if (emptyBufferElement.equals(elementToAnalyze)) {
				break;
			}

			// only thread that execute compare and set successfully can perform changes
			if (nextForAnalysis.compareAndSet(elementToAnalyze, elementToAnalyze.getNextElement())) {

				// perform analysis
				elementToAnalyze.calculateAndSetBufferElementSize(objectSizes);
				elementToAnalyze.setAnalyzed(true);
				addToCurrentSize(elementToAnalyze.getBufferElementSize(), true);
				elementsAnalyzed.incrementAndGet();

				// if now we are pointing to the marker we go to sleep until we are informed that
				// something was added in the buffer
				while (emptyBufferElement == nextForAnalysis.get()) {
					analyzeLock.lock();
					try {
						if (emptyBufferElement == nextForAnalysis.get()) {
							nothingToAnalyze.await();
							// when we are awake, next for analysis is now set to point to the next
							// logically connected element
							nextForAnalysis.compareAndSet(emptyBufferElement, elementToAnalyze.getNextElement());
						}
					} finally {
						analyzeLock.unlock();
					}
				}

				// break from while
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method also performs the cleaning of the indexing tree if the cleaning flag is on.
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void indexNext() throws InterruptedException {
		// wait until there are elements to index
		// pointing to the marker means that there is nothing to work on
		while (emptyBufferElement == nextForIndexing.get()) {
			indexingLock.lock();
			try {
				// check again for avoiding deadlocks
				if (emptyBufferElement == nextForIndexing.get()) {
					nothingToIndex.await();
				}
			} finally {
				indexingLock.unlock();
			}
		}

		while (true) {
			// get next for analysis
			IBufferElement<E> elementToIndex = nextForIndexing.get();
			// check if we really have concrete element because clear buffer can happen anywhere
			if (emptyBufferElement.equals(elementToIndex)) {
				break;
			}

			// only thread that execute compare and set successfully can perform changes
			if (nextForIndexing.compareAndSet(elementToIndex, elementToIndex.getNextElement())) {
				try {
					// index element
					indexingTree.put(elementToIndex.getObject());

					// increase number of indexed elements, and perform calculation of the indexing
					// tree size if enough elements have been indexed
					elementsIndexed.incrementAndGet();

					if (dataAddedInBytes.get() > bufferProperties.getFlagsSetOnBytes()) {
						dataAddedInBytes.set(0);
						long time = 0;
						if (LOGGER.isDebugEnabled()) {
							time = System.nanoTime();
						}

						long newSize = indexingTree.getComponentSize(objectSizes);
						newSize += newSize * objectSizes.getObjectSecurityExpansionRate();
						long oldSize = 0;
						while (true) {
							oldSize = indexingTreeSize.get();
							if (indexingTreeSize.compareAndSet(oldSize, newSize)) {
								addToCurrentSize(newSize - oldSize, false);
								break;
							}
						}

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Indexing tree size update duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
							LOGGER.debug("Indexing tree delta: " + (newSize - oldSize));
						}
					}
				} catch (IndexingException e) {
					// indexing exception should not happen
					LOGGER.error(e.getMessage(), e);
				}

				// if now we are pointing to the marker we go to sleep until we are informed that
				// something was added in the buffer
				while (emptyBufferElement == nextForIndexing.get()) {
					indexingLock.lock();
					try {
						if (emptyBufferElement == nextForIndexing.get()) {
							nothingToIndex.await();
							// when we are awake, next for indexing is now set to point to the next
							// logically connected element
							nextForIndexing.compareAndSet(emptyBufferElement, elementToIndex.getNextElement());
						}
					} finally {
						indexingLock.unlock();
					}
				}

				// break from while
				break;
			}
		}

		// if clean flag is set thread should try to perform indexing tree cleaning
		// only thread that successfully executes the compare and set will do the cleaning
		if (dataRemovedInBytes.get() > bufferProperties.getFlagsSetOnBytes()) {
			dataRemovedInBytes.set(0);
			long time = 0;
			if (LOGGER.isDebugEnabled()) {
				time = System.nanoTime();
			}

			indexingTree.clean();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Indexing tree cleaning duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public long getMaxSize() {
		return maxSize.get();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 * <p>
	 * Using this method does not provide any check for the supplied new maximum size. Thus, it is
	 * responsibility of the user to assure that the given value is correct.
	 */
	public void setMaxSize(long maxSize) {
		this.maxSize.set(maxSize);
		notifyEvictionIfNeeded();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getCurrentSize() {
		return currentSize.get();
	}

	/**
	 * Sets the current size of the buffer.
	 * 
	 * @param currentSize
	 *            Size in bytes.
	 */
	public void setCurrentSize(long currentSize) {
		this.currentSize.set(currentSize);
		notifyEvictionIfNeeded();
	}

	/**
	 * Adds size value to the current size.
	 * <p>
	 * This method is thread safe.
	 * 
	 * @param size
	 *            Size in bytes.
	 * @param areObjects
	 *            Defines if the size that is added to the current size relates to the objects in
	 *            buffer. True means size is related to objects in buffer, false means that the size
	 *            relates to the indexing tree.
	 */
	private void addToCurrentSize(long size, boolean areObjects) {
		currentSize.addAndGet(size);
		notifyEvictionIfNeeded();
		if (areObjects) {
			dataAddedInBytes.addAndGet(size);
		}
	}

	/**
	 * Subtracts size value from the current size.
	 * <p>
	 * This method is thread safe.
	 * 
	 * @param size
	 *            Size in bytes.
	 */
	private void substractFromCurrentSize(long size) {
		currentSize.addAndGet(-(size));
		dataRemovedInBytes.addAndGet(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getEvictionOccupancyPercentage() {
		return Float.intBitsToFloat(evictionOccupancyPercentage.get());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 */
	public void setEvictionOccupancyPercentage(float evictionOccupancyPercentage) {
		this.evictionOccupancyPercentage.set(Float.floatToIntBits(evictionOccupancyPercentage));
		notifyEvictionIfNeeded();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 */
	public float getOccupancyPercentage() {
		return ((float) currentSize.get()) / maxSize.get();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 */
	public boolean shouldEvict() {
		return getOccupancyPercentage() > Float.intBitsToFloat(evictionOccupancyPercentage.get());
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAll() {
		first.set(emptyBufferElement);
		last.set(emptyBufferElement);
		nextForAnalysis.set(emptyBufferElement);
		nextForIndexing.set(emptyBufferElement);
		setCurrentSize(0);
		elementsAdded.set(0);
		elementsAnalyzed.set(0);
		elementsIndexed.set(0);
		elementsEvicted.set(0);
		indexingTree.clearAll();
		dataAddedInBytes.set(0);
		dataRemovedInBytes.set(0);
	}

	/**
	 * Returns the number of inserted elements since the buffer has been created.
	 * 
	 * @return Number of inserted elements.
	 */
	public long getInsertedElemenets() {
		return elementsAdded.get();
	}

	/**
	 * Returns the number of evicted elements since the buffer has been created.
	 * 
	 * @return Number of evicted elements.
	 */
	public long getEvictedElemenets() {
		return elementsEvicted.get();
	}

	/**
	 * Returns the number of indexed elements since the buffer has been created.
	 * 
	 * @return Number of indexed elements.
	 */
	public long getIndexedElements() {
		return elementsIndexed.get();
	}

	/**
	 * Setter for object sizes.
	 * 
	 * @param objectSizes
	 *            Proper {@link IObjectSizes} instance.
	 */
	public void setObjectSizes(IObjectSizes objectSizes) {
		this.objectSizes = objectSizes;
	}

	/**
	 * Setter for buffer properties.
	 * 
	 * @param bufferProperties
	 *            Set of properties for this buffer.
	 */
	public void setBufferProperties(BufferProperties bufferProperties) {
		this.bufferProperties = bufferProperties;
	}

	/**
	 * Setter for the root branch for the indexing tree.
	 * 
	 * @param indexingTree
	 *            Root branch.
	 */
	public void setIndexingTree(ITreeComponent<E> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.maxSize = new AtomicLong(bufferProperties.getInitialBufferSize());
		this.evictionOccupancyPercentage = new AtomicInteger(Float.floatToIntBits(bufferProperties.getEvictionOccupancyPercentage()));
		this.objectSizes.setObjectSecurityExpansionRate(bufferProperties.getObjectSecurityExpansionRate(maxSize.get()));
		this.first = new AtomicReference<IBufferElement<E>>(emptyBufferElement);
		this.last = new AtomicReference<IBufferElement<E>>(emptyBufferElement);
		this.nextForAnalysis = new AtomicReference<IBufferElement<E>>(emptyBufferElement);
		this.nextForIndexing = new AtomicReference<IBufferElement<E>>(emptyBufferElement);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Using buffer with maximum size " + NumberFormat.getInstance().format(maxSize) + " bytes...");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer(256);
		msg.append("The buffer occupancy status: ");
		msg.append(NumberFormat.getInstance().format(currentSize.get()));
		msg.append(" bytes occupied from total ");
		msg.append(NumberFormat.getInstance().format(maxSize.get()));
		msg.append(" bytes available (");
		msg.append(NumberFormat.getInstance().format(getOccupancyPercentage() * 100));
		msg.append("%).\nElements processed in the buffer since last clear buffer:\n-Elements added: ");
		msg.append(NumberFormat.getInstance().format(elementsAdded.get()));

		msg.append("\n-Elements analyzed: ");
		msg.append(NumberFormat.getInstance().format(elementsAnalyzed.get()));

		msg.append("\n-Elements indexed: ");
		msg.append(NumberFormat.getInstance().format(elementsIndexed.get()));

		msg.append("\n-Elements evicted: ");
		msg.append(NumberFormat.getInstance().format(elementsEvicted.get()));
		msg.append('\n');
		return msg.toString();
	}

	/**
	 * Checks if the eviction should start, and if it does notifies the right thread.
	 */
	private void notifyEvictionIfNeeded() {
		if (shouldEvict()) {
			evictLock.lock();
			try {
				nothingToEvict.signal();
			} finally {
				evictLock.unlock();
			}
		}
	}

	/**
	 * Class that serves as a marker for empty buffer element.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class EmptyBufferElement implements IBufferElement<E> {

		@Override
		public E getObject() {
			return null;
		}

		@Override
		public long getBufferElementSize() {
			return 0;
		}

		@Override
		public void setBufferElementSize(long size) {
		}

		@Override
		public void calculateAndSetBufferElementSize(IObjectSizes objectSizes) {
		}

		@Override
		public IBufferElement<E> getNextElement() {
			return null;
		}

		@Override
		public void setNextElement(IBufferElement<E> element) {
		}

		@Override
		public boolean isAnalyzed() {
			return false;
		}

		@Override
		public void setAnalyzed(boolean analyzed) {
		}

		@Override
		public boolean isEvicted() {
			return false;
		}

		@Override
		public void setEvicted(boolean evicted) {
		}

	}

}
