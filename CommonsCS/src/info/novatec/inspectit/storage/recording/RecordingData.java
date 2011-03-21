package info.novatec.inspectit.storage.recording;

import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.WritingStatus;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This POJO joins several recording information data.
 * 
 * @author Ivan Senic
 * 
 */
public class RecordingData implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4409533435016692576L;

	/**
	 * Recording properties.
	 */
	private RecordingProperties recordingProperties;

	/**
	 * {@link WritingStatus} of the recording storage writer.
	 */
	private WritingStatus recordingWritingStatus;

	/**
	 * Storage that is used for recording.
	 */
	private StorageData recordingStorage;

	/**
	 * No-arguments constructor.
	 */
	public RecordingData() {
	}

	/**
	 * @param recordingProperties
	 *            Sets the {@link #recordingProperties}.
	 * @param recordingWritingStatus
	 *            Sets the {@link #recordingWritingStatus}.
	 * @param recordingStorage
	 *            Sets the {@link #recordingStorage}.
	 */
	public RecordingData(RecordingProperties recordingProperties, WritingStatus recordingWritingStatus, StorageData recordingStorage) {
		super();
		this.recordingProperties = recordingProperties;
		this.recordingWritingStatus = recordingWritingStatus;
		this.recordingStorage = recordingStorage;
	}

	/**
	 * Gets {@link #recordingProperties}.
	 * 
	 * @return {@link #recordingProperties}
	 */
	public RecordingProperties getRecordingProperties() {
		return recordingProperties;
	}

	/**
	 * Sets {@link #recordingProperties}.
	 * 
	 * @param recordingProperties
	 *            New value for {@link #recordingProperties}
	 */
	public void setRecordingProperties(RecordingProperties recordingProperties) {
		this.recordingProperties = recordingProperties;
	}

	/**
	 * Gets {@link #recordingWritingStatus}.
	 * 
	 * @return {@link #recordingWritingStatus}
	 */
	public WritingStatus getRecordingWritingStatus() {
		return recordingWritingStatus;
	}

	/**
	 * Sets {@link #recordingWritingStatus}.
	 * 
	 * @param recordingWritingStatus
	 *            New value for {@link #recordingWritingStatus}
	 */
	public void setRecordingWritingStatus(WritingStatus recordingWritingStatus) {
		this.recordingWritingStatus = recordingWritingStatus;
	}

	/**
	 * Gets {@link #recordingStorage}.
	 * 
	 * @return {@link #recordingStorage}
	 */
	public StorageData getRecordingStorage() {
		return recordingStorage;
	}

	/**
	 * Sets {@link #recordingStorage}.
	 * 
	 * @param recordingStorage
	 *            New value for {@link #recordingStorage}
	 */
	public void setRecordingStorage(StorageData recordingStorage) {
		this.recordingStorage = recordingStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("recordingProperties", recordingProperties);
		toStringBuilder.append("recordingWritingStatus", recordingWritingStatus);
		toStringBuilder.append("recordingStorage", recordingStorage);
		return toStringBuilder.toString();
	}

}
