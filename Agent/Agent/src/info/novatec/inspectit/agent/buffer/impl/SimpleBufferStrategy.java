package info.novatec.inspectit.agent.buffer.impl;

import info.novatec.inspectit.agent.buffer.IBufferStrategy;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The simplest version of a buffer strategy contains just the reference to one
 * measurement list. Every time a new one is added, the old one is thrown away.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SimpleBufferStrategy implements IBufferStrategy {

	/**
	 * Stores the reference to the last given measurements.
	 */
	private List measurements;

	/**
	 * True if measurements were added and available.
	 */
	private boolean newMeasurements = false;

	/**
	 * {@inheritDoc}
	 */
	public final void addMeasurements(final List measurements) {
		if (null == measurements) {
			throw new IllegalArgumentException("Measurements cannot be null!");
		}

		this.measurements = measurements;
		newMeasurements = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean hasNext() {
		return newMeasurements;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object next() {
		if (newMeasurements) {
			newMeasurements = false;
			return measurements;
		}

		throw new NoSuchElementException();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void remove() {
		measurements = null;
		newMeasurements = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void init(final Map settings) {
		// nothing to do
	}

}
