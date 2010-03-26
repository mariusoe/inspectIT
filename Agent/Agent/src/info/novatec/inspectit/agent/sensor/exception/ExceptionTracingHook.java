package info.novatec.inspectit.agent.sensor.exception;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class adds additional code to a constructor of type {@link Throwable},
 * to the <code>throw</code> statement and to the <code>catch</code> block
 * catching type {@link Throwable}.
 * 
 * @author Eduard Tudenhoefner
 * @see IExceptionTracingHook
 * 
 */
public class ExceptionTracingHook implements IExceptionTracingHook {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ExceptionTracingHook.class.getName());

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The thread local containing the {@link IdentityHashToDataObject} object.
	 */
	private ThreadLocal exceptionDataHolder = new ThreadLocal();

	/**
	 * The thread local containing the id of the method where the exception was
	 * handled.
	 */
	private ThreadLocal exceptionHandlerId = new ThreadLocal();

	/**
	 * The default constructor which needs one parameter for initialization.
	 * 
	 * 
	 * @param idManager
	 *            The ID manager.
	 */
	public ExceptionTracingHook(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorOfThrowable(ICoreService coreService, long id, long sensorTypeId, Object exceptionObject, Object[] parameters, RegisteredSensorConfig rsc) {
		// TODO ET: this method is called twice when the constructor of a
		// Throwable calls another constructor with this(). The first created
		// data object is then discarded.
		try {
			long platformId = idManager.getPlatformId();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			long registeredConstructorId = idManager.getRegisteredMethodId(id);
			long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
			Integer identityHash = new Integer(System.identityHashCode(exceptionObject));

			// need to reset the exception handler id
			exceptionHandlerId.set(null);

			// getting the actual object with information
			Throwable throwable = (Throwable) exceptionObject;

			// creating the data object
			ExceptionSensorData data = new ExceptionSensorData(timestamp, platformId, registeredSensorTypeId, registeredConstructorId);
			data.setThrowableIdentityHashCode(identityHash.intValue());
			data.setExceptionEvent(ExceptionEventEnum.CREATED);
			data.setThrowableType(throwable.getClass().getName());

			// set the static information of the current object
			setStaticInformation(data, throwable);

			// creating the mapping object and setting it on the thread
			// local
			exceptionDataHolder.set(new IdentityHashToDataObject(identityHash, data));

			// adding the data object to the core service
			coreService.addExceptionSensorData(registeredSensorTypeId, data.getThrowableIdentityHashCode(), data);
		} catch (IdNotAvailableException e) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("Could not start exception sequence because of a (currently) not mapped ID");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchOnThrowInBody(ICoreService coreService, long id, long sensorTypeId, Object object, Object exceptionObject, Object[] parameters, RegisteredSensorConfig rsc) {
		// get the mapping object
		IdentityHashToDataObject mappingObject = (IdentityHashToDataObject) exceptionDataHolder.get();

		if (null != mappingObject) {
			try {
				long platformId = idManager.getPlatformId();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				long registeredMethodId = idManager.getRegisteredMethodId(id);
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				Integer identityHash = new Integer(System.identityHashCode(exceptionObject));

				// getting the actual object with information
				Throwable throwable = (Throwable) exceptionObject;

				// creating the data object
				ExceptionSensorData data = new ExceptionSensorData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
				data.setThrowableIdentityHashCode(identityHash.intValue());
				data.setThrowableType(throwable.getClass().getName());

				// check whether it's the same Throwable object as before
				if (mappingObject.getIdentityHash().equals(identityHash)) {
					// we have to check whether the Throwable object is just
					// passed or explicitly rethrown
					if ((null != exceptionHandlerId.get()) && (registeredMethodId == ((Long) exceptionHandlerId.get()).longValue())) {
						// the Throwable object is explicitly rethrown
						data.setExceptionEvent(ExceptionEventEnum.RETHROWN);
					} else {
						// the Throwable object is thrown the first time or just
						// passed by the JVM, so it's a PASSED event
						data.setExceptionEvent(ExceptionEventEnum.PASSED);
					}

					// current object is the child of the previous object
					ExceptionSensorData parent = mappingObject.getExceptionSensorData();
					parent.setChild(data);

					// we are just exchanging the data object and setting it on
					// the mapping object
					mappingObject.setExceptionSensorData(data);
					exceptionDataHolder.set(mappingObject);
				} else {
					// it's a new Throwable object, that we didn't recognize
					// earlier
					data.setExceptionEvent(ExceptionEventEnum.UNREGISTERED_PASSED);
					setStaticInformation(data, throwable);

					// we are creating a new mapping object and setting it on
					// the thread local
					exceptionDataHolder.set(new IdentityHashToDataObject(identityHash, data));
				}

				// adding the data object to the core service
				coreService.addExceptionSensorData(registeredSensorTypeId, data.getThrowableIdentityHashCode(), data);
			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not start exception sequence because of a (currently) not mapped ID");
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchBeforeCatchBody(ICoreService coreService, long id, long sensorTypeId, Object exceptionObject, RegisteredSensorConfig rsc) {
		// get the mapping object
		IdentityHashToDataObject mappingObject = (IdentityHashToDataObject) exceptionDataHolder.get();

		if (null != mappingObject) {
			try {
				long platformId = idManager.getPlatformId();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				long registeredMethodId = idManager.getRegisteredMethodId(id);
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				Integer identityHash = new Integer(System.identityHashCode(exceptionObject));

				// save id of the method where the exception is catched
				exceptionHandlerId.set(new Long(registeredMethodId));

				// getting the actual object with information
				Throwable throwable = (Throwable) exceptionObject;

				// creating the data object
				ExceptionSensorData data = new ExceptionSensorData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
				data.setThrowableIdentityHashCode(identityHash.intValue());
				data.setThrowableType(throwable.getClass().getName());
				data.setExceptionEvent(ExceptionEventEnum.HANDLED);

				// check whether it's the same Throwable object as before
				if (mappingObject.getIdentityHash().equals(identityHash)) {
					// current object is the child of the previous object
					ExceptionSensorData parent = mappingObject.getExceptionSensorData();
					parent.setChild(data);

					// we are just exchanging the data object and setting it on
					// the mapping object
					mappingObject.setExceptionSensorData(data);
					exceptionDataHolder.set(mappingObject);
				} else {
					// it's a Throwable object, that we didn't recognize
					// earlier
					data.setExceptionEvent(ExceptionEventEnum.UNREGISTERED_PASSED);
					setStaticInformation(data, throwable);

					// we are creating a new mapping object and setting it on
					// the thread local
					exceptionDataHolder.set(new IdentityHashToDataObject(identityHash, data));
				}

				// adding the data object to the core service
				coreService.addExceptionSensorData(registeredSensorTypeId, data.getThrowableIdentityHashCode(), data);
			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not start exception sequence because of a (currently) not mapped ID");
				}
			}
		}
	}

	/**
	 * Gets static information (class name, stackTrace, cause) from the
	 * {@link Throwable} object and sets them on the passed data object.
	 * 
	 * @param exceptionSensorData
	 *            The {@link ExceptionSensorData} object where to set the
	 *            information.
	 * @param throwable
	 *            The current {@link Throwable} object where to get the
	 *            information.
	 */
	private void setStaticInformation(ExceptionSensorData exceptionSensorData, Throwable throwable) {
		Throwable cause = throwable.getCause();
		if (null != cause) {
			exceptionSensorData.setCause(crop(cause.getClass().getName(), 1000));
		}
		exceptionSensorData.setErrorMessage(throwable.getMessage());
		exceptionSensorData.setStackTrace(stackTraceToString(throwable));
		// exceptionSensorData.setStackTrace(getStackTrace(throwable));
	}

	/**
	 * This method is called to retrieve the stack trace of a {@link Throwable}.
	 * The stack trace is gathered by accessing the private method
	 * {@link Throwable#getOurStackTrace()} with reflection. If this method is
	 * not available or cannot be accessed, then the stack trace is gathered
	 * using {@link Throwable#getStackTrace()}.
	 * 
	 * @param throwable
	 *            The {@link Throwable} object where to get the stack trace.
	 * @return An array of {@link StackTraceElement}.
	 */
	private StackTraceElement[] getStackTrace(Throwable throwable) {
		StackTraceElement[] stackTrace = null;
		try {
			Method method = Throwable.class.getDeclaredMethod("getOurStackTrace", null);
			method.setAccessible(true);
			stackTrace = ((StackTraceElement[]) method.invoke(throwable, null));
		} catch (Exception e) {
			stackTrace = throwable.getStackTrace();
		}

		return stackTrace;
	}

	/**
	 * Gets the stack trace from the {@link Throwable} object and returns it as
	 * a string.
	 * 
	 * @param throwable
	 *            The {@link Throwable} object where to get the stack trace
	 *            from.
	 * @return A string representation of a stack trace.
	 */
	private String stackTraceToString(Throwable throwable) {
		Writer result = new StringWriter();
		PrintWriter writer = new PrintWriter(result);
		throwable.printStackTrace(writer);
		return result.toString();
	}

	/**
	 * Crops a string if it is longer than the specified maxLength.
	 * 
	 * @param value
	 *            The value to crop.
	 * @param maxLength
	 *            The maximum length of the string. All characters above
	 *            maxLength will be cropped.
	 * @return A cropped string which length is smaller than the maxLength.
	 */
	private String crop(String value, int maxLength) {
		if (null != value && value.length() >= maxLength) {
			return value.substring(0, maxLength - 1);
		}
		return value;
	}
}
