package info.novatec.novaspy.agent.sensor.method.jdbc;

import info.novatec.novaspy.agent.config.IPropertyAccessor;
import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.core.ICoreService;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IdNotAvailableException;
import info.novatec.novaspy.agent.core.impl.CoreService;
import info.novatec.novaspy.agent.hooking.IMethodHook;
import info.novatec.novaspy.communication.data.SqlStatementData;
import info.novatec.novaspy.util.ThreadLocalStack;
import info.novatec.novaspy.util.Timer;

import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The hook implementation for the statement sensor. It uses the
 * {@link ThreadLocalStack} class to know if some execute methods call each
 * other which would result in multiple data objects for only one query. After
 * the complete SQL method was executed, it computes how long the method took to
 * finish and saves the executed SQL Statement String. Afterwards, the
 * measurement is added to the {@link CoreService}.
 * 
 * @author Christian Herzog
 * @author Patrice Bouillet
 * 
 */
public class StatementHook implements IMethodHook {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(StatementHook.class.getName());

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
	 * The ThreadLocal for a boolean value so only the last before and first
	 * after hook of an invocation is measured.
	 */
	private ThreadLocal threadLast = new ThreadLocal();

	/**
	 * The only constructor which needs the {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public StatementHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
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

			double duration = endTime - startTime;
			String sql = parameters[0].toString();
			SqlStatementData sqlData = (SqlStatementData) coreService.getMethodSensorData(sensorTypeId, methodId, sql);

			if (null == sqlData) {
				try {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					long platformId = idManager.getPlatformId();
					long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
					long registeredMethodId = idManager.getRegisteredMethodId(methodId);

					sqlData = new SqlStatementData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
					sqlData.setPreparedStatement(false);
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
		}
	}

}
