package info.novatec.novaspy.agent.sensor.method.timer;

import info.novatec.novaspy.agent.config.IPropertyAccessor;
import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.core.ICoreService;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IdNotAvailableException;
import info.novatec.novaspy.agent.hooking.IConstructorHook;
import info.novatec.novaspy.agent.hooking.IMethodHook;
import info.novatec.novaspy.agent.sensor.method.averagetimer.AverageTimerHook;
import info.novatec.novaspy.communication.data.ParameterContentData;
import info.novatec.novaspy.util.ThreadLocalStack;
import info.novatec.novaspy.util.Timer;

import java.lang.management.ThreadMXBean;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The hook implementation for the timer sensor. It uses the
 * {@link ThreadLocalStack} class to save the time when the method was called.
 * <p>
 * The difference to the {@link AverageTimerHook} is that it's using
 * {@link ITimerStorage} objects to save the values. The {@link ITimerStorage}
 * is responsible for the actual data saving, so different strategies can be
 * chosen from (set through the configuration file).
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimerHook implements IMethodHook, IConstructorHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TimerHook.class.getName());

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
	 * The property accessor.
	 */
	private final IPropertyAccessor propertyAccessor;

	/**
	 * The timer storage factory which returns a new {@link ITimerStorage}
	 * object every time we request one. The returned storage depends on the
	 * settings in the configuration file.
	 */
	private final TimerStorageFactory timerStorageFactory = TimerStorageFactory.getFactory();

	/** DOWN BELOW THE ATTRIBUTES USED BY THE AGENT 5.0 */
	/** =============================================== */

	/**
	 * The thread MX bean.
	 */
	private ThreadMXBean threadMXBean;

	/**
	 * Defines if the thread CPU time is supported.
	 */
	private boolean supported = false;

	/**
	 * Defines if the thread CPU time is enabled.
	 */
	private boolean enabled = false;

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack threadCpuTimeStack = new ThreadLocalStack();

	/**
	 * The only constructor which needs the used {@link ICoreService}
	 * implementation and the used {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param param
	 *            Additional parameters passed to the
	 *            {@link TimerStorageFactory} for proper initialization.
	 */
	public TimerHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor, Map<String, String> param, ThreadMXBean threadMXBean) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
		this.threadMXBean = threadMXBean;

		try {
			// if it is even supported by this JVM
			supported = threadMXBean.isThreadCpuTimeSupported();
			if (supported) {
				// check if its enabled
				enabled = threadMXBean.isThreadCpuTimeEnabled();
				if (!enabled) {
					// try to enable it
					threadMXBean.setThreadCpuTimeEnabled(true);
					// check again now if it is enabled now
					enabled = threadMXBean.isThreadCpuTimeEnabled();
				}
			}
		} catch (RuntimeException e) {
			// catching the runtime exceptions which could be thrown by the
			// above statements.
			e.printStackTrace();
		}

		timerStorageFactory.setParameters(param);
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(new Long(threadMXBean.getCurrentThreadCpuTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(new Long(threadMXBean.getCurrentThreadCpuTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		double endTime = ((Double) timeStack.pop()).doubleValue();
		double startTime = ((Double) timeStack.pop()).doubleValue();
		double duration = endTime - startTime;

		// default setting to a negative number
		double cpuDuration = -1.0d;
		if (enabled) {
			long cpuEndTime = ((Long) threadCpuTimeStack.pop()).longValue();
			long cpuStartTime = ((Long) threadCpuTimeStack.pop()).longValue();
			cpuDuration = (cpuEndTime - cpuStartTime) / 1000000.0d;
		}

		List<ParameterContentData> parameterContentData = null;
		String prefix = null;
		// check if some properties need to be accessed and saved
		if (rsc.isPropertyAccess()) {
			parameterContentData = propertyAccessor.getParameterContentData(rsc.getPropertyAccessorList(), object, parameters);
			prefix = parameterContentData.toString();
		}

		ITimerStorage storage = (ITimerStorage) coreService.getObjectStorage(sensorTypeId, methodId, prefix);

		if (null == storage) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(methodId);

				Timestamp timestamp = new Timestamp(System.currentTimeMillis());

				storage = timerStorageFactory.newStorage(timestamp, platformId, registeredSensorTypeId, registeredMethodId, parameterContentData);
				storage.addData(duration, cpuDuration);

				coreService.addObjectStorage(sensorTypeId, methodId, prefix, storage);
			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not save the timer data because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			storage.addData(duration, cpuDuration);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(new Long(threadMXBean.getCurrentThreadCpuTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(new Long(threadMXBean.getCurrentThreadCpuTime()));
		}
		// just call the second after body method directly
		secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, null, rsc);
	}

}
