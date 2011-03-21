package info.novatec.inspectit.storage;

import java.io.Serializable;

/**
 * Abstract storage data.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractStorageData implements IStorageIdProvider, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8161482616652852623L;

	/**
	 * Storage ID.
	 */
	private String id;

	/**
	 * Name.
	 */
	private String name;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getStorageFolder() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AbstractStorageData other = (AbstractStorageData) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "'" + name + "' (id=" + id + ")";
	}

}
