package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.IndexQuery;
import info.novatec.inspectit.communication.DefaultData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Leaf class is the one that holds the weak references to objects, thus last in tree structure.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Element type that the leaf can index (and hold).
 */
public class Leaf<E extends DefaultData> implements ITreeComponent<E> {

	/**
	 * Map for week references.
	 */
	private Map<Long, WeakReference<E>> map;

	/**
	 * Default constructor.
	 */
	public Leaf() {
		super();
		map = new ConcurrentHashMap<Long, WeakReference<E>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(E element) {
		WeakReference<E> weakReference = new WeakReference<E>(element);
		map.put(element.getId(), weakReference);
	}

	/**
	 * {@inheritDoc}
	 */
	public E get(E template) {
		long id = template.getId();
		WeakReference<E> weakReference = map.get(id);
		if (null != weakReference) {
			if (null == weakReference.get()) {
				map.remove(id);
				return null;
			}
			return weakReference.get();
		} else {
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public E getAndRemove(E template) {
		long id = template.getId();
		WeakReference<E> weakReference = map.get(id);
		if (null != weakReference) {
			if (null == weakReference.get()) {
				map.remove(id);
				return null;
			} else {
				E result = weakReference.get();
				map.remove(id);
				return result;
			}
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<E> query(IndexQuery query) {
		List<E> results = new ArrayList<E>();
		Iterator<WeakReference<E>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			WeakReference<E> weakReference = iterator.next();
			if (null != weakReference) {
				E element = weakReference.get();
				if (null != element && element.isQueryComplied(query)) {
					results.add(weakReference.get());
				}
			}
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public long getComponentSize(IObjectSizes objectSizes) {
		long size = objectSizes.getSizeOfObject();
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size += objectSizes.getSizeOf((ConcurrentHashMap) map);
		size += map.size() * (16 + 16); // for Long and WeekReference in a Map.Entry
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean clean() {
		List<Long> toClean = new ArrayList<Long>();
		for (Map.Entry<Long, WeakReference<E>> entry : map.entrySet()) {
			if (null == entry.getValue() || null == entry.getValue().get()) {
				toClean.add(entry.getKey());
			}
		}
		for (Object key : toClean) {
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
		return map.size();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearAll() {
		map.clear();
	}

}
