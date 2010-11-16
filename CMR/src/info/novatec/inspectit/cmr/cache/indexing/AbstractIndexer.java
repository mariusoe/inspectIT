package info.novatec.inspectit.cmr.cache.indexing;

import info.novatec.inspectit.cmr.cache.indexing.impl.Branch;
import info.novatec.inspectit.cmr.cache.indexing.impl.Leaf;
import info.novatec.inspectit.cmr.cache.indexing.impl.LeafingBranch;
import info.novatec.inspectit.communication.DefaultData;

/**
 * Abstract {@link IBranchIndexer} that has functionality for suppling the child indexer.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Element type that indexer can return keys for.
 */
public abstract class AbstractIndexer<E extends DefaultData> implements IBranchIndexer<E> {

	/**
	 * Enum that defines the type of the child brunch.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum ChildBranchType {
		NORMAL_BRANCH, LEAFING_BRANCH;
	}

	/**
	 * Child indexer.
	 */
	protected IBranchIndexer<E> childIndexer;

	/**
	 * Type of the child brunch.
	 */
	protected ChildBranchType childBranchType;

	/**
	 * Default constructor. No child indexer is set.
	 */
	public AbstractIndexer() {
		super();
	}

	/**
	 * Constructor that defines child indexer and child branch type.
	 * 
	 * @param childBrunchType
	 *            Type of the branch that will be made under {@link Branch} that this indexer is
	 *            serving.
	 * @param childIndexer
	 *            Indexer for the branch that will be made under {@link Branch} that this indexer is
	 *            serving.
	 * 
	 */
	public AbstractIndexer(ChildBranchType childBranchType, IBranchIndexer<E> childIndexer) {
		super();
		this.childIndexer = childIndexer;
		this.childBranchType = childBranchType;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBranchIndexer<E> getChildIndexer() {
		return childIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITreeComponent<E> getNextTreeComponent() {
		if (null != childIndexer) {
			if (childIndexer.sharedInstance()) {
				if (childBranchType == ChildBranchType.NORMAL_BRANCH) {
					return new Branch<E>(childIndexer);
				} else if (childBranchType == ChildBranchType.LEAFING_BRANCH) {
					return new LeafingBranch<E>(childIndexer);
				}
			} else {
				if (childBranchType == ChildBranchType.NORMAL_BRANCH) {
					return new Branch<E>(childIndexer.getNewInstance());
				} else if (childBranchType == ChildBranchType.LEAFING_BRANCH) {
					return new LeafingBranch<E>(childIndexer.getNewInstance());
				}
			}
		} else {
			return new Leaf<E>();
		}
		return null;
	}

}
