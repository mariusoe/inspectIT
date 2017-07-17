package rocks.inspectit.shared.all.communication.message;

/**
 * @author Marius Oehler
 *
 */
public class ThreadDumpRequestMessage implements IAgentMessage<Void> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void getMessageContent() {
		// not needed
		return null;
	}
}
