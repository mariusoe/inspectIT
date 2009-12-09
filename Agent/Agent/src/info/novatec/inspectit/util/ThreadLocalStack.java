package info.novatec.inspectit.util;

import java.util.LinkedList;

/**
 * The ThreadLocalStack class extends {@link ThreadLocal} to have a
 * {@link LinkedList} to be used as a stack. Extending {@link ThreadLocal} and
 * using the {@link #initialValue()} method helps to keep the variable private
 * to the actual {@link Thread}. A {@link LinkedList} is used because it can be
 * easily used as a FIFO stack.
 * 
 * @author Patrice Bouillet
 */
public class ThreadLocalStack extends ThreadLocal {

	/**
	 * {@inheritDoc}
	 */
	public Object initialValue() {
		return new LinkedList();
	}

	/**
	 * Pushes the specified value onto the stack.
	 * 
	 * @param value
	 *            the value to push onto the stack.
	 */
	public void push(Object value) {
		((LinkedList) super.get()).addLast(value);
	}

	/**
	 * Returns the last pushed value.
	 * 
	 * @return The last pushed value.
	 */
	public Object pop() {
		return ((LinkedList) super.get()).removeLast();
	}

	/**
	 * Returns the last pushed value without removing it.
	 * 
	 * @return The last pushed value.
	 */
	public Object getLast() {
		return ((LinkedList) super.get()).getLast();
	}

	/**
	 * Returns the first value pushed onto the stack.
	 * 
	 * @return The first value pushed onto the stack.
	 */
	public Object getAndRemoveFirst() {
		return ((LinkedList) super.get()).removeFirst();
	}

}
