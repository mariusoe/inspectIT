package info.novatec.inspectit.storage.nio.stream;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.read.ReadingChannelManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.io.ByteBufferInputStream;

/**
 * This is a specially designed {@link InputStream} to read the data from our storage. The stream
 * has a number of buffers that will data from the disk will be filled. Later, these buffers will be
 * used to stream the data when needed (fulfilling the {@link InputStream} functionality).
 * <p>
 * The stream uses {@link ByteBufferProvider} to get the buffers and will release the buffers on the
 * {@link #close()} method. It's a must to call a {@link #close()} after the stream has been used.
 * 
 * @author Ivan Senic
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ExtendedByteBufferInputStream extends ByteBufferInputStream {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * Minimum amount of buffers that can be used.
	 */
	private static final int MIN_BUFFERS = 2;

	/**
	 * Maximum amount of buffers that can be used.
	 */
	private static final int MAX_BUFFERS = 5;

	/**
	 * {@link ReadingChannelManager}.
	 */
	@Autowired
	private ReadingChannelManager readingChannelManager;

	/**
	 * {@link ByteBufferProvider}.
	 */
	@Autowired
	private ByteBufferProvider byteBufferProvider;

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private StorageManager storageManager;

	/**
	 * {@link ExecutorService} for reading tasks executions.
	 */
	@Autowired
	@Resource(name = "storageExecutorService")
	private ExecutorService executorService;

	/**
	 * {@link IStorageData} to read data for.
	 */
	private IStorageData storageData;

	/**
	 * List of descriptors that point to the data.
	 */
	private List<IStorageDescriptor> descriptors;

	/**
	 * Amount of buffers to use during read.
	 */
	private int numberOfBuffers;

	/**
	 * Total size that has to be read.
	 */
	private long totalSize;

	/**
	 * Current streaming position.
	 */
	private long position;

	/**
	 * Next index of the descriptor to be read.
	 */
	private AtomicInteger nextDescriptorIndex = new AtomicInteger(0);

	/**
	 * Queue of empty buffers. These buffers will be filled with the information from the disk.
	 */
	private LinkedBlockingQueue<ByteBuffer> emptyBuffers = new LinkedBlockingQueue<ByteBuffer>();

	/**
	 * Queue of full buffers. These buffers will be used to stream data.
	 */
	private LinkedBlockingQueue<ByteBuffer> fullBuffers = new LinkedBlockingQueue<ByteBuffer>();

	/**
	 * Set of opened paths.
	 */
	private Set<Path> openedChannelPaths = Collections.newSetFromMap(new ConcurrentHashMap<Path, Boolean>(16, 0.75f, 1));

	/**
	 * No-arg constructor.
	 */
	public ExtendedByteBufferInputStream() {
	}

	/**
	 * Default constructor. Sets number of buffers to 3. Same as calling {@link
	 * #ExtendedByteBufferInputStream(StorageData, List, 3)}.
	 * 
	 * @param storageData
	 *            {@link StorageData} to read information for.
	 * @param descriptors
	 *            List of descriptors that point to the data.
	 */
	public ExtendedByteBufferInputStream(IStorageData storageData, List<IStorageDescriptor> descriptors) {
		this(storageData, descriptors, 3);
	}

	/**
	 * Secondary constructor. Sets the amount of buffers to use.
	 * 
	 * @param numberOfBuffers
	 *            Amount of buffers to use during read.
	 * @param storageData
	 *            {@link StorageData} to read information for.
	 * @param descriptors
	 *            List of descriptors that point to the data.
	 */
	public ExtendedByteBufferInputStream(IStorageData storageData, List<IStorageDescriptor> descriptors, int numberOfBuffers) {
		this.numberOfBuffers = numberOfBuffers;
		this.storageData = storageData;
		this.descriptors = descriptors;
	}

	/**
	 * Prepares the stream for read. Must be called before any read operation is executed.
	 */
	public void prepare() {
		// get the buffers first
		int buffers = numberOfBuffers;
		if (buffers < MIN_BUFFERS) {
			buffers = MIN_BUFFERS;
		} else if (buffers > MAX_BUFFERS) {
			buffers = MAX_BUFFERS;
		}
		for (int i = 0; i < buffers; i++) {
			ByteBuffer byteBuffer = byteBufferProvider.acquireByteBuffer();
			emptyBuffers.add(byteBuffer);
		}

		// Calculate total read size
		totalSize = 0;
		for (IStorageDescriptor descriptor : descriptors) {
			totalSize += descriptor.getSize();
		}

		executorService.execute(new ReadTask());
	}

	/**
	 * Returns if the stream has more bytes remaining to stream.
	 * 
	 * @return True if stream can provide more bytes.
	 */
	public boolean hasRemaining() {
		return bytesLeft() > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() throws IOException {
		return (int) bytesLeft();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		if (hasRemaining() && null == super.getByteBuffer()) {
			bufferChange();
		}

		if (!super.getByteBuffer().hasRemaining()) {
			// check if we can read more
			if (bytesLeft() > 0) {
				bufferChange();
				int read = super.read();
				position += read;
				return read;
			} else {
				return -1;
			}
		} else {
			int read = super.read();
			position += read;
			return read;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (hasRemaining() && null == super.getByteBuffer()) {
			bufferChange();
		}

		int bufferRemaining = super.getByteBuffer().remaining();
		if (bufferRemaining >= len) {
			int read = super.read(b, off, len);
			position += read;
			return read;
		} else {
			int res = 0;
			if (bufferRemaining > 0) {
				super.getByteBuffer().get(b, off, bufferRemaining);
				res = bufferRemaining;
				position += bufferRemaining;
			}
			if (bytesLeft() > 0) {
				bufferChange();
				int read = this.read(b, off + bufferRemaining, len - bufferRemaining);
				res += read;
			}

			if (res > 0) {
				return res;
			} else {
				return -1;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Releases all byte buffers that are hold.
	 */
	@Override
	public void close() throws IOException {
		// release buffers from both queues
		while (!fullBuffers.isEmpty()) {
			ByteBuffer byteBuffer = fullBuffers.poll();
			if (null != byteBuffer) {
				byteBufferProvider.releaseByteBuffer(byteBuffer);
			}
		}
		while (!emptyBuffers.isEmpty()) {
			ByteBuffer byteBuffer = emptyBuffers.poll();
			if (null != byteBuffer) {
				byteBufferProvider.releaseByteBuffer(byteBuffer);
			}
		}

		// close opened channel paths
		for (Path path : openedChannelPaths) {
			readingChannelManager.finalizeChannel(path);
		}
	}

	/**
	 * Changes the current buffer used for streaming with a full one.
	 */
	private synchronized void bufferChange() {
		ByteBuffer current = super.getByteBuffer();
		if (null != current) {
			emptyBuffers.add(current);
		}

		try {
			super.setByteBuffer(fullBuffers.take());
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}

	/**
	 * Return number of bytes left for read.
	 * 
	 * @return Number of bytes left.
	 */
	private long bytesLeft() {
		return totalSize - position;
	}

	/**
	 * Sets {@link #storageData}.
	 * 
	 * @param storageData
	 *            New value for {@link #storageData}
	 */
	public void setStorageData(IStorageData storageData) {
		this.storageData = storageData;
	}

	/**
	 * Gets {@link #descriptors}.
	 * 
	 * @return {@link #descriptors}
	 */
	public List<IStorageDescriptor> getDescriptors() {
		return descriptors;
	}

	/**
	 * Sets {@link #descriptors}.
	 * 
	 * @param descriptors
	 *            New value for {@link #descriptors}
	 */
	public void setDescriptors(List<IStorageDescriptor> descriptors) {
		this.descriptors = descriptors;
	}

	/**
	 * Sets {@link #readingChannelManager}.
	 * 
	 * @param readingChannelManager
	 *            New value for {@link #readingChannelManager}
	 */
	public void setReadingChannelManager(ReadingChannelManager readingChannelManager) {
		this.readingChannelManager = readingChannelManager;
	}

	/**
	 * Sets {@link #byteBufferProvider}.
	 * 
	 * @param byteBufferProvider
	 *            New value for {@link #byteBufferProvider}
	 */
	public void setByteBufferProvider(ByteBufferProvider byteBufferProvider) {
		this.byteBufferProvider = byteBufferProvider;
	}

	/**
	 * Sets {@link #storageManager}.
	 * 
	 * @param storageManager
	 *            New value for {@link #storageManager}
	 */
	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	/**
	 * Sets {@link #executorService}.
	 * 
	 * @param executorService
	 *            New value for {@link #executorService}
	 */
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Read task that reads one by one descriptor and puts the full buffers to the full buffers
	 * queue.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ReadTask implements Runnable {

		/**
		 * Lock for stop reading.
		 */
		private Lock continueReadLock = new ReentrantLock();

		/**
		 * Condition for signaling countinue reading can occur.
		 */
		private Condition canContinueRead = continueReadLock.newCondition();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			// we run the task until all descriptors are processed
			while (nextDescriptorIndex.get() < descriptors.size()) {
				IStorageDescriptor storageDescriptor = descriptors.get(nextDescriptorIndex.get());
				Path channelPath = storageManager.getChannelPath(storageData, storageDescriptor);
				openedChannelPaths.add(channelPath);
				long readPosition = storageDescriptor.getPosition();
				long readSize = 0;
				while (readSize < storageDescriptor.getSize()) {
					// we read until whole descriptor size has been read
					ByteBuffer buffer = null;
					try {
						buffer = emptyBuffers.take();
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
					buffer.clear();
					final ByteBuffer finalByteBuffer = buffer;
					// in single shot we can read only till the buffer's capacity
					long singleReadSize = Math.min(storageDescriptor.getSize() - readSize, buffer.capacity());
					WriteReadCompletionRunnable completionRunnable = new WriteReadCompletionRunnable() {
						@Override
						public void run() {
							if (isCompleted()) {
								// add buffer to the queue
								fullBuffers.add(finalByteBuffer);
							} else {
								// if is failed, return buffer to empty buffers and decrease the
								// total read size
								finalByteBuffer.clear();
								emptyBuffers.add(finalByteBuffer);
								totalSize -= getAttemptedWriteReadSize();
							}
							// signal continue reading if await is active
							continueReadLock.lock();
							try {
								canContinueRead.signal();
							} finally {
								continueReadLock.unlock();
							}
						}
					};

					try {
						// execute read
						readingChannelManager.read(finalByteBuffer, readPosition, singleReadSize, channelPath, completionRunnable);
						// update the position and size for this descriptor
						readSize += singleReadSize;
						readPosition += singleReadSize;
						if (readSize < storageDescriptor.getSize()) {
							// if the descriptor has not been read completely we have to block until
							// the read is finished
							// this ensures that the data for one descriptor will be read in order
							continueReadLock.lock();
							try {
								canContinueRead.await();
							} catch (InterruptedException e) {
								Thread.interrupted();
							} finally {
								continueReadLock.unlock();
							}
						}
					} catch (IOException e) {
						log.warn("Exception occurred trying to read in the ReadTask.", e);
					}
				}

				nextDescriptorIndex.incrementAndGet();
			}
		}
	}

}
