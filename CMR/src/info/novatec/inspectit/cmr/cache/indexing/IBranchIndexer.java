package info.novatec.inspectit.cmr.cache.indexing;

/**
 * {@link IBranchIndexer} supplies the {@link Branch} with information about mapping keys.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Element type that indexer can return keys for.
 */
public interface IBranchIndexer<E> {

	/**
	 * Returns the key for one element.
	 * 
	 * @param element 
	 * @return Key or null if passed element is null, or indexing value is not set.
	 */
	Object getKey(E element);

	/**
	 * Return arrays of mapping keys that correspond to the passed query.
	 * 
	 * @param query 
	 * @return Keys or null if no keys are associated with query.
	 */
	Object[] getKeys(IndexQuery query);

	/**
	 * Returns the {@link IBranchIndexer} for next {@link ITreeComponent} in the tree.
	 * 
	 * @return Child indexer.
	 */
	IBranchIndexer<E> getChildIndexer();

	/**
	 * Returns the correct {@link ITreeComponent} for the next level. If the
	 * {@link #getChildIndexer()} returned object is not null the tree will create new
	 * {@link Branch} (or its subclass), otherwise new {@link Leaf}.
	 * 
	 * @return Next tree component.
	 */
	ITreeComponent<E> getNextTreeComponent();

	/**
	 * Returns if the implementation of the {@link IBranchIndexer} can be used as shared instance,
	 * meaning if all branches on the level where this indexer is used can use one and same
	 * instance.
	 * 
	 * @return If instance of indexer is shared between all indexers of same type.
	 */
	boolean sharedInstance();

	/**
	 * Returns the new instance of the same {@link IBranchIndexer} with correct relationship to its
	 * child indexer. This method will be used only if {@link #sharedInstance()} returns false, thus
	 * with indexer that can not have same instance for all branches on the level where it is used.
	 * 
	 * @return New instance of indexer of same type.
	 */
	IBranchIndexer<E> getNewInstance();

}
