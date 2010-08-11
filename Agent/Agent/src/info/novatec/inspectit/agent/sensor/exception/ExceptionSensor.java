package info.novatec.inspectit.agent.sensor.exception;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

/**
 * The {@link ExceptionSensor} which initializes and returns the {@link ExceptionSensorHook} class.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionSensor implements IExceptionSensor {

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
	 * The used exception sensor hook.
	 */
	private static ExceptionSensorHook exceptionSensorHook = null;

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
	public ExceptionSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return exceptionSensorHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map parameter) {
		// currently we need only the idManager.
		exceptionSensorHook = new ExceptionSensorHook(idManager);
	}

}
