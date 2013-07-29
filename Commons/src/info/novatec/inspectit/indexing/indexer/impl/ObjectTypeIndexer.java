package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.ITreeComponent;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

/**
 * {@link IBranchIndexer} that makes indexes based on a object class. Thus all same object types
 * will be in one {@link ITreeComponent}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class ObjectTypeIndexer<E extends DefaultData> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		return element.getClass();
	}

	/**
	 * {@inheritDoc}
	 */

	public Object[] getKeys(IIndexQuery query) {
		if (null == query.getObjectClasses()) {
			return new Object[0];
		}
		Object[] keys = new Object[query.getObjectClasses().size()];
		int index = 0;
		for (Object key : query.getObjectClasses()) {
			keys[index++] = key;
		}
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
