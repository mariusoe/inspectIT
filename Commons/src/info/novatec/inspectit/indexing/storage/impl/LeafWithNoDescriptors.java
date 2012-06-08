package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This leaf for the storage is not keeping the {@link SimpleStorageDescriptor} for each element.
 * This leaf just keeps track of the total size of elements saved. It is expected that this leafs
 * are used when the elements does not have to be retrieved singularly.
 * <P>
 * The clear advantage with this leaf is size of the leaf in memory/disk. However, the price for
 * this is not being able to find one concrete element in the leaf. When this is necessary, the
 * other leaf type must be used.
 * <P>
 * When querying this leaf, only one {@link IStorageDescriptor} is returned that contains the
 * position 0 and size of all elements together. However, performing read with this descriptor
 * should be safe and the result should be all elements in the leaf.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of data that can be indexed.
 */
public class LeafWithNoDescriptors<E extends DefaultData> implements IStorageTreeComponent<E> {

	/**
	 * Max size of one range is 8MB. If more that is going to be written in the same leaf then we
	 * need to split data in several ranges. The problem can occur if we write too much in one leaf
	 * (like > 100MB) loading of this data on the UI has to be in one request, making this request a
	 * high impact on the memory that can make OOME in the UI.
	 */
	private static final long MAX_RANGE_SIZE = 8388608;

	/**
	 * Leaf id.
	 */
	private int id;

	/**
	 * Bounded descriptor.
	 */
	private transient BoundedDecriptor boundedDecriptor = new BoundedDecriptor();

	/**
	 * Size counting.
	 */
	private AtomicLong size;

	/**
	 * Current splitter value.
	 */
	private transient AtomicLong currentSplitter;

	/**
	 * Valid ranges hold by this leaf.
	 */
	private List<Long> rangeSplitters;

	/**
	 * Default constructor.
	 */
	public LeafWithNoDescriptors() {
		id = UUID.randomUUID().hashCode();
		size = new AtomicLong(0);
		currentSplitter = new AtomicLong(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor put(E element) throws IndexingException {
		return boundedDecriptor;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calling this method with this implementation of the storage leaf is highly discouraged,
	 * because we simply can not find one element, and have to return descriptor for all elements.
	 */
	public IStorageDescriptor get(E element) {
		return this.getDescriptorForAllElements();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IStorageDescriptor> query(IIndexQuery query) {
		List<IStorageDescriptor> returnList = new ArrayList<IStorageDescriptor>();

		long rangeStart = 0;
		if (null != rangeSplitters) {
			for (Long rangeEnd : rangeSplitters) {
				SimpleStorageDescriptor simpleStorageDescriptor = new SimpleStorageDescriptor();
				simpleStorageDescriptor.setPosition((int) rangeStart);
				simpleStorageDescriptor.setSize((int) (rangeEnd.longValue() - rangeStart));
				returnList.add(new StorageDescriptor(id, simpleStorageDescriptor));
				rangeStart = rangeEnd.longValue();
			}
		}

		SimpleStorageDescriptor simpleStorageDescriptor = new SimpleStorageDescriptor();
		simpleStorageDescriptor.setPosition((int) rangeStart);
		simpleStorageDescriptor.setSize((int) (size.get() - rangeStart));
		returnList.add(new StorageDescriptor(id, simpleStorageDescriptor));

		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor getAndRemove(E template) {
		// FIXME Not good, when write fails, we need to remove somehow the peace that is missing..
		return null;
	};

	/**
	 * {@inheritDoc}
	 */
	public long getComponentSize(IObjectSizes objectSizes) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(4, 0, 1, 0, 0, 0);
		size += objectSizes.getSizeOfLongObject();
		size += objectSizes.getSizeOfLongObject();
		size += objectSizes.getSizeOfObjectObject();
		if (null != rangeSplitters) {
			size += objectSizes.getSizeOf(rangeSplitters);
		}
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * @return Returns one descriptor that describes all elements.
	 */
	private IStorageDescriptor getDescriptorForAllElements() {
		SimpleStorageDescriptor simpleStorageDescriptor = new SimpleStorageDescriptor();
		simpleStorageDescriptor.setPosition(0);
		simpleStorageDescriptor.setSize(size.intValue());
		return new StorageDescriptor(id, simpleStorageDescriptor);
	}

	/**
	 * Initial processing of the size add to the leaf.
	 * 
	 * @param sizeToAdd
	 *            Size to add to leaf.
	 */
	private void addSize(long sizeToAdd) {
		size.addAndGet(sizeToAdd);
		while (true) {
			long totalCurrentSize = size.get();
			long currentSplitValue = currentSplitter.get();
			if (totalCurrentSize - currentSplitValue > MAX_RANGE_SIZE) {
				if (currentSplitter.compareAndSet(currentSplitValue, totalCurrentSize)) {
					if (null == rangeSplitters) {
						rangeSplitters = new CopyOnWriteArrayList<Long>();
					}
					rangeSplitters.add(totalCurrentSize);
					break;
				}
			} else {
				break;
			}
		}
	}

	/**
	 * This is the private implementation of {@link IStorageDescriptor} that reflects operations
	 * directly to the leaf. The usage of descriptor outside of this class should not be changed,
	 * but calling some methods won't create any actions.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class BoundedDecriptor implements IStorageDescriptor {

		/**
		 * {@inheritDoc}
		 */
		public int getChannelId() {
			return id;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setChannelId(int channelId) {
		}

		/**
		 * {@inheritDoc}
		 */
		public long getPosition() {
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setPosition(long position) {
		}

		/**
		 * {@inheritDoc}
		 */
		public long getSize() {
			return 0;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Delegates the call to the internal leaf processing of size add.
		 * 
		 * @see LeafWithNoDescriptors#addSize(long);
		 */
		public void setSize(long size) {
			LeafWithNoDescriptors.this.addSize(size);
		}

		/**
		 * {@inheritDoc}
		 */
		public int compareTo(IStorageDescriptor other) {
			if (this.getChannelId() - other.getChannelId() != 0) {
				return this.getChannelId() - other.getChannelId();
			}
			if (this.getPosition() - other.getPosition() != 0) {
				return (int) (this.getPosition() - other.getPosition());
			}
			if (this.getSize() - other.getSize() != 0) {
				return (int) (this.getSize() - other.getSize());
			}
			return 0;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("totalSize", size.get());
		return toStringBuilder.toString();
	}
}
