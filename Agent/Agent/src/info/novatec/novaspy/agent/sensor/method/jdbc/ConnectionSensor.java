package info.novatec.novaspy.agent.sensor.method.jdbc;

import info.novatec.novaspy.agent.hooking.IHook;
import info.novatec.novaspy.agent.sensor.method.IMethodSensor;

import java.sql.PreparedStatement;
import java.util.Map;

/**
 * This sensor initializes the {@link ConnectionHook} to intercept the creation
 * of {@link PreparedStatement} classes.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ConnectionSensor implements IMethodSensor {

	/**
	 * The statement storage.
	 */
	private StatementStorage statementStorage;

	/**
	 * The used prepared statement hook.
	 */
	private ConnectionHook connectionHook = null;

	/**
	 * The default constructor which needs one parameter for initialization.
	 * 
	 * @param statementStorage
	 *            The statement storage.
	 */
	public ConnectionSensor(StatementStorage statementStorage) {
		this.statementStorage = statementStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return connectionHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map parameter) {
		connectionHook = new ConnectionHook(statementStorage);
	}

}
