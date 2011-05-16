package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.communication.DefaultData;

/**
 * {@link IBranchIndexer} that makes indexes based on a object class. Thus all same object types
 * will be in one {@link ITreeComponent}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class ObjectTypeIndexer<E extends DefaultData> extends AbstractIndexer<E> {

	/**
	 * Default constructor. No child indexer is set.
	 */
	public ObjectTypeIndexer() {
		super();
	}

	/**
	 * Constructor that defines child indexer and child branch type. See
	 * {@link AbstractIndexer#AbstractIndexer(ChildBranchType, IBranchIndexer)}
	 * 
	 * @param childBranchType 
	 * @param branchIndexer 
	 */
	public ObjectTypeIndexer(ChildBranchType childBranchType, IBranchIndexer<E> branchIndexer) {
		super(childBranchType, branchIndexer);
	}

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
			return null;
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

}
