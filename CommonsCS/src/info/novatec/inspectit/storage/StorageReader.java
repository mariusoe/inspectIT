package info.novatec.inspectit.storage;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageDescriptor;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.nio.ByteBufferPoolOverflowException;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.read.ReadingChannelManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reader that uses {@link ReadingChannelManager} to read content from the disk.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageReader {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private StorageManager storageManager;

	/**
	 * {@link ReadingChannelManager}.
	 */
	@Autowired
	private ReadingChannelManager readingChannelManager;

	/**
	 * {@link ExecutorService} for reading tasks executions.
	 */
	@Autowired
	@Resource(name = "storageExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * {@link ByteBufferProvider}.
	 */
	@Autowired
	private ByteBufferProvider byteBufferProvider;

	/**
	 * Reads set of data parts described by given {@link StorageDescriptor}s from a single storage.
	 * 
	 * @param storageData
	 *            Storage to read from.
	 * @param descriptors
	 *            Collection of descriptors that describe the data that needs to be read.
	 * @return Combined array of bytes. The order of bytes in the array does not have to be same as
	 *         the order of descriptors.
	 */
	public byte[] read(IStorageData storageData, Collection<IStorageDescriptor> descriptors) {
		final AtomicInteger count = new AtomicInteger(0);
		final AtomicLong totalReadSize = new AtomicLong(0);
		final LinkedBlockingQueue<ByteBuffer> localBufferQueue = new LinkedBlockingQueue<ByteBuffer>();
		Set<Path> openedChannelsSet = new HashSet<Path>();

		for (final IStorageDescriptor descriptor : descriptors) {
			final Path channelPath = storageManager.getChannelPath(storageData, descriptor);
			openedChannelsSet.add(channelPath);
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					final ByteBuffer finalByteBuffer;
					try {
						finalByteBuffer = byteBufferProvider.acquireByteBuffer((int) descriptor.getSize());
					} catch (ByteBufferPoolOverflowException e) {
						log.error("Can not perform read because the byte buffer of capacity " + descriptor.getSize() + " is not available.", e);
						return;
					}
					WriteReadCompletionRunnable completionRunnable = new WriteReadCompletionRunnable() {

						@Override
						public void run() {
							// if completed put buffer ready to read from it, if not clear and
							// return for somebody else
							if (this.isCompleted()) {
								localBufferQueue.add(finalByteBuffer);
							} else {
								byteBufferProvider.releaseByteBuffer(finalByteBuffer);
								count.decrementAndGet();
								totalReadSize.addAndGet(-descriptor.getSize());
							}

						}
					};
					try {
						readingChannelManager.read(finalByteBuffer, descriptor.getPosition(), descriptor.getSize(), channelPath, completionRunnable);
					} catch (Exception e) {
						// for exception also return the buffer and refine the vars
						byteBufferProvider.releaseByteBuffer(finalByteBuffer);
						count.decrementAndGet();
						totalReadSize.addAndGet(-descriptor.getSize());
						log.warn("Error reading from descirptor:" + descriptor, e);
					}
				}
			});
			count.incrementAndGet();
			totalReadSize.addAndGet(descriptor.getSize());
		}

		int index = 0;
		byte[] result = new byte[totalReadSize.intValue()];

		while (count.intValue() != 0) {
			try {
				ByteBuffer buffer = localBufferQueue.poll(3, TimeUnit.SECONDS);
				if (buffer != null) {
					int size = buffer.remaining();
					buffer.get(result, index, size);
					byteBufferProvider.releaseByteBuffer(buffer);
					index += size;
					count.decrementAndGet();
				}
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}

		for (Path openChannel : openedChannelsSet) {
			try {
				readingChannelManager.finalize(openChannel);
			} catch (IOException e) {
				Thread.interrupted();
			}
		}

		if (result.length != totalReadSize.intValue()) {
			// It can happen that somewhere read fails, and total read size gets decreased
			// Then we need to send back just a sub array
			return Arrays.copyOfRange(result, 0, totalReadSize.intValue());
		} else {
			return result;
		}
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
	 * Sets {@link #readingChannelManager}.
	 * 
	 * @param readingChannelManager
	 *            New value for {@link #readingChannelManager}
	 */
	public void setReadingChannelManager(ReadingChannelManager readingChannelManager) {
		this.readingChannelManager = readingChannelManager;
	}

	/**
	 * Sets {@link #executorService}.
	 * 
	 * @param executorService
	 *            New value for {@link #executorService}
	 */
	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("executorService", executorService);
		return toStringBuilder.toString();
	}

}
