package info.novatec.inspectit.agent.sensor.method.http;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * The http sensor which initializes and returns the {@link HttpHook} class.
 * 
 * @author Stefan Siegl
 */
public class HttpSensor implements IMethodSensor {

	/**
	 * The hook.
	 */
	private HttpHook hook = null;

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * Constructor
	 * 
	 * @param timer
	 *            the timer.
	 * @param idManager
	 *            the idmanager.
	 */
	public HttpSensor(Timer timer, IIdManager idManager) {
		this.timer = timer;
		this.idManager = idManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(@SuppressWarnings("rawtypes") Map parameters) {
		hook = new HttpHook(timer, idManager, parameters, ManagementFactory.getThreadMXBean());
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}
}