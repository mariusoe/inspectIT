package info.novatec.inspectit.indexing.storage.impl;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simple storage descriptor stores only position and size as int values, since this is enough
 * information to read a object if file is known. File specification will be done by
 * {@link StorageLeaf}s directly.
 * 
 * @author Ivan Senic
 * 
 */
public class SimpleStorageDescriptor {

	/**
	 * Position in file.
	 */
	private int position;

	/**
	 * Size.
	 */
	private int size;

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + position;
		result = prime * result + size;
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
		SimpleStorageDescriptor other = (SimpleStorageDescriptor) obj;
		if (position != other.position) {
			return false;
		}
		if (size != other.size) {
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
		toStringBuilder.append("position", position);
		toStringBuilder.append("size", size);
		return toStringBuilder.toString();
	}
}
