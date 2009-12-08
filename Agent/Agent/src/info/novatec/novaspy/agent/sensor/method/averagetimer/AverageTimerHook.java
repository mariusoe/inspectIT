package info.novatec.novaspy.agent.sensor.method.averagetimer;

import info.novatec.novaspy.agent.config.IPropertyAccessor;
import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.core.ICoreService;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IdNotAvailableException;
import info.novatec.novaspy.agent.core.impl.CoreService;
import info.novatec.novaspy.agent.hooking.IConstructorHook;
import info.novatec.novaspy.agent.hooking.IMethodHook;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.util.ThreadLocalStack;
import info.novatec.novaspy.util.Timer;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The hook implementation for the average timer sensor. It uses the
 * {@link ThreadLocalStack} class to save the time when the method was called.
 * After the complete original method was executed, it computes the how long the
 * method took to finish. Afterwards, the measurement is added to the
 * {@link CoreService}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class AverageTimerHook implements IMethodHook, IConstructorHook {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AverageTimerHook.class.getName());

	/**
	 * The stack containing the start time values.
	 */
	private ThreadLocalStack timeStack = new ThreadLocalStack();

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
	 * The only constructor which needs the {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public AverageTimerHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
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
		double duration = endTime - startTime;

		List parameterContentData = null;
		String prefix = null;
		// check if some properties need to be accessed and saved
		if (rsc.isPropertyAccess()) {
			parameterContentData = propertyAccessor.getParameterContentData(rsc.getPropertyAccessorList(), object, parameters);
			prefix = parameterContentData.toString();
		}

		TimerData timerData = (TimerData) coreService.getMethodSensorData(sensorTypeId, methodId, prefix);

		if (null == timerData) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(methodId);

				Timestamp timestamp = new Timestamp(System.currentTimeMillis());

				timerData = new TimerData(timestamp, platformId, registeredSensorTypeId, registeredMethodId, parameterContentData);
				timerData.increaseCount();
				timerData.addDuration(duration);
				timerData.setMin(duration);
				timerData.setMax(duration);

				coreService.addMethodSensorData(sensorTypeId, methodId, prefix, timerData);
			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not save the average timer data because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			timerData.increaseCount();
			timerData.addDuration(duration);

			if (duration < timerData.getMin()) {
				timerData.setMin(duration);
			}

			if (duration > timerData.getMax()) {
				timerData.setMax(duration);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, null, rsc);
	}

}
