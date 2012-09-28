package info.novatec.inspectit.storage.recording;

import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class for holding the recording properties.
 * 
 * @author Ivan Senic
 * 
 */
public class RecordingProperties implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8838581567710175793L;

	/**
	 * Date when the recording started.
	 */
	private Date recordStartDate;

	/**
	 * Date when the recording should be stopped.
	 */
	private Date recordEndDate;

	/**
	 * Collection of recording processors. if supplied this processors will be used to process
	 * recording data.
	 */
	private Collection<AbstractDataProcessor> recordingDataProcessors;

	/**
	 * @return the recordStartDate
	 */
	public Date getRecordStartDate() {
		return recordStartDate;
	}

	/**
	 * @param recordStartDate
	 *            the recordStartDate to set
	 */
	public void setRecordStartDate(Date recordStartDate) {
		this.recordStartDate = recordStartDate;
	}

	/**
	 * @return the recordEndDate
	 */
	public Date getRecordEndDate() {
		return recordEndDate;
	}

	/**
	 * @param recordEndDate
	 *            the recordEndDate to set
	 */
	public void setRecordEndDate(Date recordEndDate) {
		this.recordEndDate = recordEndDate;
	}

	/**
	 * @return the recordingDataProcessors
	 */
	public Collection<AbstractDataProcessor> getRecordingDataProcessors() {
		return recordingDataProcessors;
	}

	/**
	 * @param recordingDataProcessors
	 *            the recordingDataProcessors to set
	 */
	public void setRecordingDataProcessors(Collection<AbstractDataProcessor> recordingDataProcessors) {
		this.recordingDataProcessors = recordingDataProcessors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("recordEndDate", recordEndDate);
		toStringBuilder.append("recordingDataProcessors", recordingDataProcessors);
		return toStringBuilder.toString();
	}

}
