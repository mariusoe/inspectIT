package info.novatec.inspectit.storage;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

/**
 * Storage recorder that uses the {@link StorageWriter} to provide recording functionality.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageRecorder {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * Storage writer to use for writing.
	 */
	private StorageWriter storageWriter;

	/**
	 * Properties used when recording.
	 */
	private RecordingProperties recordingProperties;

	/**
	 * Is recording on.
	 */
	private volatile boolean recordingOn = false;

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
	 * Prepares the writer for recording by passing the data processors that will be used when
	 * {@link #record(DefaultData)} is called.
	 * 
	 * @param stWriter
	 *            Writer for executing writing tasks.
	 * @param recProperties
	 *            {@link RecordingProperties} used during the recording.
	 * @return True if the recording started successfully.
	 */
	public synchronized boolean startRecording(StorageWriter stWriter, RecordingProperties recProperties) {
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
			recordingProperties.setRecordStartDate(new Date());

			// prepare the processors if they are given
			Collection<AbstractDataProcessor> recordingDataProcessors = recordingProperties.getRecordingDataProcessors();
			if (null != recordingDataProcessors) {
				for (AbstractDataProcessor abstractDataProcessor : recordingDataProcessors) {
					abstractDataProcessor.setStorageWriter(storageWriter);
				}
			}

			recordingOn = true;

			if (log.isDebugEnabled()) {
				log.info("Recording started for storage: " + getStorageData());
			}
			return true;
		}
		return false;
	}

	/**
	 * Stops recording by flushing all the recording processors.
	 */
	public synchronized void stopRecording() {
		if (isRecordingOn()) {
			Collection<AbstractDataProcessor> recordingDataProcessors = recordingProperties.getRecordingDataProcessors();
			if (null != recordingDataProcessors) {
				for (AbstractDataProcessor abstractDataProcessor : recordingDataProcessors) {
					abstractDataProcessor.flush();
				}
			}

			if (log.isDebugEnabled()) {
				log.info("Recording stopped for storage: " + getStorageData());
			}

			recordingOn = false;
			recordingProperties = null;
			storageWriter = null;
		}
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
	 * Is recording active. The recording is active only when the {@link #storageWriter}
	 * {@link #recordingProperties} are set.
	 * 
	 * @return True if the recording is active.
	 */
	public boolean isRecordingOn() {
		return recordingOn;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("recordingOn", recordingOn);
		toStringBuilder.append("storageWriter", storageWriter);
		toStringBuilder.append("recordingProperties", recordingProperties);
		return toStringBuilder.toString();
	}
}
