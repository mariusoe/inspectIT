package info.novatec.novaspy.agent.sending.impl;

import info.novatec.novaspy.agent.core.ListListener;
import info.novatec.novaspy.agent.sending.AbstractSendingStrategy;

import java.util.List;
import java.util.Map;


/**
 * A simple implementation which checks the size of the list of the current
 * value objects. If the size of the list is greater than the defined one,
 * {@link #sendNow()} is called.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ListSizeStrategy extends AbstractSendingStrategy implements ListListener {

	/**
	 * Default size.
	 */
	private static final long DEFAULT_SIZE = 10L;

	/**
	 * The size.
	 */
	private long size = DEFAULT_SIZE;

	/**
	 * {@inheritDoc}
	 */
	public void startStrategy() {
		getCoreService().addListListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		getCoreService().removeListListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contentChanged(List list) {
		if (list.size() > size) {
			sendNow();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map settings) {
		this.size = Long.parseLong((String) settings.get("size"));
	}

}
