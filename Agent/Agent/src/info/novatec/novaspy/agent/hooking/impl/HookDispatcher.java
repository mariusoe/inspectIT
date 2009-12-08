package info.novatec.novaspy.agent.hooking.impl;

import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.core.ICoreService;
import info.novatec.novaspy.agent.hooking.IConstructorHook;
import info.novatec.novaspy.agent.hooking.IHookDispatcher;
import info.novatec.novaspy.agent.hooking.IMethodHook;
import info.novatec.novaspy.agent.sensor.exception.ExceptionTracingSensor;
import info.novatec.novaspy.agent.sensor.exception.IExceptionTracingHook;
import info.novatec.novaspy.agent.sensor.method.IMethodSensor;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The hook dispatching service which is called by all the hooks throughout the
 * instrumented target application.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public class HookDispatcher implements IHookDispatcher {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(HookDispatcher.class.getName());

	/**
	 * The default core service.
	 */
	private final ICoreService coreService;

	/**
	 * Contains all method hooks.
	 */
	private Map methodHooks = new Hashtable();

	/**
	 * Contains all constructor hooks.
	 */
	private Map constructorHooks = new Hashtable();

	/**
	 * Stores the current Status of the invocation sequence tracer in a
	 * {@link ThreadLocal} object.
	 */
	private InvocationSequenceCount invocationSequenceCount = new InvocationSequenceCount();

	/**
	 * A thread local holder object to save the current started invocation
	 * sequence.
	 */
	private ThreadLocal invocationSequenceHolder = new ThreadLocal();

	/**
	 * The link to the exception tracing hook.
	 */
	private IExceptionTracingHook exceptionHook = ExceptionTracingSensor.getHook();

	/**
	 * Default constructor which needs a reference to the core service. This is
	 * needed for the invocation sensor.
	 * 
	 * @param coreService
	 *            The core service.
	 */
	public HookDispatcher(ICoreService coreService) {
		this.coreService = coreService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodMapping(long id, RegisteredSensorConfig rsc) {
		methodHooks.put(new Long(id), rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addConstructorMapping(long id, RegisteredSensorConfig rsc) {
		constructorHooks.put(new Long(id), rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMethodBeforeBody(long id, Object object, Object[] parameters) {
		try {
			RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(new Long(id));

			if (rsc.startsInvocationSequence()) {
				// The sensor configuration contains an invocation sequence
				// sensor. We have to set it on the thread local map for later
				// access. Additionally, we need to save the count of the called
				// invocation sensors, as another nested one could be started,
				// too.
				invocationSequenceCount.increment();

				if (null == invocationSequenceHolder.get()) {
					invocationSequenceHolder.set(((IMethodSensor) rsc.getInvocationSequenceSensorTypeConfig().getSensorType()).getHook());
				}
			} else if (null != invocationSequenceHolder.get()) {
				// We are executing the following sensor types in an invocation
				// sequence context, thus we have to execute the before body
				// method of the invocation sequence hook manually.
				IMethodHook invocationHook = (IMethodHook) invocationSequenceHolder.get();

				// The sensor type ID is not important here, thus we are passing
				// a -1. It is already stored in the data object
				invocationHook.beforeBody(id, -1, object, parameters, rsc);
			}

			// Now iterate over all registered sensor types and execute them
			for (Iterator i = rsc.getReverseMethodHooks().entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				IMethodHook methodHook = (IMethodHook) entry.getValue();
				methodHook.beforeBody(id, ((Long) entry.getKey()).longValue(), object, parameters, rsc);
			}
		} catch (Throwable throwable) {
			LOGGER.severe("An error happened in the Hook Dispatcher! (before body)");
			throwable.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchFirstMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue) {
		try {
			RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(new Long(id));
			for (Iterator i = rsc.getMethodHooks().entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				IMethodHook methodHook = (IMethodHook) entry.getValue();
				methodHook.firstAfterBody(id, ((Long) entry.getKey()).longValue(), object, parameters, returnValue, rsc);
			}
		} catch (Throwable throwable) {
			LOGGER.severe("An error happened in the Hook Dispatcher! (after body)");
			throwable.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchSecondMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue) {
		try {
			RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(new Long(id));

			if (null != invocationSequenceHolder.get()) {
				// Need to replace the core service with the one from the
				// invocation sequence so that all data objects can be
				// associated to that invocation record.
				ICoreService invocCoreService = (ICoreService) invocationSequenceHolder.get();

				for (Iterator i = rsc.getMethodHooks().entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					IMethodHook methodHook = (IMethodHook) entry.getValue();
					// the invocation sequence sensor needs the original core
					// service!
					if (invocCoreService == methodHook) {
						methodHook.secondAfterBody(coreService, id, ((Long) entry.getKey()).longValue(), object, parameters, returnValue, rsc);
					} else {
						methodHook.secondAfterBody(invocCoreService, id, ((Long) entry.getKey()).longValue(), object, parameters, returnValue, rsc);
					}
				}
			} else {
				for (Iterator i = rsc.getMethodHooks().entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					IMethodHook methodHook = (IMethodHook) entry.getValue();
					methodHook.secondAfterBody(coreService, id, ((Long) entry.getKey()).longValue(), object, parameters, returnValue, rsc);
				}
			}

			if (rsc.startsInvocationSequence()) {
				invocationSequenceCount.decrement();

				if (0 == invocationSequenceCount.getCount()) {
					invocationSequenceHolder.set(null);
				}
			} else if (null != invocationSequenceHolder.get()) {
				// We have to execute the after body method of the invocation
				// sequence hook manually.
				IMethodHook invocationHook = (IMethodHook) invocationSequenceHolder.get();

				// The sensor type ID is not important here, thus we are passing
				// a -1. It is already stored in the data object
				invocationHook.secondAfterBody(coreService, id, -1, object, parameters, returnValue, rsc);
			}
		} catch (Throwable throwable) {
			LOGGER.severe("An error happened in the Hook Dispatcher! (second after body)");
			throwable.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorOfThrowable(long id, Object exceptionObject, Object[] parameters) {
		RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(new Long(id));
		long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

		exceptionHook.dispatchConstructorOfThrowable(coreService, id, sensorTypeId, exceptionObject, parameters, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject) {
		// rsc contains the settings for the actual method where the exception
		// was thrown.
		RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(new Long(id));
		long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

		exceptionHook.dispatchOnThrowInBody(coreService, id, sensorTypeId, object, exceptionObject, parameters, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchBeforeCatch(long id, Object exceptionObject) {
		// rsc contains the settings of the actual method where the exception
		// is catched.
		RegisteredSensorConfig rsc = (RegisteredSensorConfig) methodHooks.get(new Long(id));
		long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

		exceptionHook.dispatchBeforeCatchBody(coreService, id, sensorTypeId, exceptionObject, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject) {
		// rsc contains the settings for the actual constructor where the
		// exception was thrown.
		RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(new Long(id));
		long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

		exceptionHook.dispatchOnThrowInBody(coreService, id, sensorTypeId, object, exceptionObject, parameters, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorBeforeCatch(long id, Object exceptionObject) {
		// rsc contains the settings of the actual constructor where the
		// exception is catched.
		RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(new Long(id));
		long sensorTypeId = rsc.getExceptionSensorTypeConfig().getId();

		exceptionHook.dispatchBeforeCatchBody(coreService, id, sensorTypeId, exceptionObject, rsc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorBeforeBody(long id, Object object, Object[] parameters) {
		try {
			RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(new Long(id));

			if (rsc.startsInvocationSequence()) {
				// The sensor configuration contains an invocation sequence
				// sensor. We have to set it on the thread local map for later
				// access. Additionally, we need to save the count of the called
				// invocation sensors, as another nested one could be started,
				// too.
				invocationSequenceCount.increment();
				if (null == invocationSequenceHolder.get()) {
					invocationSequenceHolder.set(((IMethodSensor) rsc.getInvocationSequenceSensorTypeConfig().getSensorType()).getHook());
				}
			} else if (null != invocationSequenceHolder.get()) {
				// We are executing the following sensor types in an invocation
				// sequence context, thus we have to execute the before body
				// method of the invocation sequence hook manually.
				IConstructorHook invocationHook = (IConstructorHook) invocationSequenceHolder.get();

				// The sensor type ID is not important here, thus we are passing
				// a -1. It is already stored in the data object
				invocationHook.beforeConstructor(id, -1, object, parameters, rsc);
			}

			// Now iterate over all registered sensor types and execute them
			for (Iterator i = rsc.getReverseMethodHooks().entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				IConstructorHook constructorHook = (IConstructorHook) entry.getValue();
				constructorHook.beforeConstructor(id, ((Long) entry.getKey()).longValue(), object, parameters, rsc);
			}
		} catch (Throwable throwable) {
			LOGGER.severe("An error happened in the Hook Dispatcher! (before constructor)");
			throwable.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchConstructorAfterBody(long id, Object object, Object[] parameters) {
		try {
			RegisteredSensorConfig rsc = (RegisteredSensorConfig) constructorHooks.get(new Long(id));

			if (null != invocationSequenceHolder.get()) {
				// Need to replace the core service with the one from the
				// invocation sequence so that all data objects can be
				// associated to that invocation record.
				ICoreService invocCoreService = (ICoreService) invocationSequenceHolder.get();

				for (Iterator i = rsc.getMethodHooks().entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					IConstructorHook constructorHook = (IConstructorHook) entry.getValue();
					// the invocation sequence sensor needs the original core
					// service!
					if (invocCoreService == constructorHook) {
						constructorHook.afterConstructor(coreService, id, ((Long) entry.getKey()).longValue(), object, parameters, rsc);
					} else {
						constructorHook.afterConstructor(invocCoreService, id, ((Long) entry.getKey()).longValue(), object, parameters, rsc);
					}
				}
			} else {
				for (Iterator i = rsc.getMethodHooks().entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					IConstructorHook constructorHook = (IConstructorHook) entry.getValue();
					constructorHook.afterConstructor(coreService, id, ((Long) entry.getKey()).longValue(), object, parameters, rsc);
				}
			}

			if (rsc.startsInvocationSequence()) {
				invocationSequenceCount.decrement();

				if (0 == invocationSequenceCount.getCount()) {
					invocationSequenceHolder.set(null);
				}
			} else if (null != invocationSequenceHolder.get()) {
				// We have to execute the after body method of the invocation
				// sequence hook manually.
				IConstructorHook invocationHook = (IConstructorHook) invocationSequenceHolder.get();

				// The sensor type ID is not important here, thus we are passing
				// a -1. It is already stored in the data object
				invocationHook.afterConstructor(coreService, id, -1, object, parameters, rsc);
			}
		} catch (Throwable throwable) {
			LOGGER.severe("An error happened in the Hook Dispatcher! (after constructor)");
			throwable.printStackTrace();
		}
	}

	/**
	 * Private inner class used to track the count of the started invocation
	 * sequences in one thread. Thus it extends {@link ThreadLocal} to provide a
	 * unique number for every Thread.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static class InvocationSequenceCount extends ThreadLocal {

		/**
		 * {@inheritDoc}
		 */
		protected Object initialValue() {
			return new Long(0);
		}

		/**
		 * Increments the stored value.
		 */
		public void increment() {
			super.set(new Long(((Long) super.get()).longValue() + 1));
		}

		/**
		 * Decrements the stored value.
		 */
		public void decrement() {
			super.set(new Long(((Long) super.get()).longValue() - 1));
		}

		/**
		 * Returns the current count.
		 * 
		 * @return The count.
		 */
		public long getCount() {
			return ((Long) super.get()).longValue();
		}

	}
}
