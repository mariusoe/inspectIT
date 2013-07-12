package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.util.TimeFrame;

/**
 * Label type to define the data time-frame in storage.
 * 
 * @author Ivan Senic
 * 
 */
public class DataTimeFrameLabelType extends AbstractStorageLabelType<TimeFrame> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -6293072492276850761L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGroupingEnabled() {
		return false;
	}

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
		return false;
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
	public boolean isEditable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<TimeFrame> getValueClass() {
		return TimeFrame.class;
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
