package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.communication.DefaultData;

/**
 * {@link IBranchIndexer} that indexes on the sensor type idents of {@link DefaultData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class SensorTypeIdentIndexer<E extends DefaultData> extends AbstractIndexer<E> {

	/**
	 * Default constructor. No child indexer is set.
	 */
	public SensorTypeIdentIndexer() {
		super();
	}

	/**
	 * Constructor that defines child indexer and child branch type. See
	 * {@link AbstractIndexer#AbstractIndexer(ChildBranchType, IBranchIndexer)}
	 * 
	 * @param childBranchType 
	 * @param branchIndexer 
	 */
	public SensorTypeIdentIndexer(ChildBranchType childBranchType, IBranchIndexer<E> branchIndexer) {
		super(childBranchType, branchIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (0 == element.getSensorTypeIdent()) {
			return null;
		}
		return element.getSensorTypeIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		if (0 == query.getSensorTypeIdent()) {
			return null;
		}
		Object[] keys = new Object[1];
		keys[0] = query.getSensorTypeIdent();
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
