package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

/**
 * Type of the label to denote the Assignee.
 * 
 * @author Ivan Senic
 * 
 */
public class AssigneeLabelType extends AbstractStorageLabelType<String> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2732903969469885507L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOnePerStorage() {
		return true;
	}

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
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueClass() {
		return String.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getClass().hashCode();
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
		return true;
	}

}
