package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IndexQuery;
import info.novatec.inspectit.communication.DefaultData;

import java.util.ArrayList;
import java.util.List;

/**
 * This type of branch also maps all elements that are belonging to its children, to a separate
 * leaf. Thus, when the tree includes this kind if a branch, it can perform queries faster. The
 * drawback is that this kind of branch requires more heap memory to store all the references.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class LeafingBranch<E extends DefaultData> extends Branch<E> {

	/**
	 * Additional leaf to the brunch, where all elements that are under this branch would also be
	 * referenced.
	 */
	private Leaf<E> leaf = new Leaf<E>();

	/**
	 * Default constructor.
	 * 
	 * @param branchIndexer
	 */
	public LeafingBranch(IBranchIndexer<E> branchIndexer) {
		super(branchIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(E element) throws IndexingException {
		super.put(element);
		leaf.put(element);
	};

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does not search for the element in tree, but returns it directly from its
	 * leaf.
	 */
	@Override
	public E get(E template) {
		return leaf.get(template);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getAndRemove(E template) {
		E result = leaf.getAndRemove(template);
		super.getAndRemove(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does not query the tree structure below if query data is not indexed
	 * below this branch. If this is the case, it just returns the elements from its own leaf.
	 */
	@Override
	public List<E> query(IndexQuery query) {
		Object[] keys = branchIndexer.getKeys(query);
		if (null == keys) {
			if (isQueryIndexedUnder(query)) {
				return queryAllTreeComponents(query);
			} else {
				return leaf.query(query);
			}
		} else {
			if (1 == keys.length) {
				// if only one key is returned, search in exactly this one
				return querySingleKey(query, keys[0]);
			} else {
				// combine results for all keys
				List<E> results = new ArrayList<E>();
				for (Object key : keys) {
					List<E> componentResult = querySingleKey(query, key);
					if (null != componentResult && !componentResult.isEmpty()) {
						results.addAll(componentResult);
					}
				}
				return results;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getComponentSize(IObjectSizes objectSizes) {
		long size = super.getComponentSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size += leaf.getComponentSize(objectSizes);
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean clean() {
		leaf.clean();
		return super.clean();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAll() {
		leaf.clearAll();
		super.clearAll();
	}

	/**
	 * Returns true if passed query object data will be indexed somewhere under this leafing branch,
	 * otherwise false.
	 * 
	 * @param query
	 *            Query to check for.
	 * @return Returns true if passed query object data will be indexed somewhere under this leafing
	 *         branch, otherwise false.
	 */
	private boolean isQueryIndexedUnder(IndexQuery query) {
		IBranchIndexer<E> branchIndexer = this.branchIndexer.getChildIndexer();
		while (null != branchIndexer) {
			if (null != branchIndexer.getKeys(query)) {
				return true;
			}
			branchIndexer = branchIndexer.getChildIndexer();
		}
		return false;
	}

}
