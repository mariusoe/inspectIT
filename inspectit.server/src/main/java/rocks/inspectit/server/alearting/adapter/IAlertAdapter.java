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
	 * Sends the given message if the adapter is enabled.
	 *
	 * @param message
	 *            the message to send
	 */
	void sendMessage(String message);
}
