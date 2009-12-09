package info.novatec.inspectit.agent.sensor.exception;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

/**
 * The {@link ExceptionTracingSensor} which initializes and returns the
 * {@link ExceptionTracingHook} class.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionTracingSensor implements IExceptionTracingSensor {

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
	 * The used exception tracing hook.
	 */
	private static ExceptionTracingHook exceptionTracingHook = null;

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
	public ExceptionTracingSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public static IExceptionTracingHook getHook() {
		// TODO ET: move this method into interface
		return exceptionTracingHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map parameter) {
		// currently we need only the idManager.
		exceptionTracingHook = new ExceptionTracingHook(idManager);
	}

}
