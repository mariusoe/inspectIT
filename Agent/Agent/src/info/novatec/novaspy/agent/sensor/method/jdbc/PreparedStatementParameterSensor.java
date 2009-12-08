package info.novatec.novaspy.agent.sensor.method.jdbc;

import info.novatec.novaspy.agent.hooking.IHook;
import info.novatec.novaspy.agent.sensor.method.IMethodSensor;

import java.util.Map;

/**
 * @author Patrice Bouillet
 * 
 */
public class PreparedStatementParameterSensor implements IMethodSensor {

	/**
	 * The statement storage.
	 */
	private StatementStorage statementStorage;

	/**
	 * The used prepared statement hook.
	 */
	private PreparedStatementParameterHook preparedStatementParameterHook = null;

	/**
	 * The default constructor which needs one parameter for initialization.
	 * 
	 * @param statementStorage
	 *            The statement storage.
	 */
	public PreparedStatementParameterSensor(StatementStorage statementStorage) {
		this.statementStorage = statementStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return preparedStatementParameterHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map parameter) {
		preparedStatementParameterHook = new PreparedStatementParameterHook(statementStorage);
	}

}
