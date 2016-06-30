/**
 *
 */
package rocks.inspectit.server.alearting;

import rocks.inspectit.server.alearting.adapter.IAlertAdapter;

/**
 * Factory to create instances of {@link IAlertAdapter} implementations.
 *
 * @author Marius Oehler
 *
 */
public abstract class AlertingAdapterFactory {

	/**
	 * {@inheritDoc}
	 */
	public abstract IAlertAdapter createGitterAdapter();

	/**
	 * {@inheritDoc}
	 */
	public abstract IAlertAdapter createEmailAdapter();

}
