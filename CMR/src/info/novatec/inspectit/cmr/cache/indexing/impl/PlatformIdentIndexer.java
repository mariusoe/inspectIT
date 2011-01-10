package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IndexQuery;
import info.novatec.inspectit.communication.DefaultData;

/**
 * {@link IBranchIndexer} that indexes on the platform idents of {@link DefaultData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class PlatformIdentIndexer<E extends DefaultData> extends AbstractIndexer<E> {

	/**
	 * Default constructor. No child indexer is set.
	 */
	public PlatformIdentIndexer() {
		super();
	}

	/**
	 * Constructor that defines child indexer and child branch type. See
	 * {@link AbstractIndexer#AbstractIndexer(ChildBranchType, IBranchIndexer)}
	 * 
	 * @param childBrunchType 
	 * @param branchIndexer 
	 */
	public PlatformIdentIndexer(ChildBranchType childBrunchType, IBranchIndexer<E> branchIndexer) {
		super(childBrunchType, branchIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (0 == element.getPlatformIdent()) {
			return null;
		}
		return element.getPlatformIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IndexQuery query) {
		if (0 == query.getPlatformIdent()) {
			return null;
		}
		Object[] keys = new Object[1];
		keys[0] = query.getPlatformIdent();
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
