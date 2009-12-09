package info.novatec.inspectit.agent.sensor.method.invocationsequence;

import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IObjectStorage;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.core.ListListener;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.net.ConnectException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The invocation sequence hook stores the record of the invocation sequences in
 * a {@link ThreadLocal} object.
 * <p>
 * This hook implements the {@link ICoreService} interface which simulates the
 * core service to all other hooks which are called during the execution of this
 * invocation. The <code>defaultCoreService</code> field is used to delegate
 * some calls directly to the original core service and later sending of the
 * data to the server.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocationSequenceHook implements IMethodHook, IConstructorHook, ICoreService {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(InvocationSequenceHook.class.getName());

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The property accessor.
	 */
	private final IPropertyAccessor propertyAccessor;

	/**
	 * The {@link ThreadLocal} object which holds an
	 * {@link InvocationSequenceData} object if an invocation record is started.
	 */
	private final ThreadLocal threadLocalInvocationData = new ThreadLocal();

	/**
	 * Stores the value of the method ID in the {@link ThreadLocal} object. Used
	 * to identify the correct start and end of the record.
	 */
	private final ThreadLocal invocationStartId = new ThreadLocal();

	/**
	 * Stores the count of the of the starting method being called in the same
	 * invocation sequence so that closing is done on the right end.
	 */
	private final ThreadLocal invocationStartIdCount = new ThreadLocal();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack timeStack = new ThreadLocalStack();

	/**
	 * Saves the min duration for faster access of the values.
	 */
	private Map minDurationMap = new HashMap();

	/**
	 * The default constructor is initialized with a reference to the original
	 * {@link ICoreService} implementation to delegate all calls to if the data
	 * needs to be sent.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public InvocationSequenceHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		try {
			long platformId = idManager.getPlatformId();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			long registeredMethodId = idManager.getRegisteredMethodId(methodId);

			if (null == threadLocalInvocationData.get()) {
				// save the start time
				timeStack.push(new Double(timer.getCurrentTime()));

				// the sensor type is only available in the beginning of the
				// sequence trace
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);

				// no invocation tracer is currently started, so we do that now.
				InvocationSequenceData invocationSequenceData = new InvocationSequenceData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
				threadLocalInvocationData.set(invocationSequenceData);

				invocationStartId.set(new Long(methodId));
				invocationStartIdCount.set(new Long(1));
			} else {
				if (methodId == ((Long) invocationStartId.get()).longValue()) {
					long count = ((Long) invocationStartIdCount.get()).longValue();
					invocationStartIdCount.set(new Long(count + 1));
				}
				// A subsequent call to the before body method where an
				// invocation tracer is already started.
				InvocationSequenceData invocationSequenceData = (InvocationSequenceData) threadLocalInvocationData.get();
				invocationSequenceData.setChildCount(invocationSequenceData.getChildCount() + 1L);

				InvocationSequenceData nestedInvocationSequenceData = new InvocationSequenceData(timestamp, platformId, invocationSequenceData.getSensorTypeIdent(), registeredMethodId);
				nestedInvocationSequenceData.setStart(timer.getCurrentTime());
				nestedInvocationSequenceData.setParentSequence(invocationSequenceData);

				invocationSequenceData.getNestedSequences().add(nestedInvocationSequenceData);

				threadLocalInvocationData.set(nestedInvocationSequenceData);
			}
		} catch (IdNotAvailableException idNotAvailableException) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("Could not start invocation sequence because of a (currently) not mapped ID");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		InvocationSequenceData invocationSequenceData = (InvocationSequenceData) threadLocalInvocationData.get();

		if (null != invocationSequenceData) {
			if (methodId == ((Long) invocationStartId.get()).longValue()) {
				long count = ((Long) invocationStartIdCount.get()).longValue();
				invocationStartIdCount.set(new Long(count - 1));

				if (0 == count - 1) {
					timeStack.push(new Double(timer.getCurrentTime()));
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		InvocationSequenceData invocationSequenceData = (InvocationSequenceData) threadLocalInvocationData.get();

		if (null != invocationSequenceData) {
			// check if some properties need to be accessed and saved
			if (rsc.isPropertyAccess()) {
				List parameterContentData = propertyAccessor.getParameterContentData(rsc.getPropertyAccessorList(), object, parameters);
				invocationSequenceData.setParameterContentData(new HashSet(parameterContentData));
			}

			if ((methodId == ((Long) invocationStartId.get()).longValue()) && (0 == ((Long) invocationStartIdCount.get()).longValue())) {
				double endTime = ((Double) timeStack.pop()).doubleValue();
				double startTime = ((Double) timeStack.pop()).doubleValue();
				double duration = endTime - startTime;

				// complete the sequence and store the data object in the 'true'
				// core service so that it can be transmitted to the server. we
				// just need an arbitrary prefix so that this sequence will
				// never be overwritten in the core service!
				if (minDurationMap.containsKey(invocationStartId.get())) {
					checkForSavingOrNot(coreService, methodId, sensorTypeId, rsc, invocationSequenceData, startTime, endTime, duration);
				} else {
					// maybe not saved yet in the map
					if (rsc.getSettings().containsKey("minduration")) {
						minDurationMap.put(invocationStartId.get(), Double.valueOf((String) rsc.getSettings().get("minduration")));
						checkForSavingOrNot(coreService, methodId, sensorTypeId, rsc, invocationSequenceData, startTime, endTime, duration);
					} else {
						invocationSequenceData.setDuration(duration);
						invocationSequenceData.setStart(startTime);
						invocationSequenceData.setEnd(endTime);
						coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.currentTimeMillis()), invocationSequenceData);
					}
				}

				threadLocalInvocationData.set(null);
			} else {
				// just close the nested sequence and set the correct child
				// count
				InvocationSequenceData parentSequence = invocationSequenceData.getParentSequence();
				invocationSequenceData.setEnd(timer.getCurrentTime());
				invocationSequenceData.setDuration(invocationSequenceData.getEnd()-invocationSequenceData.getStart());
				parentSequence.setChildCount(parentSequence.getChildCount() + invocationSequenceData.getChildCount());
				threadLocalInvocationData.set(parentSequence);
			}
		}
	}

	/**
	 * This checks if the invocation has to be saved or not (like the
	 * minduration is set and the invocation is faster than the specified time).
	 * 
	 * @param coreService
	 *            The reference to the core service which holds the data objects
	 *            etc.
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object which holds all the
	 *            information of the executed method.
	 * @param invocationSequenceData
	 *            The invocation sequence data object.
	 * @param duration
	 *            The actual duration.
	 */
	private void checkForSavingOrNot(ICoreService coreService, long methodId, long sensorTypeId, RegisteredSensorConfig rsc, InvocationSequenceData invocationSequenceData, double startTime, double endTime, double duration) {
		double minduration = ((Double) minDurationMap.get(invocationStartId.get())).doubleValue();
		if (duration >= minduration) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Saving invocation. " + duration + " > " + minduration + " ID(local): " + rsc.getId());
			}
			invocationSequenceData.setDuration(duration);
			invocationSequenceData.setStart(startTime);
			invocationSequenceData.setEnd(endTime);
			coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.currentTimeMillis()), invocationSequenceData);
		} else {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Not saving invocation. " + duration + " < " + minduration + " ID(local): " + rsc.getId());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		beforeBody(methodId, sensorTypeId, object, parameters, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		firstAfterBody(methodId, sensorTypeId, object, parameters, null, rsc);
		secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, null, rsc);
	}

	/**
	 * Save the data objects which are coming from all the different sensor
	 * types in the current invocation tracer context.
	 * 
	 * @param dataObject
	 *            The data object to save.
	 */
	private void saveDataObject(DefaultData dataObject) {
		InvocationSequenceData invocationSequenceData = (InvocationSequenceData) threadLocalInvocationData.get();

		if (dataObject instanceof SqlStatementData) {
			// don't overwrite an already existing sql statement data object.
			if (null == invocationSequenceData.getSqlStatementData()) {
				invocationSequenceData.setSqlStatementData((SqlStatementData) dataObject);
			}
		}

		if (dataObject instanceof TimerData) {
			// don't overwrite an already existing timerdata object
			if (null == invocationSequenceData.getTimerData()) {
				invocationSequenceData.setTimerData((TimerData) dataObject);
			}
		}
	}

	// //////////////////////////////////////////////
	// All methods from the ICoreService are below //
	// //////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	public void addMethodSensorData(long sensorTypeId, long methodId, String prefix, MethodSensorData methodSensorData) {
		if (null == threadLocalInvocationData.get()) {
			LOGGER.info("thread data NULL!!!!");
			return;
		}
		saveDataObject(methodSensorData.finalizeData());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addObjectStorage(long sensorTypeId, long methodId, String prefix, IObjectStorage objectStorage) {
		if (null == threadLocalInvocationData.get()) {
			LOGGER.info("thread data NULL!!!!");
			return;
		}
		DefaultData defaultData = objectStorage.finalizeDataObject();
		saveDataObject(defaultData.finalizeData());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorData(long sensorTypeIdent, SystemSensorData systemSensorData) {
		saveDataObject(systemSensorData.finalizeData());
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodSensorData getMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IObjectStorage getObjectStorage(long sensorTypeIdent, long methodIdent, String prefix) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemSensorData getPlatformSensorData(long sensorTypeIdent) {
		return null;
	}

	// //////////////////////////////////////////////
	// All unsupported methods are below from here //
	// //////////////////////////////////////////////

	public void addListListener(ListListener listener) {
		throw new UnsupportedMethodException();
	}

	public void addSendStrategy(ISendingStrategy strategy) {
		throw new UnsupportedMethodException();
	}

	public void connect() throws ConnectException {
		throw new UnsupportedMethodException();
	}

	public void removeListListener(ListListener listener) {
		throw new UnsupportedMethodException();
	}

	public void sendData() {
		throw new UnsupportedMethodException();
	}

	public void setBufferStrategy(IBufferStrategy bufferStrategy) {
		throw new UnsupportedMethodException();
	}

	public void startSendingStrategies() {
		throw new UnsupportedMethodException();
	}

	public void addPlatformSensorType(PlatformSensorTypeConfig platformSensorTypeConfig) {
		throw new UnsupportedMethodException();
	}

	public void start() {
		throw new UnsupportedMethodException();
	}

	public void stop() {
		throw new UnsupportedMethodException();
	}

	public void addExceptionSensorData(long sensorTypeIdent, int throwableIdentityHashCode, ExceptionSensorData exceptionSensorData) {
		throw new UnsupportedMethodException();
	}

	public ExceptionSensorData getExceptionSensorData(long sensorTypeIdent, int throwableIdentityHashCode) {
		throw new UnsupportedMethodException();
	}

}
