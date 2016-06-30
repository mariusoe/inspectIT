/**
 *
 */
package rocks.inspectit.server.alearting;

import rocks.inspectit.server.alearting.adapter.IAlertAdapter;
import rocks.inspectit.server.alearting.adapter.impl.EmailAlertingAdapter;
import rocks.inspectit.server.alearting.adapter.impl.GitterAlertingAdapter;

/**
 * Factory to create instances of {@link IAlertAdapter} implementations.
 *
 * @author Marius Oehler
 *
 */
public abstract class AlertingAdapterFactory {

	/**
	 * Returns an instance of the {@link GitterAlertingAdapter}.
	 *
	 * @return the Gitter adapter instance
	 */
	public abstract IAlertAdapter getGitterAdapter();

	/**
	 * Returns an instance of the {@link EmailAlertingAdapter}.
	 *
	 * @return the email adapter instance
	 */
	public abstract IAlertAdapter getEmailAdapter();

}
