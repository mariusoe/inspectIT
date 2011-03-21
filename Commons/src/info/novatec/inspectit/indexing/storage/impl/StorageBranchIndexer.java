package info.novatec.inspectit.indexing.storage.impl;

import org.apache.commons.lang.builder.ToStringBuilder;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;
import info.novatec.inspectit.indexing.storage.IStorageBranchIndexer;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

/**
 * Implementation of the indexer for the {@link IStorageTreeComponent}. This indexer delegate the
 * key creation to the {@link IBranchIndexer}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of element indexed by indexer.
 */
public class StorageBranchIndexer<E extends DefaultData> implements IStorageBranchIndexer<E> {

	/**
	 * The delegate indexer for keys creation.
	 */
	private IBranchIndexer<E> delegateIndexer;

	/**
	 * The indexer that is next in the heirarchy.
	 */
	private StorageBranchIndexer<E> childIndexer;

	/**
	 * No-args constructor.
	 */
	public StorageBranchIndexer() {
	}

	/**
	 * 
	 * @param delegateIndexer
	 *            Provides delegate indexer with a constructor.
	 */
	public StorageBranchIndexer(IBranchIndexer<E> delegateIndexer) {
		this(delegateIndexer, null);
	}

	/**
	 * @param delegateIndexer
	 *            Provides delegate indexer with a constructor.
	 * @param childIndexer
	 *            Provides child indexer.
	 */
	public StorageBranchIndexer(IBranchIndexer<E> delegateIndexer, StorageBranchIndexer<E> childIndexer) {
		this.delegateIndexer = delegateIndexer;
		this.childIndexer = childIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		return delegateIndexer.getKey(element);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		return delegateIndexer.getKeys(query);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean sharedInstance() {
		return delegateIndexer.sharedInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageBranchIndexer<E> getNewInstance() {
		if (!sharedInstance()) {
			StorageBranchIndexer<E> storageBranchIndexer = new StorageBranchIndexer<E>(delegateIndexer.getNewInstance(), childIndexer);
			return storageBranchIndexer;
		} else {
			throw new UnsupportedOperationException("Method getNewInstance() called on the Indexer that has a shared instance.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageTreeComponent<E> getNextTreeComponent(E object) {
		if (null != childIndexer) {
			return new StorageBranch<E>(childIndexer);
		} else {
			if (object instanceof InvocationSequenceData) {
				return new ArrayBasedStorageLeaf<E>();
			} else {
				return new LeafWithNoDescriptors<E>();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childIndexer == null) ? 0 : childIndexer.hashCode());
		result = prime * result + ((delegateIndexer == null) ? 0 : delegateIndexer.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
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
		StorageBranchIndexer<E> other = (StorageBranchIndexer<E>) obj;
		if (childIndexer == null) {
			if (other.childIndexer != null) {
				return false;
			}
		} else if (!childIndexer.equals(other.childIndexer)) {
			return false;
		}
		if (delegateIndexer == null) {
			if (other.delegateIndexer != null) {
				return false;
			}
		} else if (!delegateIndexer.equals(other.delegateIndexer)) {
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
		toStringBuilder.append("delegateIndexer", delegateIndexer);
		toStringBuilder.append("childIndexer", childIndexer);
		return toStringBuilder.toString();
	}

}
