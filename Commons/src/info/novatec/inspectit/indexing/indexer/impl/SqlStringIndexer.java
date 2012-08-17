package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

/**
 * Indexer that indexes SQLs based on the query string. All other objects types are indexed with
 * same key.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class SqlStringIndexer<E extends DefaultData> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (element instanceof SqlStatementData) {
			SqlStatementData sqlStatementData = (SqlStatementData) element;
			if (null != sqlStatementData.getSql()) {
				return sqlStatementData.getSql().hashCode();
			}
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		if (query instanceof StorageIndexQuery) {
			if (null != ((StorageIndexQuery) query).getSql()) {
				Object[] keys = new Object[1];
				keys[0] = ((StorageIndexQuery) query).getSql().hashCode();
				return keys;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean sharedInstance() {
		return true;
	}

	/**
	 * {@inheritDoc}
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
