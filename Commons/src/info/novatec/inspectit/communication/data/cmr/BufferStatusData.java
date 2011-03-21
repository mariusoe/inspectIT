package info.novatec.inspectit.communication.data.cmr;

import info.novatec.inspectit.communication.DefaultData;

import java.io.Serializable;

/**
 * Class that hold all information about a buffer status.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferStatusData implements Serializable {

	/**
	 * generated UID.
	 */
	private static final long serialVersionUID = 126245907015200153L;

	/**
	 * Current buffer occupancy in bytes.
	 */
	private long currentBufferSize;

	/**
	 * Maximum buffer occupancy in bytes.
	 */
	private long maxBufferSize;

	/**
	 * Oldest element in buffer.
	 */
	private DefaultData bufferOldestElement;

	/**
	 * Newest element in the buffer.
	 */
	private DefaultData bufferNewestElement;

	/**
	 * @return the currentBufferSize
	 */
	public long getCurrentBufferSize() {
		return currentBufferSize;
	}

	/**
	 * @param currentBufferSize the currentBufferSize to set
	 */
	public void setCurrentBufferSize(long currentBufferSize) {
		this.currentBufferSize = currentBufferSize;
	}

	/**
	 * @return the maxBufferSize
	 */
	public long getMaxBufferSize() {
		return maxBufferSize;
	}

	/**
	 * @param maxBufferSize the maxBufferSize to set
	 */
	public void setMaxBufferSize(long maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	/**
	 * @return the bufferOldestElement
	 */
	public DefaultData getBufferOldestElement() {
		return bufferOldestElement;
	}

	/**
	 * @param bufferOldestElement the bufferOldestElement to set
	 */
	public void setBufferOldestElement(DefaultData bufferOldestElement) {
		this.bufferOldestElement = bufferOldestElement;
	}

	/**
	 * @return the bufferNewestElement
	 */
	public DefaultData getBufferNewestElement() {
		return bufferNewestElement;
	}

	/**
	 * @param bufferNewestElement the bufferNewestElement to set
	 */
	public void setBufferNewestElement(DefaultData bufferNewestElement) {
		this.bufferNewestElement = bufferNewestElement;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bufferNewestElement == null) ? 0 : bufferNewestElement.hashCode());
		result = prime * result + ((bufferOldestElement == null) ? 0 : bufferOldestElement.hashCode());
		result = prime * result + (int) (currentBufferSize ^ (currentBufferSize >>> 32));
		result = prime * result + (int) (maxBufferSize ^ (maxBufferSize >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BufferStatusData other = (BufferStatusData) obj;
		if (bufferNewestElement == null) {
			if (other.bufferNewestElement != null) {
				return false;
			}
		} else if (!bufferNewestElement.equals(other.bufferNewestElement)) {
			return false;
		}
		if (bufferOldestElement == null) {
			if (other.bufferOldestElement != null) {
				return false;
			}
		} else if (!bufferOldestElement.equals(other.bufferOldestElement)) {
			return false;
		}
		if (currentBufferSize != other.currentBufferSize) {
			return false;
		}
		if (maxBufferSize != other.maxBufferSize) {
			return false;
		}
		return true;
	}

}
