package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.communication.DefaultData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * {@link Branch} is a {@link ITreeComponent} that holds references to other {@link ITreeComponent}
 * s, which are actually branch children.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Element type that the branch can index (and hold).
 */
public class Branch<E extends DefaultData> implements ITreeComponent<E> {
	
	/**
	 * Initial concurrency level for the {@link ConcurrentHashMap}.
	 */
	private static final int CONCURRENCY_LEVEL = 4;

	/**
	 * Branch indexer.
	 */
	private IBranchIndexer<E> branchIndexer;

	/**
	 * Map for holding references.
	 */
	private Map<Object, ITreeComponent<E>> map;

	/**
	 * Default constructor. {@link Branch} can only be initialized with proper branch indexer
	 * supplied. If null is passed, {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param branchIndexer 
	 */
	public Branch(IBranchIndexer<E> branchIndexer) {
		super();
		if (null == branchIndexer) {
			throw new IllegalArgumentException();
		}
		this.branchIndexer = branchIndexer;
		map = new ConcurrentHashMap<Object, ITreeComponent<E>>(1, 0.75f, CONCURRENCY_LEVEL);
	}

	/**
	 * Returns branch indexer.
	 * 
	 * @return Branch indexer
	 */
	protected IBranchIndexer<E> getBranchIndexer() {
		return branchIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(E element) throws IndexingException {
		// get key for object
		Object key = branchIndexer.getKey(element);
		if (null == key) {
			throw new IndexingException("Branch indexer " + branchIndexer + " can not create the key for the object " + element + ".");
		}
		// get the tree component for key
		ITreeComponent<E> treeComponent = map.get(key);
		if (null != treeComponent) {
			// if component exists, put element into the component
			treeComponent.put(element);
		} else {
			// otherwise create new tree component, add it to the map and put element into
			ITreeComponent<E> nextTreeComponent = branchIndexer.getNextTreeComponent();
			map.put(key, nextTreeComponent);
			nextTreeComponent.put(element);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public E get(E template) {
		// get key for template
		Object key = branchIndexer.getKey(template);
		// get the tree component for key
		ITreeComponent<E> treeComponent = map.get(key);
		if (null != treeComponent) {
			// if component exists, get element from the component
			return treeComponent.get(template);
		} else if (null == key) {
			// if key could not be created, search into all branches/leafs
			// keys can not be created when the template object does not carry the information that
			// branch indexer on this branch needs
			Iterator<ITreeComponent<E>> iterator = map.values().iterator();
			E result = null;
			while (iterator.hasNext()) {
				result = iterator.next().get(template);
				if (null != result) {
					return result;
				}
			}
			return null;
		} else {
			// finally return nothing cause temple object was never put before
			return null;
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public E getAndRemove(E template) {
		// get key for template
		Object key = getBranchIndexer().getKey(template);
		// get the tree component for key
		ITreeComponent<E> treeComponent = map.get(key);
		if (null != treeComponent) {
			// if component exists, get element from the component
			return treeComponent.getAndRemove(template);
		} else if (null == key) {
			// if key could not be created, search into all branches/leafs
			// keys can not be created when the template object does not carry the information that
			// branch indexer on this branch needs
			Iterator<ITreeComponent<E>> iterator = map.values().iterator();
			E result = null;
			while (iterator.hasNext()) {
				result = iterator.next().getAndRemove(template);
				if (null != result) {
					return result;
				}
			}
			return null;
		} else {
			// finally return nothing cause temple object was never put before
			return null;
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public List<E> query(IIndexQuery query) {
		// get all keys for query
		Object[] keys = getBranchIndexer().getKeys(query);
		if (null == keys) {
			// if key can not be created search in next level
			return queryAllTreeComponents(query);
		} else if (1 == keys.length) {
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

	/**
	 * Queries the single {@link ITreeComponent} that is mapped with key. If passed key is null, or
	 * if there is no component mapped with given key, result will be empty list.
	 * 
	 * @param query
	 *            Query to process.
	 * @param key
	 *            Mapping key value for {@link ITreeComponent}.
	 * @return Result from queried {@link ITreeComponent} or empty list if key is null or none of
	 *         {@link ITreeComponent} is mapped with given key.
	 */
	protected List<E> querySingleKey(IIndexQuery query, Object key) {
		// get tree component for key
		ITreeComponent<E> treeComponent = map.get(key);
		if (null != treeComponent) {
			// if it is found search in that one
			return treeComponent.query(query);
		} else {
			// finally this brunch did not find anything that matches the search
			return Collections.emptyList();
		}
	}

	/**
	 * Returns results from all branches for given query.
	 * 
	 * @param query
	 *            Query to process.
	 * @return Combined result from all {@link ITreeComponent}s that are mapped in this branch.
	 */
	protected List<E> queryAllTreeComponents(IIndexQuery query) {
		List<E> results = new ArrayList<E>();
		Iterator<ITreeComponent<E>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			List<E> componentResult = iterator.next().query(query);
			if (null != componentResult && !componentResult.isEmpty()) {
				results.addAll(componentResult);
			}
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getComponentSize(IObjectSizes objectSizes) {
		int mapSize = map.size();
		long size = objectSizes.getSizeOfObject();
		size += objectSizes.getPrimitiveTypesSize(2, 0, 0, 0, 0, 0);
		size += objectSizes.getSizeOfConcurrentHashMap(mapSize, CONCURRENCY_LEVEL);
		size += mapSize * objectSizes.getSizeOfLongObject(); // for a Long key in a Map.entry
		for (ITreeComponent<E> treeComponent : map.values()) {
			size += treeComponent.getComponentSize(objectSizes);
		}
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean clean() {
		ArrayList<Object> keysToRemove = new ArrayList<Object>();
		for (Map.Entry<Object, ITreeComponent<E>> entry : map.entrySet()) {
			boolean toClear = entry.getValue().clean();
			if (toClear) {
				keysToRemove.add(entry.getKey());
			}
		}
		for (Object key : keysToRemove) {
			map.remove(key);
		}

		if (map.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getNumberOfElements() {
		long sum = 0;
		for (ITreeComponent<E> treeComponent : map.values()) {
			sum += treeComponent.getNumberOfElements();
		}
		return sum;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAll() {
		map.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanWithRunnable(ExecutorService executorService) {
		for (Map.Entry<Object, ITreeComponent<E>> entry : map.entrySet()) {
			entry.getValue().cleanWithRunnable(executorService);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean clearEmptyComponents() {
		ArrayList<Object> keysToRemove = new ArrayList<Object>();
		for (Map.Entry<Object, ITreeComponent<E>> entry : map.entrySet()) {
			boolean toClear = entry.getValue().clearEmptyComponents();
			if (toClear) {
				keysToRemove.add(entry.getKey());
			}
		}
		for (Object key : keysToRemove) {
			map.remove(key);
		}

		if (map.isEmpty()) {
			return true;
		}
		return false;
	}

}
