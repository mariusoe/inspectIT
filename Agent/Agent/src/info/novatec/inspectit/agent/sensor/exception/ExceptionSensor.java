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
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The used exception sensor hook.
	 */
	private ExceptionSensorHook exceptionSensorHook = null;

	/**
	 * The default constructor which needs 3 parameter for initialization.
	 * 
	 * @param idManager
	 *            The ID manager.
	 */
	public ExceptionSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.idManager = idManager;
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
		exceptionSensorHook = new ExceptionSensorHook(idManager, parameter);
	}

}
