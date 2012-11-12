package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.util.ThreadLocalStack;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores the mapping between statements and objects so that these statements are later accessible.
 * 
 * @author Patrice Bouillet
 * 
 */
public class StatementStorage {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(StatementStorage.class.getName());

	/**
	 * The size of the prepared statement map.
	 */
	private static final int MAP_SIZE = 50;

	/**
	 * The statement map contains a mapping between the prepared statement objects and their created
	 * query string with parameter placeholders in it.
	 */
	private Map<Object, String> preparedStatementMap = new WeakHashMap<Object, String>(MAP_SIZE);

	/**
	 * Stores the parameters of prepared statements.
	 */
	private Map<Object, String[]> parameterMap = new WeakHashMap<Object, String[]>(MAP_SIZE);

	/**
	 * Returns the sql thread local stack.
	 */
	private ThreadLocalStack<String> sqlThreadLocalStack = new ThreadLocalStack<String>();

	/**
	 * Adds a prepared statement to this storage for later retrieval.
	 * 
	 * @param object
	 *            The object which will be the key of the mapping.
	 */
	public void addPreparedStatement(Object object) {
		String sql = sqlThreadLocalStack.getLast();

		synchronized (this) {
			preparedStatementMap.put(object, sql);

			char[] sqlChars = sql.toCharArray();
			int count = 0;
			for (int i = 0; i < sqlChars.length; i++) {
				char c = sqlChars[i];
				if ('?' == c) {
					count = count + 1;
				}
			}

			parameterMap.put(object, new String[count]);
		}

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("Recorded preparded sql statement: " + sql);
		}
	}

	/**
	 * Returns a stored sql string for this object.
	 * 
	 * @param object
	 *            The object which will be used to look up in the map.
	 * @return The sql string.
	 */
	protected String getPreparedStatement(Object object) {
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("Return preparded sql statement: " + preparedStatementMap.get(object));
		}

		return preparedStatementMap.get(object);
	}

	/**
	 * Returns a stored parameters for the object.
	 * 
	 * @param object
	 *            The object which will be used to look up in the map.
	 * @return The list of parameters.
	 */
	protected List<String> getParameters(Object object) {
		String[] params = parameterMap.get(object);
		if (null != params) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("Return preparded sql statement parameters: " + params);
			}

			List<String> paramList = new ArrayList<String>(params.length);
			for (String param : params) {
				paramList.add(param);
			}
			return paramList;
		} else {
			return null;
		}
	}

	/**
	 * Adds a parameter to a specific prepared statement.
	 * 
	 * @param preparedStatement
	 *            The prepared statement object.
	 * @param index
	 *            The index of the value.
	 * @param value
	 *            The value to be inserted.
	 */
	protected void addParameter(Object preparedStatement, int index, Object value) {
		String[] parameters = parameterMap.get(preparedStatement);

		if (null != parameters) {
			if (parameters.length <= index) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.warning("Trying to set the parameter with value " + value + " at index " + index + ", but the prepared statement did not have this parameter.");
				}
				return;
			}

			if (null != value) {
				if (value instanceof String || value instanceof Date || value instanceof Time || value instanceof Timestamp) {
					parameters[index] = "'" + value.toString() + "'";
				} else {
					parameters[index] = value.toString();
				}
			} else {
				value = "[null]";
				parameters[index] = (String) value;
			}

			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.finer("Prepared Statement :: Added value:" + value.toString() + " with index:" + index + " to prepared statement:" + preparedStatement);
			}
		} else {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Could not get the prepared statement from the cache to add a parameter! Prepared Statement:" + preparedStatement + " index:" + index + " value:" + value);
			}
		}
	}

	/**
	 * Clears all the parameters in the array.
	 * 
	 * @param preparedStatement
	 *            The prepared statement for which all parameters are going to be cleared.
	 */
	protected void clearParameters(Object preparedStatement) {
		if (parameterMap.containsKey(preparedStatement)) {
			String[] parameters = parameterMap.get(preparedStatement);
			parameterMap.put(preparedStatement, new String[parameters.length]);
		} else {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Could not get the prepared statement from the cache to clear the parameters! Prepared Statement:" + preparedStatement);
			}
		}
	}

	/**
	 * This method adds an SQL String to the current thread local stack. This is needed so that
	 * created prepared statements can be associated to the SQL Strings.
	 * <p>
	 * So if three times the prepared statement method is called with the same string, the stack
	 * contains the string three times. Now the Prepared Statement is created which results in
	 * calling the {@link #addPreparedStatement(Object, String)} method. The last added String is
	 * taken and associated with the object.
	 * 
	 * @param sql
	 *            The SQL String.
	 */
	protected void addSql(String sql) {
		sqlThreadLocalStack.push(sql);
	}

	/**
	 * Removes the last added sql from the thread local stack. We don't need the String object here.
	 */
	protected void removeSql() {
		sqlThreadLocalStack.pop();
	}

}
