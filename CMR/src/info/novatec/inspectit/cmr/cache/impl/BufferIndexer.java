package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;

/**
 * Thread that invokes the {@link IBuffer#indexNext()} method constantly.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferIndexer extends BufferWorker {

	/**
	 * Default constructor. Just calls super class constructor.
	 * 
	 * @param buffer
	 *            Buffer to work on.
	 */
	public BufferIndexer(IBuffer<?> buffer) {
		super(buffer);
		setPriority(NORM_PRIORITY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void work() throws InterruptedException {
		getBuffer().indexNext();
	}

}
