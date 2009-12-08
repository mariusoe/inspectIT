package info.novatec.novaspy.agent.sensor.method.jdbc;

import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.hooking.IHook;
import info.novatec.novaspy.agent.sensor.method.IMethodSensor;
import info.novatec.novaspy.util.Timer;

import java.util.Map;

/**
 * @author Patrice Bouillet
 * 
 */
public class PreparedStatementSensor implements IMethodSensor {

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The statement storage.
	 */
	private final StatementStorage statementStorage;

	/**
	 * The used prepared statement hook.
	 */
	private PreparedStatementHook preparedStatementHook = null;

	/**
	 * The default constructor which needs 3 parameter for initialization.
	 * 
	 * @param timer
	 *            The timer used for accurate measuring.
	 * @param idManager
	 *            The ID manager.
	 * @param statementStorage
	 *            The statement storage.
	 */
	public PreparedStatementSensor(Timer timer, IIdManager idManager, StatementStorage statementStorage) {
		this.timer = timer;
		this.idManager = idManager;
		this.statementStorage = statementStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return preparedStatementHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map parameter) {
		preparedStatementHook = new PreparedStatementHook(timer, idManager, statementStorage);
	}

}
