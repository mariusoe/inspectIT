package info.novatec.inspectit.storage.nio;

import info.novatec.inspectit.storage.nio.read.ReadingCompletionHandler;
import info.novatec.inspectit.storage.nio.write.WritingCompletionHandler;

/**
 * Completion runnable that know if the IO operation was successful.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class WriteReadCompletionRunnable implements Runnable {

	/**
	 * Denotes if the write/read operation was completed successfully. Note that this will always be
	 * correctly set before {@link #run()} is executed on the {@link WritingCompletionHandler} or
	 * {@link ReadingCompletionHandler}, thus it can be used in run to determine if the IO operation
	 * failed.
	 */
	private boolean completed;

	/**
	 * @return the completed
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @param completed
	 *            the completed to set
	 */
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

}
