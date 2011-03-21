package info.novatec.inspectit.indexing.storage.impl;

import org.apache.commons.lang.builder.ToStringBuilder;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;

/**
 * Storage descriptor. POJO that keeps information about where element is stored on disk.
 * 
 * @see IStorageDescriptor
 * @author Ivan Senic
 * 
 */
public class StorageDescriptor implements IStorageDescriptor {

	/**
	 * Channel id.
	 */
	private int channelId;

	/**
	 * Reference to the {@link SimpleStorageDescriptor} that will actually hold the position and
	 * size values.
	 */
	private SimpleStorageDescriptor simpleStorageDescriptor;

	/**
	 * Default constructor. Instantiates new {@link SimpleStorageDescriptor}.
	 */
	public StorageDescriptor() {
		simpleStorageDescriptor = new SimpleStorageDescriptor();
	}

	/**
	 * Instantiates new {@link SimpleStorageDescriptor} and assigns the channel ID.
	 * 
	 * @param channelId
	 *            Channel id to hold.
	 */
	public StorageDescriptor(int channelId) {
		this();
		this.channelId = channelId;
	}

	/**
	 * Assigns the channel ID and {@link SimpleStorageDescriptor}.
	 * 
	 * @param channelId
	 *            Channel id to hold.
	 * @param simpleStorageDescriptor
	 *            {@link SimpleStorageDescriptor} to hold.
	 */
	public StorageDescriptor(int channelId, SimpleStorageDescriptor simpleStorageDescriptor) {
		this.channelId = channelId;
		this.simpleStorageDescriptor = simpleStorageDescriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getChannelId() {
		return channelId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	/**
	 * @return the simpleStorageDescriptor
	 */
	public SimpleStorageDescriptor getSimpleStorageDescriptor() {
		return simpleStorageDescriptor;
	}

	/**
	 * @param simpleStorageDescriptor
	 *            the simpleStorageDescriptor to set
	 */
	public void setSimpleStorageDescriptor(SimpleStorageDescriptor simpleStorageDescriptor) {
		this.simpleStorageDescriptor = simpleStorageDescriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPosition() {
		return simpleStorageDescriptor.getPosition();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPosition(long position) {
		simpleStorageDescriptor.setPosition((int) position);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSize() {
		return simpleStorageDescriptor.getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSize(long size) {
		simpleStorageDescriptor.setSize((int) size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channelId;
		result = prime * result + ((simpleStorageDescriptor == null) ? 0 : simpleStorageDescriptor.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
		StorageDescriptor other = (StorageDescriptor) obj;
		if (channelId != other.channelId) {
			return false;
		}
		if (simpleStorageDescriptor == null) {
			if (other.simpleStorageDescriptor != null) {
				return false;
			}
		} else if (!simpleStorageDescriptor.equals(other.simpleStorageDescriptor)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("channelId", channelId);
		toStringBuilder.append("position", simpleStorageDescriptor.getPosition());
		toStringBuilder.append("size", simpleStorageDescriptor.getSize());
		return toStringBuilder.toString();
	}
}
