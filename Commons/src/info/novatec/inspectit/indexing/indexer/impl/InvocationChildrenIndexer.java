package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

/**
 * Special indexer, that indexes invocations without children in one branch and every other object,
 * including invocations with children, in second one.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class InvocationChildrenIndexer<E extends DefaultData> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (element instanceof InvocationSequenceData) {
			InvocationSequenceData invoc = (InvocationSequenceData) element;
			if (invoc.getNestedSequences() == null || invoc.getNestedSequences().isEmpty()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		Object[] keys = new Object[1];
		if (query instanceof StorageIndexQuery) {
			if (((StorageIndexQuery) query).isOnlyInvocationsWithoutChildren()) {
				keys[0] = Boolean.TRUE;
			} else {
				keys[0] = Boolean.FALSE;
			}
		} else {
			keys[0] = Boolean.FALSE;
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
