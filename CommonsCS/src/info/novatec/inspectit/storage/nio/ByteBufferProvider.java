package info.novatec.inspectit.storage.nio;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This component enables reusing of the {@link ByteBuffer}s. It creates a set of initial buffers
 * that are available for "rent". This class does not have protection against not returning a
 * buffer, thus all components using the provider have to insure they will return the buffer to the
 * provider.
 * <P>
 * If the component ask for a buffer of a size that is not available in the current pool (all
 * existing buffers are smaller), the provider will make the buffer of wanted size if the creation
 * of such buffer will not increase the {@link #poolTotalCapacity} over {@link #poolMaxCapacity}. If
 * the buffer of wanted size exists in the pool, but it is currently "rented", the component asking
 * for the buffer will have to wait until the buffer is returned to the pool.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ByteBufferProvider {

	/**
	 * Default buffer size that will be used if not initial buffer list size is provided to create
	 * one buffer.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

	/**
	 * List of the initial buffer sizes, which will be created on the start up.
	 */
	@Resource(name = "byteBufferSizes")
	private List<Integer> initialBufferSizeList;

	/**
	 * Amount of bytes this pool can acquire in total.
	 * <p>
	 * <i>Hard coded to 50MB</i>
	 */
	@Value(value = "52428800")
	private int poolMaxCapacity;

	/**
	 * Total amount of capacity of all buffers.
	 */
	private AtomicInteger poolTotalCapacity = new AtomicInteger(0);

	/**
	 * Biggest buffer capacity.
	 */
	private AtomicInteger biggestBufferCapacity = new AtomicInteger(0);

	/**
	 * Queue of buffers.
	 */
	private LinkedBlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<ByteBuffer>();

	/**
	 * Returns arbitrary buffer from the pool. This method waits for buffer to be available.
	 * 
	 * @return {@link ByteBuffer}.
	 */
	public ByteBuffer acquireByteBuffer() {
		ByteBuffer buffer = null;
		while (null == buffer) {
			try {
				buffer = queue.take();
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
		return buffer;
	}

	/**
	 * Returns the buffer with capacity at least of specified minimum capacity. This method waits
	 * until a buffer of requested size is available. If buffer of such size does not exists in the
	 * pool, it will be created.
	 * 
	 * @param minCapacity
	 *            Minimum wanted capacity.
	 * @return {@link ByteBuffer} with minimum capacity of provided size.
	 * @throws ByteBufferPoolOverflowException
	 *             If the new buffer of wanted capacity needs to be created and the capacity would
	 *             exceed the pool's max capacity.
	 */
	public ByteBuffer acquireByteBuffer(int minCapacity) throws ByteBufferPoolOverflowException {
		if (minCapacity < biggestBufferCapacity.get()) {
			while (true) {
				ByteBuffer buffer = null;
				try {
					buffer = queue.take();
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				if (buffer.capacity() >= minCapacity) {
					return buffer;
				} else {
					try {
						queue.put(buffer);
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
				}
			}
		} else {
			ByteBuffer buffer = createNewBuffer(minCapacity);
			if (null != buffer) {
				return buffer;
			} else {
				throw new ByteBufferPoolOverflowException("Creating a new buffer with capacity of " + minCapacity + " bytes would exceed the total byte buffer pool capacity of " + poolMaxCapacity
						+ " bytes. Current pool capacity is " + poolTotalCapacity.get() + " bytes.");
			}
		}
	}

	/**
	 * Gives back the {@link ByteBuffer} to the pool, so that others can use it.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} to put back to the pool.
	 * @throws InterruptedException
	 */
	public void releaseByteBuffer(ByteBuffer byteBuffer) {
		byteBuffer.clear();
		try {
			queue.put(byteBuffer);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}

	/**
	 * Creates set of initial buffers.
	 * 
	 * @throws Exception
	 *             If by the end of the method pool of buffers is empty.
	 */
	@PostConstruct
	public void createInitialSetOfBuffers() throws Exception {
		if (null != initialBufferSizeList && !initialBufferSizeList.isEmpty()) {
			for (Integer capacity : initialBufferSizeList) {
				ByteBuffer byteBuffer = createNewBuffer(capacity.intValue());
				if (null != byteBuffer) {
					try {
						queue.put(byteBuffer);
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
				}
			}
		} else {
			try {
				ByteBuffer byteBuffer = createNewBuffer(DEFAULT_BUFFER_SIZE);
				if (null != byteBuffer) {
					queue.put(byteBuffer);
				}
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
		if (queue.isEmpty()) {
			throw new Exception("Buffers pool could not be initilized with given Byte buffer provider properties.");
		}
	}

	/**
	 * Creates new buffer with wanted capacity. If the creation of the buffer with wanted capacity
	 * extends the max pool capacity, buffer will not be created and null would be returned.
	 * 
	 * @param capacity
	 *            Buffer's capacity.
	 * @return {@link ByteBuffer} or null if the creation of the buffer would excess the pool max
	 *         capacity..
	 */
	private ByteBuffer createNewBuffer(int capacity) {
		while (true) {
			int currentCapacity = poolTotalCapacity.get();
			if (currentCapacity + capacity < poolMaxCapacity) {
				if (poolTotalCapacity.compareAndSet(currentCapacity, currentCapacity + capacity)) {
					ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

					// check if we created biggest buffer
					while (true) {
						int biggest = biggestBufferCapacity.get();
						if (biggest < capacity) {
							if (biggestBufferCapacity.compareAndSet(biggest, capacity)) {
								break;
							}
						} else {
							break;
						}
					}

					return buffer;
				}
			} else {
				break;
			}
		}
		return null;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 * 
	 * @param initialBufferSizeList
	 *            the initialBufferSizeList to set
	 */
	public void setInitialBufferSizeList(List<Integer> initialBufferSizeList) {
		this.initialBufferSizeList = initialBufferSizeList;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 * 
	 * @param poolMaxCapacity
	 *            the poolMaxCapacity to set
	 */
	public void setPoolMaxCapacity(int poolMaxCapacity) {
		this.poolMaxCapacity = poolMaxCapacity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("poolMaxCapacity", poolMaxCapacity);
		toStringBuilder.append("poolTotalCapacity", poolTotalCapacity.get());
		toStringBuilder.append("biggestBufferCapacity", biggestBufferCapacity.get());
		toStringBuilder.append("availableBufferQueue", queue);
		return toStringBuilder.toString();
	}

}
