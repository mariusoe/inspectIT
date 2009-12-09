package info.novatec.inspectit.util;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * This list is used to store elements as weak references. Especially useful to
 * save a list of {@link ClassLoader} objects in a JEE environment. Objects are
 * added and removed as in any other list, but can turn to become null. Calling
 * {@link #removeAllNullElements()} is the only way to get rid of the garbage
 * collected elements. {@link #getHardReferences()} returns an {@link ArrayList}
 * suppressing all the references in this list which are already removed by the
 * garbage collector.
 * 
 * @author Patrice Bouillet
 * 
 */
public class WeakList extends AbstractList {

	/**
	 * Stores the weak references to the object.
	 */
	private List refs = new ArrayList();

	/**
	 * Returns the hard reference.
	 * 
	 * @param o
	 *            The object.
	 * @return The reference to the object.
	 */
	private Object getHardReference(Object o) {
		if (null != o && o instanceof WeakReference) {
			return ((WeakReference) o).get();
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(int index) {
		return getHardReference(refs.get(index));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(Object o) {
		return refs.add(new WeakReference(o));
	}

	/**
	 * {@inheritDoc}
	 */
	public void add(int index, Object o) {
		refs.add(index, new WeakReference(o));
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		refs.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object remove(int index) {
		return getHardReference(refs.remove(index));
	}

	/**
	 * {@inheritDoc}
	 */
	public Object set(int index, Object element) {
		return getHardReference(refs.set(index, new WeakReference(element)));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object o) {
		for (int i = 0; i < size(); i++) {
			if (o == get(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return refs.size();
	}

	/**
	 * Returns a list of hard references. Skips all weak references but doesn't
	 * delete them if there are any. The list returned (especially the indices)
	 * aren't the same as the ones from this list.
	 * 
	 * @return An {@link ArrayList} containing all the hard references of this
	 *         weak list.
	 */
	public List getHardReferences() {
		List result = new ArrayList();

		for (int i = 0; i < size(); i++) {
			Object tmp = get(i);

			if (null != tmp) {
				result.add(tmp);
			}
		}

		return result;
	}

	/**
	 * Calling this method removes all the garbage collected elements in this
	 * list which appear to be null now.
	 */
	public void removeAllNullElements() {
		for (int i = size(); i-- > 0;) {
			if (get(i) == null) {
				remove(i);
			}
		}
	}

}
