package info.novatec.inspectit.storage;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.nio.ByteBufferPoolOverflowException;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.write.WritingChannelManager;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * {@link StorageWriter} is class that contains shared functionality for writing data on one
 * storage. It can be overwritten, with special additional functionality, but care needs to be taken
 * that methods of this class are correctly called in super classes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageWriter {

	/**
	 * Amount of time to re-check if the writing tasks are done and finalization can start.
	 */
	private static final int FINALIZATION_TASKS_SLEEP_TIME = 500;

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * Total amount of tasks submitted to {@link #writingExecutorService}.
	 */
	private long totalTasks = 0;

	/**
	 * Total amount of finished tasks by {@link #writingExecutorService}.
	 */
	private long finishedTasks = 0;

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private StorageManager storageManager;

	/**
	 * Storage to write to.
	 */
	private StorageData storageData;

	/**
	 * Indexing tree handler.
	 */
	@Autowired
	private StorageIndexingTreeHandler indexingTreeHandler;

	/**
	 * Path used for writing.
	 */
	private Path writingFolderPath;

	/**
	 * How much serialization attempts there will be, before the objects writing is abandoned.
	 */
	@Value(value = "${storage.serializationAttempts}")
	private int serializationAttempts;

	/**
	 * {@link WritingChannelManager}.
	 */
	@Autowired
	private WritingChannelManager writingChannelManager;

	/**
	 * {@link ISerializer} used for object serialization.
	 */
	@Autowired
	private ISerializer serializer;

	/**
	 * {@link ExecutorService} for writing tasks.
	 */
	@Autowired
	@Resource(name = "storageExecutorService")
	private ScheduledThreadPoolExecutor writingExecutorService;

	/**
	 * {@link ByteBufferProvider}.
	 */
	@Autowired
	private ByteBufferProvider byteBufferProvider;

	/**
	 * Opened channels {@link Paths}. These paths need to be closed when writing is finalized.
	 */
	private Set<Path> openedChannelPaths = Collections.newSetFromMap(new ConcurrentHashMap<Path, Boolean>(32, 0.75f, 1));

	/**
	 * Defines if the writer is ready for writing, thus is writing active.
	 */
	private volatile boolean writingOn = false;

	/**
	 * Status of writing. Initially status is {@link WritingStatus#GOOD}.
	 */
	private WritingStatus writingStatus = WritingStatus.GOOD;

	/**
	 * If the writer is finalized.
	 */
	private boolean finalized = false;

	/**
	 * Process the list of objects against the all the {@link AbstractDataProcessor}s that are
	 * provided. Processor define which data will be stored, when and in which format.
	 * <p>
	 * If null is passed as a processors list the data will be directly written.
	 * 
	 * @param defaultDataList
	 *            List of objects to process.
	 * @param processors
	 *            List of processors. Can be null, and in this case direct write will be executed.
	 */
	public void process(Collection<? extends DefaultData> defaultDataList, Collection<AbstractDataProcessor> processors) {
		if (null != processors && !processors.isEmpty()) {
			// first prepare processors
			for (AbstractDataProcessor processor : processors) {
				processor.setStorageWriter(this);
			}

			// the write all data
			for (DefaultData defaultData : defaultDataList) {
				for (AbstractDataProcessor processor : processors) {
					processor.process(defaultData);
				}
			}

			// at the end flush the data from processors and reset its storage writer
			for (AbstractDataProcessor processor : processors) {
				processor.flush();
				processor.setStorageWriter(null);
			}
		} else {
			// the write all data with out processing
			for (DefaultData defaultData : defaultDataList) {
				this.write(defaultData);
			}
		}
	}

	/**
	 * Writes one object to the disk. This method is only submitting a new writing task, thus it is
	 * thread safe and very fast.
	 * 
	 * @param defaultData
	 *            Object to be written.
	 */
	public void write(DefaultData defaultData) {
		if (writingOn) {
			WriteTask writeTask = new WriteTask(defaultData);
			writingExecutorService.submit(writeTask);
		}
	}

	/**
	 * Informs the {@link StorageWriter} to prepare for writing. The writer will perform all
	 * necessary operations so that calls to {@link #write(DefaultData)} can be executed. The
	 * {@link StorageWriter} will be in prepared state until {@link #finalizeWrite()} method is
	 * called.
	 * 
	 * @param storageData
	 *            Storage to write to.
	 * @return True if the preparation was successfully done, otherwise false.
	 * @throws IOException
	 *             IOException occurred.
	 */
	public synchronized boolean prepareForWrite(StorageData storageData) throws IOException {
		if (!writingOn) {
			this.storageData = storageData;
			writingFolderPath = storageManager.getStoragePath(storageData);
			// if path does not exists create
			if (!Files.exists(writingFolderPath)) {
				Files.createDirectories(writingFolderPath);
			}

			// prepare the indexing tree handler
			indexingTreeHandler.prepare();

			writingOn = true;
			return true;
		}
		return false;
	}

	/**
	 * Performs all operation prior to finalizing the write and then calls {@link #finalizeWrite()}.
	 */
	public final synchronized void closeStorageWriter() {
		if (writingOn) {
			// mark writing false so that no more task are created
			writingOn = false;

			boolean logged = false;
			// check amount of active tasks
			while (true) {
				long activeTasks = getQueuedTaskCount();
				if (activeTasks > 0) {
					if (log.isDebugEnabled() && !logged) {
						log.info("Storage: " + storageData + " is waiting for finalization. Still " + activeTasks + " queued tasks need to be processed.");
						logged = true;
					}
					// if still are not done sleep
					try {
						Thread.sleep(FINALIZATION_TASKS_SLEEP_TIME);
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
				} else {
					break;
				}
			}

			if (log.isDebugEnabled()) {
				log.info("Finalization started for storage: " + storageData + ".");
			}

			// if yes finalize
			finalizeWrite();
		}
	}

	/**
	 * Stops recording if the recording is currently in process. This method will wait until all
	 * pending writing tasks are finished, but after it's invocation no new tasks will be accepted.
	 * <p>
	 * Sub-classes can override this method to include additional writes before the storage write is
	 * finalized. Note that the overriding of this method has to be in the way to first execute the
	 * additional saving, and the call super.finalizeWrite().
	 */
	protected synchronized void finalizeWrite() {
		if (!finalized) {
			try {
				// Disable new tasks from being submitted
				writingExecutorService.shutdown();
				try {
					// Wait a while for existing tasks to terminate
					if (!writingExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
						// Cancel currently executing tasks
						writingExecutorService.shutdownNow();
						// Wait a while for tasks to respond to being canceled
						if (!writingExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
							log.error("Executor service of the Storage writer for the storage " + storageData + " did not terminate.");
						}
					}
				} catch (InterruptedException ie) {
					// (Re-)Cancel if current thread also interrupted
					writingExecutorService.shutdownNow();
					// Preserve interrupt status
					Thread.currentThread().interrupt();
				}

				// when nothing more is left save the indexing tree
				indexingTreeHandler.finish();

				// close all opened channels
				for (Path channelPath : openedChannelPaths) {
					writingChannelManager.finalize(channelPath);
				}

				finalized = true;

				if (log.isDebugEnabled()) {
					log.info("Finalization done for storage: " + storageData + ".");
				}
			} catch (IOException e) {
				log.error("Finalze write failed.", e);
			}
		}
	}

	/**
	 * Number of queued tasks in the executor service.
	 * 
	 * @return Number of queued tasks in the executor service.
	 */
	public long getQueuedTaskCount() {
		return writingExecutorService.getTaskCount() - writingExecutorService.getCompletedTaskCount();
	}

	/**
	 * Writes any object to the file with given file name.
	 * 
	 * @param object
	 *            Object to write. Note that object of this kind has to be serializable by
	 *            {@link ISerializer}.
	 * @param fileName
	 *            Name of the file to save data to.
	 * @return True if the object was written successfully, otherwise false.
	 */
	public boolean writeNonDefaultDataObject(Object object, String fileName) {
		// Start the serialization, if serialization fails, we search for a bigger buffer
		boolean serializationPassed = false;
		int serializationAttemptsFailed = 0;
		ByteBuffer byteBuffer = byteBufferProvider.acquireByteBuffer();
		int capacity = byteBuffer.capacity();
		while (!serializationPassed) {
			if (serializationAttemptsFailed >= serializationAttempts) {
				log.error("All " + serializationAttempts + " serialization attempts for the object " + object + " failed. Data will be skipped.");
				return false;
			}

			if (serializationAttemptsFailed > 0) {
				try {
					byteBuffer = byteBufferProvider.acquireByteBuffer(capacity);
				} catch (ByteBufferPoolOverflowException e) {
					log.error("Buffer with enough capacity can not be created for the object " + object + ". Data will be skipped.", e);
					return false;
				}
			}

			// make sure buffer is clear
			if (byteBuffer.position() > 0) {
				byteBuffer.clear();
			}

			try {
				serializer.serialize(object, byteBuffer);
				serializationPassed = true;
			} catch (SerializationException e) {
				serializationAttemptsFailed++;
				capacity *= 3;
				byteBufferProvider.releaseByteBuffer(byteBuffer);
				if (log.isDebugEnabled()) {
					log.warn("Serialization failed. Attempt number " + serializationAttemptsFailed + " out of " + serializationAttempts + ".", e);
				}
			}
		}

		// flip so that writing channel manager knows where to start
		byteBuffer.flip();

		// prepare path
		Path channelPath = writingFolderPath.resolve(fileName);
		if (Files.exists(channelPath)) {
			try {
				Files.delete(channelPath);
			} catch (IOException e) {
				log.error("Exception thrown trying to delete file from disk", e);
			}
		}
		openedChannelPaths.add(channelPath);

		// create completion runnable
		final ByteBuffer finalByteBuffer = byteBuffer;
		WriteReadCompletionRunnable completionRunnable = new WriteReadCompletionRunnable() {
			@Override
			public void run() {
				byteBufferProvider.releaseByteBuffer(finalByteBuffer);
			}
		};

		// execute write
		try {
			writingChannelManager.write(byteBuffer, channelPath, completionRunnable);
			return true;
		} catch (IOException e) {
			log.error("Execption occured while attempting to write data to disk", e);
			return false;
		}
	}

	/**
	 * Updates the write status.
	 */
	@Scheduled(fixedDelay = 30000)
	protected void checkWritingStatus() {
		if (null != writingExecutorService) {
			long completedTasks = writingExecutorService.getCompletedTaskCount();
			long queuedTasks = writingExecutorService.getTaskCount() - completedTasks;

			long arrivedTasksForPeriod = queuedTasks + completedTasks - totalTasks;
			long finishedTasksForPeriod = completedTasks - finishedTasks;

			writingStatus = WritingStatus.getWritingStatus(arrivedTasksForPeriod, finishedTasksForPeriod);

			finishedTasks = completedTasks;
			totalTasks = completedTasks + queuedTasks;
		} else {
			writingStatus = WritingStatus.GOOD;
		}
	}

	/**
	 * Task for writing one {@link DefaultData} object to the disk.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public class WriteTask implements Runnable {

		/**
		 * reference to write data.
		 */
		private SoftReference<DefaultData> referenceToWriteData;

		/**
		 * Default constructor. Object to be written.
		 * 
		 * @param data
		 *            Data to be written.
		 */
		public WriteTask(DefaultData data) {
			referenceToWriteData = new SoftReference<DefaultData>(data);
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			try {
				// get object from soft reference
				final DefaultData data = referenceToWriteData.get();
				if (null == data) {
					log.warn("Failed to write data to storage. The data to be written was already garbage collected due to the high amount of writting tasks.");
					return;
				}

				int channelId = 0;
				// get channel id
				try {
					channelId = indexingTreeHandler.startWrite(this);
				} catch (IndexingException e) {
					indexingTreeHandler.writeFailed(this);
					log.error("Indexing execption occured while attempting to write data to disk.", e);
					return;
				}

				if (0 == channelId) {
					indexingTreeHandler.writeFailed(this);
					log.error("Channel ID could not be obtained during attempt to write data to disk. Data will be skipped.");
					return;
				}

				// Start the serialization, if serialization fails, we search for a bigger buffer
				boolean serializationPassed = false;
				int serializationAttemptsFailed = 0;
				ByteBuffer byteBuffer = byteBufferProvider.acquireByteBuffer();
				int capacity = byteBuffer.capacity();
				while (!serializationPassed) {
					if (serializationAttemptsFailed >= serializationAttempts) {
						// we won't try till end of the world
						// we need to remove the reserved place for the indexing
						indexingTreeHandler.writeFailed(this);
						log.error("All " + serializationAttempts + " serialization attempts for the object " + data + " failed. Data will be skipped.");
						return;
					}

					if (serializationAttemptsFailed > 0) {
						try {
							byteBuffer = byteBufferProvider.acquireByteBuffer(capacity);
						} catch (ByteBufferPoolOverflowException e) {
							indexingTreeHandler.writeFailed(this);
							log.error("Buffer with enough capacity can not be created for the object " + data + ". Data will be skipped.", e);
							return;
						}
					}

					// make sure buffer is clear
					if (byteBuffer.position() > 0) {
						byteBuffer.clear();
					}

					try {
						serializer.serialize(data, byteBuffer);
						serializationPassed = true;
					} catch (SerializationException e) {
						serializationAttemptsFailed++;
						capacity *= 3;
						byteBufferProvider.releaseByteBuffer(byteBuffer);
						if (log.isDebugEnabled()) {
							log.warn("Serialization failed. Attempt number " + serializationAttemptsFailed + " out of " + serializationAttempts + ".", e);
						}
					}

				}
				// flip so that writing channel manager knows where to start
				byteBuffer.flip();

				// remember the writing size
				long writingSize = byteBuffer.limit() - byteBuffer.position();

				// final reference needed because of the runnable
				final ByteBuffer byteBufferFinal = byteBuffer;

				WriteReadCompletionRunnable completionRunnable = new WriteReadCompletionRunnable() {
					@Override
					public void run() {
						// clear and return to queue for somebody else
						byteBufferProvider.releaseByteBuffer(byteBufferFinal);

						// if the write fails, we have to remove the data from index
						if (!this.isCompleted()) {
							indexingTreeHandler.writeFailed(WriteTask.this);
						}
					}
				};

				// write to disk
				Path channelPath = storageManager.getChannelPath(storageData, channelId);
				openedChannelPaths.add(channelPath);
				try {
					long position = writingChannelManager.write(byteBuffer, channelPath, completionRunnable);
					indexingTreeHandler.writeSuccessful(this, position, writingSize);
				} catch (IOException e) {
					// remove from indexing tree if exception occurs
					indexingTreeHandler.writeFailed(this);
					log.error("Execption occured while attempting to write data to disk", e);
					return;
				}
			} catch (Throwable t) {
				// catch any exception
				indexingTreeHandler.writeFailed(this);
				log.error("Unknow exception occured during data write", t);
			}
		}

		/**
		 * @return Returns data to be written by this task.
		 */
		public DefaultData getData() {
			return referenceToWriteData.get();
		}

	}

	/**
	 * Returns write path for this writer.
	 * 
	 * @return Returns write path for this writer.
	 */
	public Path getWritingFolderPath() {
		return writingFolderPath;
	}

	/**
	 * Returns executor service status. This methods just returns the result of
	 * {@link #executorService#toString()} method.
	 * 
	 * @return Returns executor service status. This methods just returns the result of
	 *         {@link #executorService#toString()} method.
	 */
	public String getExecutorServiceStatus() {
		return writingExecutorService.toString();
	}

	/**
	 * Gets {@link #writingOn}.
	 * 
	 * @return {@link #writingOn}
	 */
	public boolean isWritingOn() {
		return writingOn;
	}

	/**
	 * Gets {@link #storageData}.
	 * 
	 * @return {@link #storageData}
	 */
	public StorageData getStorageData() {
		return storageData;
	}

	/**
	 * Gets {@link #writingStatus}.
	 * 
	 * @return {@link #writingStatus}
	 */
	public WritingStatus getWritingStatus() {
		return writingStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		indexingTreeHandler.registerStorageWriter(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("storageData", storageData);
		toStringBuilder.append("writingFolderPath", writingFolderPath);
		toStringBuilder.append("serializationAttempts", serializationAttempts);
		toStringBuilder.append("writingOn", writingOn);
		toStringBuilder.append("executorService", writingExecutorService);
		toStringBuilder.append("openedChannelPaths", openedChannelPaths);
		return toStringBuilder.toString();
	}

}