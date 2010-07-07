package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
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
	private final ThreadLocalStack timeStack = new ThreadLocalStack();

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
	private ThreadLocal threadLast = new ThreadLocal();

	/**
	 * The only constructor which needs the {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param statementStorage
	 *            The statement storage.
	 */
	public PreparedStatementHook(Timer timer, IIdManager idManager, StatementStorage statementStorage) {
		this.timer = timer;
		this.idManager = idManager;
		this.statementStorage = statementStorage;
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
		double endTime = ((Double) timeStack.pop()).doubleValue();
		double startTime = ((Double) timeStack.pop()).doubleValue();

		if (((Boolean) threadLast.get()).booleanValue()) {
			threadLast.set(Boolean.FALSE);

			String sql = statementStorage.getPreparedStatement(object);
			if (null != sql) {
				double duration = endTime - startTime;
				SqlStatementData sqlData = (SqlStatementData) coreService.getMethodSensorData(sensorTypeId, methodId, sql);
				if (null == sqlData) {
					try {
						Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
						long platformId = idManager.getPlatformId();
						long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
						long registeredMethodId = idManager.getRegisteredMethodId(methodId);

						sqlData = new SqlStatementData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
						sqlData.setPreparedStatement(true);
						sqlData.setSql(sql);
						sqlData.setDuration(duration);
						sqlData.setMin(duration);
						sqlData.setMax(duration);
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

					if (duration < sqlData.getMin()) {
						sqlData.setMin(duration);
					}

					if (duration > sqlData.getMax()) {
						sqlData.setMax(duration);
					}
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
			// it is possible that this exception is thrown in a 'normal' way,
			// as everyone could instantiate a prepared statement object without
			// calling first a method on the connection (prepareStatement...)
			LOGGER.info("Could not add prepared statement, no sql available! Method ID(local): " + methodId);
			LOGGER.info("This is not an inspectIT issue, please consult the management of inspectIT and send the following stacktrace!");
			// Furthermore, we will print the stack trace as it can indicate if
			// there is something wrong with the instrumentation or not. This is
			// because a prepareStatement(...) call must be seen in this stack!
			e.printStackTrace();
		}
	}

}
