package info.novatec.inspectit.indexing.buffer.impl;

import org.apache.commons.lang.builder.ToStringBuilder;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferBranchIndexer;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

/**
 * Implementation of branch indexer for the {@link IBufferTreeComponent}. This indexer is
 * delegatinggeneration of the indexing keys to the {@link IBranchIndexer}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of the elements indexed.
 */
public class BufferBranchIndexer<E extends DefaultData> implements IBufferBranchIndexer<E> {

	/**
	 * Enum describing the child branch type.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum ChildBranchType {

		/**
		 * Normal branch.
		 * 
		 * @see Branch
		 */
		NORMAL_BRANCH,

		/**
		 * Leafing branch.
		 * 
		 * @see LeafingBranch
		 */
		LEAFING_BRANCH
	}

	/**
	 * Delegate indexer.
	 */
	private IBranchIndexer<E> delegateIndexer;

	/**
	 * Type of the child branch.
	 */
	private ChildBranchType childBranchType;

	/**
	 * Child indexer.
	 */
	private IBufferBranchIndexer<E> childBufferIndexer;

	/**
	 * Default constructor.
	 * 
	 * @param delegateIndexer
	 *            Delegate indexer that should generate keys.
	 */
	public BufferBranchIndexer(IBranchIndexer<E> delegateIndexer) {
		this(delegateIndexer, null, null);
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param delegateIndexer
	 *            Type of the delegate indexer that will actually generate keys for objects.
	 * @param childBranchType
	 *            Type of the child branch.
	 * @param childBufferIndexer
	 *            Indexer to be used in the child branch.
	 */
	public BufferBranchIndexer(IBranchIndexer<E> delegateIndexer, ChildBranchType childBranchType, IBufferBranchIndexer<E> childBufferIndexer) {
		this.delegateIndexer = delegateIndexer;
		this.childBranchType = childBranchType;
		this.childBufferIndexer = childBufferIndexer;
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
	public IBufferBranchIndexer<E> getChildIndexer() {
		return childBufferIndexer;
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
	public IBufferBranchIndexer<E> getNewInstance() {
		if (!sharedInstance()) {
			BufferBranchIndexer<E> bufferBranchIndexer = new BufferBranchIndexer<E>(delegateIndexer.getNewInstance(), childBranchType, childBufferIndexer);
			return bufferBranchIndexer;
		} else {
			throw new UnsupportedOperationException("Method getNewInstance() called on the Indexer that has a shared instance.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IBufferTreeComponent<E> getNextTreeComponent() {
		if (null != childBufferIndexer) {
			if (sharedInstance()) {
				if (childBranchType == ChildBranchType.NORMAL_BRANCH) {
					return new Branch<E>(childBufferIndexer);
				} else if (childBranchType == ChildBranchType.LEAFING_BRANCH) {
					return new LeafingBranch<E>(childBufferIndexer);
				}
			} else {
				if (childBranchType == ChildBranchType.NORMAL_BRANCH) {
					return new Branch<E>(getNewInstance());
				} else if (childBranchType == ChildBranchType.LEAFING_BRANCH) {
					return new LeafingBranch<E>(getNewInstance());
				}
			}
		} else {
			return new Leaf<E>();
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("delegateIndexer", delegateIndexer);
		toStringBuilder.append("childBranchType", childBranchType);
		return toStringBuilder.toString();
	}

}
