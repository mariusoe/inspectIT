package info.novatec.inspectit.storage.nio.write;


import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.nio.WriteReadAttachment;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;

import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;

/**
 * Completion handler for asynchronous writing.
 *
 * @author Ivan Senic
 *
 */
public class WritingCompletionHandler implements CompletionHandler<Integer, WriteReadAttachment> {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * {@inheritDoc}
	 * <p>
	 * On writing completion the check if the whole content of the buffer has been written is done.
	 * If not, a new write with updated position and size is performed. If yes, the completion
	 * handler is invoke if it is not null.
	 */
	public void completed(Integer result, WriteReadAttachment attachment) {
		long bytesToWriteMore = attachment.getSize() - result.longValue();
		if (bytesToWriteMore > 0) {
			long writingSize = bytesToWriteMore;
			long writingPosition = attachment.getPosition() + result.longValue();

			attachment.setPosition(writingPosition);
			attachment.setSize(writingSize);

			attachment.getFileChannel().write(attachment.getByteBuffer(), writingPosition, attachment, this);
		} else {
			WriteReadCompletionRunnable completionRunnable = attachment.getCompletionRunnable();
			if (null != completionRunnable) {
				completionRunnable.setCompleted(true);
				completionRunnable.run();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void failed(Throwable exc, WriteReadAttachment attachment) {
		log.error("Write to the disk failed.", exc);
		WriteReadCompletionRunnable completionRunnable = attachment.getCompletionRunnable();
		if (null != completionRunnable) {
			completionRunnable.setCompleted(false);
			completionRunnable.run();
		}
	}

}
