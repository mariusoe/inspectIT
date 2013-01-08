package info.novatec.inspectit.agent.sensor.method.marvintimer;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.averagetimer.AverageTimerHook;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Two information objects should be saved along with the {@link TimerData} object to filter the
 * objects in the user interface. <br>
 * <b>Workflow</b> : <instance>.workflowClassName <br>
 * <b>Activity</b> : <param1>.activityName
 */
public class MarvinWorkflowHook implements IMethodHook {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AverageTimerHook.class.getName());

	/**
	 * The stack containing the start time values.
	 */
	private ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

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
	 * The configuration storage.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The data storage used to pass the timer data object from one method call to the other.
	 * Synchronized because it can be accessed from several threads at the same time.<br>
	 * The <b>key</b> is the instance of the class being called, and the <b>value</b> is the
	 * TimerData object.
	 */
	private Map<Object, TimerData> dataStorage = Collections.synchronizedMap(new IdentityHashMap<Object, TimerData>());

	/**
	 * This method ID is the local id for our own combined method sensor.
	 */
	private long marvinMethodId;

	/**
	 * Defines if the initialization was already done.
	 */
	private volatile boolean initialized = false;

	public MarvinWorkflowHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor, IConfigurationStorage configurationStorage) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
		this.configurationStorage = configurationStorage;
	}

	/**
	 * @param idManager
	 * @param configurationStorage
	 */
	private void init() {
		// we need to register our own method
		RegisteredSensorConfig rsc = new RegisteredSensorConfig();
		rsc.setTargetPackageName("vsa.marvin.workflow.synch.shared");
		rsc.setTargetClassName("SynchWorkflowImpl");
		rsc.setTargetMethodName("performActivity&getChangedData");
		rsc.setReturnType("void");

		marvinMethodId = idManager.registerMethod(rsc);
		// map the sensor type with the method
		long sensorTypeId = -1L;
		List<MethodSensorTypeConfig> methodSensorTypes = configurationStorage.getMethodSensorTypes();
		for (MethodSensorTypeConfig methodSensorType : methodSensorTypes) {
			if (MarvinWorkflowSensor.class.getName().equals(methodSensorType.getClassName())) {
				sensorTypeId = methodSensorType.getId();
				break;
			}
		}
		idManager.addSensorTypeToMethod(sensorTypeId, marvinMethodId);
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
		double endTime = timeStack.pop().doubleValue();
		double startTime = timeStack.pop().doubleValue();
		double duration = endTime - startTime;

		// initialize our sensor if not already done
		if (!initialized) {
			initialized = true;
			init();
		}

		String methodName = rsc.getTargetMethodName();
		if ("performActivity".equals(methodName)) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(marvinMethodId);

				List<ParameterContentData> parameterContentData = propertyAccessor.getParameterContentData(rsc.getPropertyAccessorList(), object, parameters, result);

				Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));
				TimerData timerData = new TimerData(timestamp, platformId, registeredSensorTypeId, registeredMethodId, parameterContentData);
				timerData.addDuration(duration);

				dataStorage.put(object, timerData);
			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not save the average timer data because of an unavailable id. " + e.getMessage());
				}
			}
		} else if ("getChangedData".equals(methodName)) {
			TimerData timerData = dataStorage.remove(object);

			// could be null in case of exception thrown before in the method
			// and so the timerdata object is not initialized
			if (null != timerData) {
				double combinedDuration = timerData.getDuration() + duration;
				timerData.setDuration(combinedDuration);
				timerData.calculateMin(combinedDuration);
				timerData.calculateMax(combinedDuration);
				timerData.setCount(1L);

				// the sensor type and method id is not needed here as we don't
				// want to access this object in the future. Thus we pass the
				// nanos as the prefix which should be unique and set both IDs
				// to 0
				coreService.addMethodSensorData(0L, 0L, Double.toString(timer.getCurrentTime()), timerData);
			}
		}
	}

}
