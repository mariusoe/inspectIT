package info.novatec.inspectit.storage.label.type;

import java.io.Serializable;

public abstract class AbstractCustomStorageLabelType<V> extends AbstractStorageLabelType<V> implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 4468771008746395551L;

	/**
	 * If is one per storage.
	 */
	private boolean onePerStorage;

	/**
	 * Name of this custom label type.
	 */
	private String name;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValueReusable() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMultiType() {
		return true;
	}

	/**
	 * Gets {@link #onePerStorage}.
	 * 
	 * @return {@link #onePerStorage}
	 */
	@Override
	public boolean isOnePerStorage() {
		return onePerStorage;
	}

	/**
	 * Sets {@link #onePerStorage}.
	 * 
	 * @param onePerStorage
	 *            New value for {@link #onePerStorage}
	 */
	public void setOnePerStorage(boolean onePerStorage) {
		this.onePerStorage = onePerStorage;
	}

	/**
	 * Gets {@link #name}.
	 * 
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name}.
	 * 
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (onePerStorage ? 1231 : 1237);
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
		AbstractCustomStorageLabelType<?> other = (AbstractCustomStorageLabelType<?>) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (onePerStorage != other.onePerStorage) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(AbstractStorageLabelType<?> other) {
		if (!AbstractCustomStorageLabelType.class.isAssignableFrom(other.getClass())) {
			return super.compareTo(other);
		} else {
			AbstractCustomStorageLabelType<?> abstractCustomStorageLabelType = (AbstractCustomStorageLabelType<?>) other;
			return name.compareTo(abstractCustomStorageLabelType.getName());
		}
	}

}
