package info.novatec.inspectit.cmr.storage;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CMR storage recorder that uses the {@link StorageWriter} to provide recording functionality..
 * Handles the scheduling, starting and stopping of the recording.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class CmrStorageRecorder {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * CMR storage manager.
	 */
	@Autowired
	private CmrStorageManager cmrStorageManager;

	/**
	 * {@link ExecutorService} for tasks of the tree handling.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * Future for the task of recording stop.
	 */
	private ScheduledFuture<?> stopRecordingFuture;

	/**
	 * Future for the task of recording start.
	 */
	private ScheduledFuture<?> startRecordingFuture;

	/**
	 * Storage writer to use for writing.
	 */
	private StorageWriter storageWriter;

	/**
	 * Properties used when recording.
	 */
	private RecordingProperties recordingProperties;

	/**
	 * Recording state. By default is not active.
	 */
	private volatile RecordingState recordingState = RecordingState.OFF;

	/**
	 * Records this object, by processing it against the all the recording
	 * {@link AbstractDataProcessor}s that are defined in the {@link RecordingProperties} for this
	 * {@link StorageWriter}. Processor define which data will be stored, when and in which format.
	 * <p>
	 * If the processors are not set, then the normal write will be executed.
	 * 
	 * @param defaultData
	 *            Object to be processed.
	 */
	public void record(DefaultData defaultData) {
		if (isRecordingOn() && storageWriter.isWritingOn()) {
			Collection<AbstractDataProcessor> recordingDataProcessors = recordingProperties.getRecordingDataProcessors();
			if (null != recordingDataProcessors && !recordingDataProcessors.isEmpty()) {
				for (AbstractDataProcessor dataProcessor : recordingDataProcessors) {
					dataProcessor.process(defaultData);
				}
			} else {
				storageWriter.write(defaultData);
			}
		}
	}

	/**
	 * Starts or schedules the recording. If the recording properties define the start date that is
	 * after current time, recording will be scheduled. Otherwise it is started right away.
	 * 
	 * @param stWriter
	 *            Writer for executing writing tasks.
	 * @param recProperties
	 *            {@link RecordingProperties} used during the recording.
	 */
	public synchronized void startOrScheduleRecording(final StorageWriter stWriter, final RecordingProperties recProperties) {
		if (!isRecordingOn() && !isRecordingScheduled()) {
			if (null == stWriter) {
				throw new IllegalArgumentException("Storage writer can not be null. Recording will not be started.");
			} else if (!stWriter.isWritingOn()) {
				throw new IllegalArgumentException("Storage writer must be prepared for write. Recording will not be started.");
			}
			if (null == recProperties) {
				throw new IllegalArgumentException("Recording properties can not be null. Recording will not be started.");
			}
			storageWriter = stWriter;
			recordingProperties = recProperties;

			long startDelay = recProperties.getStartDelay();
			if (startDelay > 0) {
				recordingState = RecordingState.SCHEDULED;
				Runnable startRecordingRunnable = new Runnable() {
					@Override
					public void run() {
						CmrStorageRecorder.this.startRecording(storageWriter, recordingProperties);
					}
				};
				startRecordingFuture = executorService.schedule(startRecordingRunnable, startDelay, TimeUnit.MILLISECONDS);
				Date recordStartDate = new Date(System.currentTimeMillis() + startDelay);
				recordingProperties.setRecordStartDate(recordStartDate);
			} else {
				startRecording(storageWriter, recordingProperties);
			}

		}
	}

	/**
	 * Prepares the writer for recording by passing the data processors that will be used when
	 * {@link #record(DefaultData)} is called.
	 * 
	 * @param stWriter
	 *            Writer for executing writing tasks.
	 * @param recProperties
	 *            {@link RecordingProperties} used during the recording.
	 */
	protected synchronized void startRecording(StorageWriter stWriter, RecordingProperties recProperties) {
		if (!isRecordingOn()) {
			if (null == stWriter) {
				throw new IllegalArgumentException("Storage writer can not be null. Recording will not be started.");
			} else if (!stWriter.isWritingOn()) {
				throw new IllegalArgumentException("Storage writer must be prepared for write. Recording will not be started.");
			}
			if (null == recProperties) {
				throw new IllegalArgumentException("Recording properties can not be null. Recording will not be started.");
			}

			storageWriter = stWriter;
			recordingProperties = recProperties;

			// prepare the processors if they are given
			Collection<AbstractDataProcessor> recordingDataProcessors = recordingProperties.getRecordingDataProcessors();
			if (null != recordingDataProcessors) {
				for (AbstractDataProcessor abstractDataProcessor : recordingDataProcessors) {
					abstractDataProcessor.setStorageWriter(storageWriter);
				}
			}

			// update state
			recordingState = RecordingState.ON;

			// set start and end dates
			if (null != recordingProperties) {
				recordingProperties.setRecordStartDate(new Date());

				// set the task for stopping the recording if stop date is provided
				long recordingDuration = recordingProperties.getRecordDuration();
				if (recordingDuration > 0) {
					Runnable stopRecordingRunnable = new Runnable() {
						@Override
						public void run() {
							try {
								cmrStorageManager.stopRecording();
							} catch (Exception e) {
								log.warn("Automatic stop of recording failed for the storage: " + getStorageData(), e);
							}
						}
					};

					stopRecordingFuture = executorService.schedule(stopRecordingRunnable, recordingDuration, TimeUnit.MILLISECONDS);
					Date recordEndDate = new Date(System.currentTimeMillis() + recordingDuration);
					recordingProperties.setRecordEndDate(recordEndDate);
				}
			}

			if (log.isDebugEnabled()) {
				log.info("Recording started for storage: " + getStorageData());
			}
		}
	}

	/**
	 * Stops recording by flushing all the recording processors.
	 */
	public synchronized void stopRecording() {
		if (isRecordingOn()) {
			if (null != stopRecordingFuture) {
				if (!stopRecordingFuture.isDone() && !stopRecordingFuture.isCancelled()) {
					stopRecordingFuture.cancel(false);
				}
				stopRecordingFuture = null;
			}

			Collection<AbstractDataProcessor> recordingDataProcessors = recordingProperties.getRecordingDataProcessors();
			if (null != recordingDataProcessors) {
				for (AbstractDataProcessor abstractDataProcessor : recordingDataProcessors) {
					abstractDataProcessor.flush();
				}
			}

			if (log.isDebugEnabled()) {
				log.info("Recording stopped for storage: " + getStorageData());
			}
		} else if (isRecordingScheduled()) {
			if (null != startRecordingFuture) {
				if (!startRecordingFuture.isDone() && !startRecordingFuture.isCancelled()) {
					startRecordingFuture.cancel(false);
				}
				startRecordingFuture = null;
			}
		}

		storageWriter = null;
		recordingProperties = null;
		recordingState = RecordingState.OFF;
	}

	/**
	 * Is recording active. The recording is active only when the {@link #storageWriter}
	 * {@link #recordingProperties} are set.
	 * 
	 * @return True if the recording is active.
	 */
	public boolean isRecordingOn() {
		return recordingState == RecordingState.ON;
	}

	/**
	 * Is recording scheduled.
	 * 
	 * @return True if the recording is scheduled.
	 */
	public boolean isRecordingScheduled() {
		return recordingState == RecordingState.SCHEDULED;
	}

	/**
	 * Returns the {@link StorageData} that is used for recording.
	 * 
	 * @return Returns the {@link StorageData} that is used for recording.
	 */
	protected StorageData getStorageData() {
		return storageWriter.getStorageData();
	}

	/**
	 * Gets {@link #recordingState}.
	 * 
	 * @return {@link #recordingState}
	 */
	public RecordingState getRecordingState() {
		return recordingState;
	}

	/**
	 * @return the storageWriter
	 */
	public StorageWriter getStorageWriter() {
		return storageWriter;
	}

	/**
	 * @return the recordingProperties
	 */
	public RecordingProperties getRecordingProperties() {
		return recordingProperties;
	}

}
