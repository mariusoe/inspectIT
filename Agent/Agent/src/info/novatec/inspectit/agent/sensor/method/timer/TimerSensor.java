package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

/**
 * The timer sensor which initializes and returns the {@link TimerHook} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimerSensor implements IMethodSensor {

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The property accessor.
	 */
	private final IPropertyAccessor propertyAccessor;

	/**
	 * The used timer hook.
	 */
	private TimerHook timerHook = null;

	/**
	 * The default constructor which needs 3 parameter for initialization.
	 * 
	 * @param timer
	 *            The timer used for accurate measuring.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public TimerSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return timerHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map parameter) {
		timerHook = new TimerHook(timer, idManager, propertyAccessor, parameter);
	}

}
