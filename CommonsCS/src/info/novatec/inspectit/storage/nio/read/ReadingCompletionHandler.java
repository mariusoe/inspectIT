package info.novatec.inspectit.storage.nio.read;

import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.nio.WriteReadAttachment;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;

import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;

/**
 * Completion handler for asynchronous reading.
 *
 * @author Ivan Senic
 *
 */
public class ReadingCompletionHandler implements CompletionHandler<Integer, WriteReadAttachment> {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * {@inheritDoc}
	 * <p>
	 * On reading completion the check if the wanted reading size has been read. If not, a new read
	 * with updated position and size is performed. If yes, the completion runnable is invoke if it
	 * is not null and buffer is flip.
	 */
	public void completed(Integer result, WriteReadAttachment attachment) {
		long bytesToReadMore = attachment.getSize() - result.longValue();
		if (bytesToReadMore > 0) {
			long readSize = bytesToReadMore;
			long position = attachment.getPosition() + result.longValue();

			attachment.setSize(readSize);
			attachment.setPosition(position);

			attachment.getFileChannel().read(attachment.getByteBuffer(), position, attachment, this);
		} else {
			attachment.getByteBuffer().flip();
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
