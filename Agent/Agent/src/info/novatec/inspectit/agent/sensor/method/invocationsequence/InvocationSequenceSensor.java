package info.novatec.inspectit.agent.sensor.method.invocationsequence;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

/**
 * The invocation sequence sensor which initializes and returns the
 * {@link InvocationSequenceHook} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocationSequenceSensor implements IMethodSensor {

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
	 * The invocation sequence hook.
	 */
	private InvocationSequenceHook invocationSequenceHook = null;

	/**
	 * The default constructor which needs 2 parameter for initialization.
	 * 
	 * @param timer
	 *            The timer used for accurate measuring.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public InvocationSequenceSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return invocationSequenceHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map parameter) {
		invocationSequenceHook = new InvocationSequenceHook(timer, idManager, propertyAccessor, parameter);
	}

}
