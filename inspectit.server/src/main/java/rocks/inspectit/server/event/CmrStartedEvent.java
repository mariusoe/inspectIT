package rocks.inspectit.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event for signaling that the CMR has been successfully started.
 *
 * @author Marius Oehler
 *
 */
public class CmrStartedEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8058085700816332326L;

	/**
	 * Constructor.
	 *
	 */
	public CmrStartedEvent() {
		super(new Object());
	}
}
