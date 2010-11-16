package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;

/**
 * Thread that invokes the {@link IBuffer#analyzeNext()} method constantly.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferAnalyzer extends BufferWorker {

	/**
	 * Default constructor. Just calls super class constructor.
	 * 
	 * @param buffer
	 *            Buffer to work on.
	 */
	public BufferAnalyzer(IBuffer<?> buffer) {
		super(buffer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void work() throws InterruptedException {
		buffer.analyzeNext();
	}
	
}
