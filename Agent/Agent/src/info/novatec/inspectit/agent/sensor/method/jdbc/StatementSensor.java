package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

/**
 * The SQL timer sensor which initializes and returns the {@link StatementHook} class.
 * 
 * @author Christian Herzog
 * 
 */
public class StatementSensor implements IMethodSensor {

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The used statement hook.
	 */
	private StatementHook statementHook = null;

	/**
	 * The default constructor which needs 3 parameter for initialization.
	 * 
	 * @param timer
	 *            The timer used for accurate measuring.
	 * @param idManager
	 *            The ID manager.
	 */
	public StatementSensor(Timer timer, IIdManager idManager) {
		this.timer = timer;
		this.idManager = idManager;
	}

	/**
	 * Returns the method hook.
	 * 
	 * @return The method hook.
	 */
	public IHook getHook() {
		return statementHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		statementHook = new StatementHook(timer, idManager, parameter);
	}

}
