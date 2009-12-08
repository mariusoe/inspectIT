package info.novatec.novaspy.agent.hooking.impl;

import info.novatec.novaspy.agent.config.IConfigurationStorage;
import info.novatec.novaspy.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.hooking.IHookDispatcher;
import info.novatec.novaspy.agent.hooking.IHookInstrumenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Handler;

/**
 * The byte code instrumenter class. Used to instrument the additional
 * instructions into the target byte code.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public class HookInstrumenter implements IHookInstrumenter {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(HookInstrumenter.class.getName());

	/**
	 * The hook dispatcher. This string shouldn't be touched. For changing the
	 * dispatcher, alter the hook dispatcher instance in the Agent class.
	 */
	private static String hookDispatcherTarget = "info.novatec.novaspy.agent.PicoAgent#getInstance().getHookDispatcher()";

	/**
	 * The hook dispatching service.
	 */
	private final IHookDispatcher hookDispatcher;

	/**
	 * The ID manager used to register the methods and the mapping between the
	 * method sensor type id and the method id.
	 */
	private final IIdManager idManager;

	/**
	 * The expression editor to modify a method body.
	 */
	private MethodExprEditor methodExprEditor = new MethodExprEditor();

	/**
	 * The expression editor to modify a constructor body.
	 */
	private ConstructorExprEditor constructorExprEditor = new ConstructorExprEditor();

	/**
	 * The implementation of the configuration storage where all definitions of
	 * the user are stored.
	 */
	private IConfigurationStorage configurationStorage;

	/**
	 * The default and only constructor for this class.
	 * 
	 * @param hookDispatcher
	 *            The hook dispatcher which is used in the Agent.
	 * @param idManager
	 *            The ID manager.
	 * @param configurationStorage
	 *            The configuration storage where all definitions of the user
	 *            are stored.
	 */
	public HookInstrumenter(IHookDispatcher hookDispatcher, IIdManager idManager, IConfigurationStorage configurationStorage) {
		// This will set the useContextClassLoader parameter to true to resolve
		// the current class of static methods. The standard method of looking
		// for a class via Class.forName(...) does not work!
		javassist.runtime.Desc.useContextClassLoader = true;

		this.hookDispatcher = hookDispatcher;
		this.idManager = idManager;
		this.configurationStorage = configurationStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodHook(CtMethod method, RegisteredSensorConfig rsc) throws HookException {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Match found! Class: " + rsc.getTargetClassName() + " Method: " + rsc.getTargetMethodName() + " Parameter: " + rsc.getParameterTypes() + " id: " + rsc.getId());
		}

		if (method.getDeclaringClass().isFrozen()) {
			// defrost before we are adding any instructions
			method.getDeclaringClass().defrost();
		}

		final long methodId = idManager.registerMethod(rsc);

		for (Iterator iterator = rsc.getSensorTypeConfigs().iterator(); iterator.hasNext();) {
			MethodSensorTypeConfig config = (MethodSensorTypeConfig) iterator.next();
			long sensorTypeId = config.getId();
			idManager.addSensorTypeToMethod(sensorTypeId, methodId);
		}

		try {
			boolean asFinally = !configurationStorage.isExceptionSensorActivated();
			if (Modifier.isStatic(method.getModifiers())) {
				// static method
				method.insertBefore(hookDispatcherTarget + ".dispatchMethodBeforeBody(" + methodId + "l, $class, $args);");
				method.insertAfter(hookDispatcherTarget + ".dispatchFirstMethodAfterBody(" + methodId + "l, $class, $args, ($w)$_);", asFinally);
				method.insertAfter(hookDispatcherTarget + ".dispatchSecondMethodAfterBody(" + methodId + "l, $class, $args, ($w)$_);", asFinally);

				if (!asFinally) {
					// the exception sensor is activated, so instrument the
					// static method with an addCatch
					instrumentMethodWithTryCatch(method, methodId, true);
				}
			} else {
				// normal method
				method.insertBefore(hookDispatcherTarget + ".dispatchMethodBeforeBody(" + methodId + "l, $0, $args);");
				method.insertAfter(hookDispatcherTarget + ".dispatchFirstMethodAfterBody(" + methodId + "l, $0, $args, ($w)$_);", asFinally);
				method.insertAfter(hookDispatcherTarget + ".dispatchSecondMethodAfterBody(" + methodId + "l, $0, $args, ($w)$_);", asFinally);

				if (!asFinally) {
					// the exception sensor is activated, so instrument the
					// normal method with an addCatch
					instrumentMethodWithTryCatch(method, methodId, false);
				}
			}

			// Add the information to the dispatching service
			hookDispatcher.addMethodMapping(methodId, rsc);
		} catch (CannotCompileException cannotCompileException) {
			throw new HookException("Could not insert the bytecode into the method/class", cannotCompileException);
		} catch (NotFoundException notFoundException) {
			// for the addCatch method needed
			throw new HookException("Could not insert the bytecode into the method/class", notFoundException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addConstructorHook(CtConstructor constructor, RegisteredSensorConfig rsc) throws HookException {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Constructor match found! Class: " + rsc.getTargetClassName() + " Parameter: " + rsc.getParameterTypes() + " id: " + rsc.getId());
		}

		if (constructor.getDeclaringClass().isFrozen()) {
			// defrost before we are adding any instructions
			constructor.getDeclaringClass().defrost();
		}

		long constructorId = idManager.registerMethod(rsc);

		for (Iterator iterator = rsc.getSensorTypeConfigs().iterator(); iterator.hasNext();) {
			MethodSensorTypeConfig config = (MethodSensorTypeConfig) iterator.next();
			long sensorTypeId = config.getId();
			idManager.addSensorTypeToMethod(sensorTypeId, constructorId);
		}

		try {
			boolean asFinally = !configurationStorage.isExceptionSensorActivated();
			constructor.insertBeforeBody(hookDispatcherTarget + ".dispatchConstructorBeforeBody(" + constructorId + "l, $0, $args);");
			constructor.insertAfter(hookDispatcherTarget + ".dispatchConstructorAfterBody(" + constructorId + "l, $0, $args);", asFinally);

			if (!asFinally) {
				instrumentConstructorWithTryCatch(constructor, constructorId);
			}

			// Add the information to the dispatching service
			hookDispatcher.addConstructorMapping(constructorId, rsc);
		} catch (CannotCompileException cannotCompileException) {
			throw new HookException("Could not insert the bytecode into the constructor/class", cannotCompileException);
		} catch (NotFoundException notFoundException) {
			throw new HookException("Could not insert the bytecode into the constructor/class", notFoundException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void instrumentConstructorOfThrowable(CtConstructor[] constructors, CtClass throwable, RegisteredSensorConfig rsc) throws HookException, NotFoundException {
		if (throwable.isFrozen()) {
			// defrost before we are adding any instructions
			throwable.defrost();
		}

		for (int i = 0; i < constructors.length; i++) {
			long constructorId = idManager.registerMethod(rsc);
			rsc.setId(constructorId);
			rsc.setTargetMethodName(constructors[i].getName());

			// iterate over all sensor type configs. Currently there is only one
			// ExceptionSensorTypeConfig.
			for (Iterator iterator = rsc.getSensorTypeConfigs().iterator(); iterator.hasNext();) {
				MethodSensorTypeConfig config = (MethodSensorTypeConfig) iterator.next();
				long sensorTypeId = config.getId();
				idManager.addSensorTypeToMethod(sensorTypeId, constructorId);
			}

			// we are getting the parameters of the current constructor
			List parameters = new ArrayList();
			CtClass[] parameterTypes = constructors[i].getParameterTypes();
			for (int pos = 0; pos < parameterTypes.length; pos++) {
				parameters.add(parameterTypes[pos].getName());
			}
			rsc.setParameterTypes(parameters);

			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Throwable Constructor match found! Class: " + rsc.getTargetClassName() + " Parameter: " + rsc.getParameterTypes() + " id: " + rsc.getId());
			}

			try {
				// we use the insertAfter here, because after the Throwable
				// constructor is executed we can guarantee that there were no
				// errors and can now add our instructions.
				constructors[i].insertAfter(hookDispatcherTarget + ".dispatchConstructorOfThrowable(" + constructorId + "l, $0, $args);");

				// Add the information to the dispatching service
				hookDispatcher.addConstructorMapping(constructorId, rsc);
			} catch (CannotCompileException cannotCompileException) {
				throw new HookException("Could not insert the bytecode into the throwable constructor/class", cannotCompileException);
			}
		}

	}

	/**
	 * The passed {@link CtMethod} is instrumented with an internal
	 * <code>try-catch</code> block to get an event when an exception is thrown
	 * in a method body.
	 * 
	 * @see javassist.CtMethod#addCatch(String, CtClass)
	 * 
	 * @param method
	 *            The {@link CtMethod} where additional instructions are added.
	 * @param methodId
	 *            The method id of the passed method.
	 * @param isStatic
	 *            Defines whether the current method is a static method.
	 * @throws CannotCompileException
	 *             When the additional instructions could not be compiled by
	 *             Javassist.
	 * @throws NotFoundException
	 *             When {@link Throwable} cannot be found in the default
	 *             {@link ClassPool}.
	 */
	private void instrumentMethodWithTryCatch(CtMethod method, long methodId, boolean isStatic) throws CannotCompileException, NotFoundException {
		if (configurationStorage.isExceptionSensorActivated()) {
			// we instrument the method with an expression editor to get events
			// when a handler of an exception is found.
			methodExprEditor.setId(methodId);
			method.instrument(methodExprEditor);
		}

		CtClass type = ClassPool.getDefault().get("java.lang.Throwable");

		if (!isStatic) {
			// normal method
			method.addCatch(hookDispatcherTarget + ".dispatchOnThrowInBody(" + methodId + "l, $0, $args, $e);" + hookDispatcherTarget + ".dispatchFirstMethodAfterBody(" + methodId
					+ "l, $0, $args, null);" + hookDispatcherTarget + ".dispatchSecondMethodAfterBody(" + methodId + "l, $0, $args, null);" + "throw $e; ", type);
		} else {
			// static method
			method.addCatch(hookDispatcherTarget + ".dispatchOnThrowInBody(" + methodId + "l, $class, $args, $e);" + hookDispatcherTarget + ".dispatchFirstMethodAfterBody(" + methodId
					+ "l, $class, $args, null);" + hookDispatcherTarget + ".dispatchSecondMethodAfterBody(" + methodId + "l, $class, $args, null);" + "throw $e; ", type);
		}
	}

	/**
	 * The passed {@link CtConstructor} is instrumented with an internal
	 * <code>try-catch</code> block to get an event when an exception is thrown
	 * in a constructor body.
	 * 
	 * @see javassist.CtConstructor#addCatch(String, CtClass)
	 * 
	 * @param constructor
	 *            The {@link CtConstructor} where additional instructions are
	 *            added.
	 * @param constructorId
	 *            The constructor id of the passed constructor.
	 * @throws CannotCompileException
	 *             When the additional instructions could not be compiled by
	 *             Javassist.
	 * @throws NotFoundException
	 *             When {@link Throwable} cannot be found in the default
	 *             {@link ClassPool}.
	 */
	private void instrumentConstructorWithTryCatch(CtConstructor constructor, long constructorId) throws CannotCompileException, NotFoundException {
		if (configurationStorage.isExceptionSensorActivated()) {
			// we instrument the constructor with an expression editor to get
			// events when a handler of an exception is found.
			constructorExprEditor.setId(constructorId);
			constructor.instrument(constructorExprEditor);
		}

		CtClass type = ClassPool.getDefault().get("java.lang.Throwable");
		constructor.addCatch(hookDispatcherTarget + ".dispatchConstructorOnThrowInBody(" + constructorId + "l, $0, $args, $e);" + hookDispatcherTarget + ".dispatchConstructorAfterBody("
				+ constructorId + "l, $0, $args);" + "throw $e; ", type);
	}

	/**
	 * If <code>instrument()</code> is called in <code>CtMethod</code>, the
	 * method body is scanned from the beginning to the end. Whenever an
	 * expression, such as a Handler of an Exception is found,
	 * <code>edit()</code> is called in <code>ExprEditor</code>.
	 * <code>edit()</code> can inspect and modify the given expression. The
	 * modification is reflected on the original method body. If
	 * <code>edit()</code> does nothing, the original method body is not
	 * changed.
	 * 
	 * @see javassist.expr.ExprEditor
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public class MethodExprEditor extends ExprEditor {

		/**
		 * The id of the method which is instrumented with the ExpressionEditor.
		 */
		private long id = 0;

		/**
		 * The default constructor.
		 */
		public MethodExprEditor() {
		}

		/**
		 * Sets the method id.
		 * 
		 * @param id
		 *            The id of the method to instrument.
		 */
		public void setId(long id) {
			this.id = id;
		}

		/**
		 * {@inheritDoc}
		 */
		public void edit(Handler h) throws CannotCompileException {
			// $1 is the exception object
			h.insertBefore(hookDispatcherTarget + ".dispatchBeforeCatch(" + id + "l, $1);");
		}
	}

	/**
	 * If <code>instrument()</code> is called in <code>CtConstructor</code>, the
	 * constructor body is scanned from the beginning to the end. Whenever an
	 * expression, such as a Handler of an Exception is found,
	 * <code>edit()</code> is called in <code>ExprEditor</code>.
	 * <code>edit()</code> can inspect and modify the given expression. The
	 * modification is reflected on the original constructor body. If
	 * <code>edit()</code> does nothing, the original constructor body is not
	 * changed.
	 * 
	 * @see javassist.expr.ExprEditor
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public class ConstructorExprEditor extends ExprEditor {

		/**
		 * The id of the constructor which is instrumented with the
		 * ExpressionEditor.
		 */
		private long id = 0;

		/**
		 * The default constructor.
		 */
		public ConstructorExprEditor() {
		}

		/**
		 * Sets the constructor id.
		 * 
		 * @param id
		 *            The id of the constructor to instrument.
		 */
		public void setId(long id) {
			this.id = id;
		}

		/**
		 * {@inheritDoc}
		 */
		public void edit(Handler h) throws CannotCompileException {
			// $1 is the exception object
			h.insertBefore(hookDispatcherTarget + ".dispatchConstructorBeforeCatch(" + id + "l, $1);");
		}
	}

}
