package info.novatec.novaspy.agent.buffer.impl;

import info.novatec.novaspy.agent.buffer.IBufferStrategy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This implementation will hold all list of measurements for the given size. It
 * works as a FILO stack.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SizeBufferStrategy implements IBufferStrategy {

	/**
	 * The default count if none is specified.
	 */
	private static final int DEFAULT_COUNT = 60;

	/**
	 * The linked list containing the FILO stack.
	 */
	private LinkedList stack;

	/**
	 * The stack size.
	 */
	private int size;

	/**
	 * Delegates to the second constructor with the default count.
	 */
	public SizeBufferStrategy() {
		this(DEFAULT_COUNT);
	}

	/**
	 * The second constructor where one can specify the actual count or stack
	 * size.
	 * 
	 * @param size
	 *            The stack size.
	 */
	public SizeBufferStrategy(int size) {
		this.size = size;
		stack = new LinkedList();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMeasurements(List measurements) {
		if (null == measurements) {
			throw new IllegalArgumentException("Measurements cannot be null!");
		}

		// as we can only add one element at the time, we only have to delete
		// the oldest element.
		if (stack.size() >= size) {
			stack.removeFirst();
		}

		stack.addLast(measurements);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object next() {
		return stack.getLast();
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		stack.removeLast();
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map settings) {
		if (settings.containsKey("size")) {
			this.size = Integer.parseInt((String) settings.get("size"));
		}
	}

}
