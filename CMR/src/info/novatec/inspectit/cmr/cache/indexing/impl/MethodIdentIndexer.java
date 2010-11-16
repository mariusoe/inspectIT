package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IndexQuery;
import info.novatec.inspectit.communication.MethodSensorData;

/**
 * {@link IBranchIndexer} that indexes on the method idents of {@link MethodSensorData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class MethodIdentIndexer<E extends MethodSensorData> extends AbstractIndexer<E> {

	/**
	 * Default constructor. No child indexer is set.
	 */
	public MethodIdentIndexer() {
		super();
	}

	/**
	 * Constructor that defines child indexer and child branch type. See
	 * {@link AbstractIndexer#AbstractIndexer(ChildBranchType, IBranchIndexer)}
	 * 
	 */
	public MethodIdentIndexer(ChildBranchType childBrunchType, IBranchIndexer<E> branchIndexer) {
		super(childBrunchType, branchIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (0 == element.getMethodIdent()) {
			return null;
		}
		return element.getMethodIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IndexQuery query) {
		if (0 == query.getMethodIdent()) {
			return null;
		}
		Object[] keys = new Object[1];
		keys[0] = query.getMethodIdent();
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
	 */
	public IBranchIndexer<E> getNewInstance() {
		throw new UnsupportedOperationException("Branch indexer can not return new instance because it uses the shared one.");
	}

}
