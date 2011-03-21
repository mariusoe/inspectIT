package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

/**
 * {@link IBranchIndexer} that indexes on the sensor type idents of {@link DefaultData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class SensorTypeIdentIndexer<E extends DefaultData> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (0 == element.getSensorTypeIdent()) {
			return null;
		}
		return element.getSensorTypeIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		if (0 == query.getSensorTypeIdent()) {
			return null;
		}
		Object[] keys = new Object[1];
		keys[0] = query.getSensorTypeIdent();
		return keys;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean sharedInstance() {
		return true;
	}

	/**
	 * Not supported.
	 * 
	 * @return Not supported.
	 */
	public IBranchIndexer<E> getNewInstance() {
		throw new UnsupportedOperationException("Branch indexer can not return new instance because it uses the shared one.");
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
