package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.util.StringConstraint;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This hook is intended to intercept the created prepared statement calls to the database. To not
 * create duplicate calls, a {@link ThreadLocal} attribute is used to check if this specific
 * statement is already 'sent'.
 * <p>
 * Furthermore, a {@link StatementStorage} is used which saves all the created prepared statements
 * and if the parameter hook for the sqls is installed and activated, the parameters are replaced.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PreparedStatementHook implements IMethodHook, IConstructorHook {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(PreparedStatementHook.class.getName());

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

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
	 * The ThreadLocal for a boolean value so only the last before and first after hook of an
	 * invocation is measured.
	 */
	private ThreadLocal<Boolean> threadLast = new ThreadLocal<Boolean>();

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private StringConstraint strConstraint;

	/**
	 * Contains all methodidents of all prepared statements that had a problem finding the stored
	 * SQL statement. Using this structure we can ensure that we do not throw the exception always
	 * again.
	 */
	private static List<Long> preparedStatementsWithExceptions = new ArrayList<Long>(0);

	/**
	 * The only constructor which needs the {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param statementStorage
	 *            The statement storage.
	 * @param parameter
	 *            Additional parameters.
	 */
	public PreparedStatementHook(Timer timer, IIdManager idManager, StatementStorage statementStorage, Map<String, Object> parameter) {
		this.timer = timer;
		this.idManager = idManager;
		this.statementStorage = statementStorage;
		this.strConstraint = new StringConstraint(parameter);
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		threadLast.set(Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		double endTime = timeStack.pop().doubleValue();
		double startTime = timeStack.pop().doubleValue();

		if (threadLast.get().booleanValue()) {
			threadLast.set(Boolean.FALSE);

			String sql = statementStorage.getPreparedStatement(object);
			if (null != sql) {
				double duration = endTime - startTime;
				SqlStatementData sqlData = (SqlStatementData) coreService.getMethodSensorData(sensorTypeId, methodId, sql);
				if (null == sqlData) {
					try {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));
						long platformId = idManager.getPlatformId();
						long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
						long registeredMethodId = idManager.getRegisteredMethodId(methodId);

						sqlData = new SqlStatementData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
						sqlData.setPreparedStatement(true);
						sqlData.setSql(strConstraint.crop(sql));
						sqlData.setDuration(duration);
						sqlData.calculateMin(duration);
						sqlData.calculateMax(duration);
						sqlData.setCount(1L);
						coreService.addMethodSensorData(sensorTypeId, methodId, sql, sqlData);
					} catch (IdNotAvailableException e) {
						if (LOGGER.isLoggable(Level.FINER)) {
							LOGGER.finer("Could not save the sql data because of an unavailable id. " + e.getMessage());
						}
					}
				} else {
					sqlData.increaseCount();
					sqlData.addDuration(duration);

					sqlData.calculateMin(duration);
					sqlData.calculateMax(duration);
				}
			} else {
				// the sql was not found, we'll try again
				threadLast.set(Boolean.TRUE);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		try {
			statementStorage.addPreparedStatement(object);
		} catch (NoSuchElementException e) {
			// Ensure that a problem with this statement is only thrown once to not spam the log
			// file. It is possible that we hide exceptions.
			Long methodIdLong = Long.valueOf(methodId);
			if (preparedStatementsWithExceptions.contains(methodIdLong)) {
				// we already logged this exception...
				return;
			}

			// it is possible that this exception is thrown in a 'normal' way,
			// as everyone could instantiate a prepared statement object without
			// calling first a method on the connection (prepareStatement...)
			LOGGER.info("Could not add prepared statement, no sql available! Method ID(local): " + methodId);
			LOGGER.info("This is not an inspectIT issue, but you forget to integrate the Connection creating the SQL statement in the configuration, please consult the management of inspectIT and send the following stacktrace!");
			e.printStackTrace();

			// we need to ensure thread safety for the list and do not care for lost updates, so
			// we simply create a new list based on the old list and change references after we
			// finished building it.
			List<Long> clonedList = new ArrayList<Long>(preparedStatementsWithExceptions);
			clonedList.add(methodIdLong);
			preparedStatementsWithExceptions = clonedList;
		}
	}
}
