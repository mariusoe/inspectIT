package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;

/**
 * Abstract class for all threads that work on the buffer. The work each worker performs is defined
 * in abstract method {@link #work()}.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class BufferWorker extends Thread {

	/**
	 * Buffer to work on.
	 */
	protected IBuffer<?> buffer;

	/**
	 * Default constructor. Thread is set to be a daemon, to have highest priority and started.
	 * 
	 * @param buffer
	 */
	public BufferWorker(IBuffer<?> buffer) {
		this.buffer = buffer;
		setDaemon(true);
		setPriority(MAX_PRIORITY);
	}

	/**
	 * Defines the work to be done on the buffer.
	 * 
	 * @throws InterruptedException
	 */
	public abstract void work() throws InterruptedException;

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void run() {
		while (true) {
			try {
				work();
			} catch (InterruptedException interruptedException) {
				// we should never be interrupted, because we don't use this mechanism.. if it
				// happens, we preserve evidence that the interruption happened for the code higher
				// up, that can figure it out if it wants..
				Thread.currentThread().interrupt();
			}
		}
	}
}
