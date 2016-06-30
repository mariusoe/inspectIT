/**
 *
 */
package rocks.inspectit.server.alearting.adapter;

/**
 * Interface for alerting adapter.
 *
 * @author Marius Oehler
 *
 */
public interface IAlertAdapter {

	/**
	 * Connects the adapter.
	 *
	 * @return Returns true if the adapter has been successfully connected.
	 */
	boolean connect();

	/**
	 * Sends the given message.
	 *
	 * @param message
	 *            the message to send
	 */
	void sendMessage(String message);

}
