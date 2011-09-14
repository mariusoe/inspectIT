package info.novatec.inspectit.agent.hooking.impl;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHookDispatcher;
import info.novatec.inspectit.agent.hooking.IHookInstrumenter;
import info.novatec.inspectit.javassist.CannotCompileException;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.Modifier;
import info.novatec.inspectit.javassist.NotFoundException;
import info.novatec.inspectit.javassist.expr.ExprEditor;
import info.novatec.inspectit.javassist.expr.Handler;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The byte code instrumenter class. Used to instrument the additional instructions into the target
 * byte code.
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
	 * The hook dispatcher. This string shouldn't be touched. For changing the dispatcher, alter the
	 * hook dispatcher instance in the Agent class.
	 */
	private static String hookDispatcherTarget = "info.novatec.inspectit.agent.PicoAgent#getInstance().getHookDispatcher()";

	/**
	 * The hook dispatching service.
	 */
	private final IHookDispatcher hookDispatcher;

	/**
	 * The ID manager used to register the methods and the mapping between the method sensor type id
	 * and the method id.
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
	 * The implementation of the configuration storage where all definitions of the user are stored.
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
	 *            The configuration storage where all definitions of the user are stored.
	 */
	public HookInstrumenter(IHookDispatcher hookDispatcher, IIdManager idManager, IConfigurationStorage configurationStorage) {
		this.hookDispatcher = hookDispatcher;
		this.idManager = idManager;
		this.configurationStorage = configurationStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodHook(CtMethod method, RegisteredSensorConfig rsc) throws HookException {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Match found! Class: " + rsc.getTargetClassName() + " Method: " + rsc.getTargetMethodName() + " Parameter: " + rsc.getParameterTypes() + " id: " + rsc.getId());
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
			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();
			boolean exceptionSensorEnhanced = configurationStorage.isEnhancedExceptionSensorActivated();
			// instrument as finally if exception sensor is deactivated or activated in simple mode
			boolean asFinally = !(exceptionSensorActivated && exceptionSensorEnhanced);
			
			if (Modifier.isStatic(method.getModifiers())) {
				// static method
				method.insertBefore(hookDispatcherTarget + ".dispatchMethodBeforeBody(" + methodId + "l, null, $args);");
				method.insertAfter(hookDispatcherTarget + ".dispatchFirstMethodAfterBody(" + methodId + "l, null, $args, ($w)$_);", asFinally);
				method.insertAfter(hookDispatcherTarget + ".dispatchSecondMethodAfterBody(" + methodId + "l, null, $args, ($w)$_);", asFinally);

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
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Constructor match found! Class: " + rsc.getTargetClassName() + " Parameter: " + rsc.getParameterTypes() + " id: " + rsc.getId());
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
			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();
			boolean exceptionSensorEnhanced = configurationStorage.isEnhancedExceptionSensorActivated();
			// instrument as finally if exception sensor is deactivated or activated in simple mode
			boolean asFinally = !(exceptionSensorActivated && exceptionSensorEnhanced);
			
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
	 * The passed {@link CtMethod} is instrumented with an internal <code>try-catch</code> block to
	 * get an event when an exception is thrown in a method body.
	 * 
	 * @see info.novatec.inspectit.javassist.CtMethod#addCatch(String, CtClass)
	 * 
	 * @param method
	 *            The {@link CtMethod} where additional instructions are added.
	 * @param methodId
	 *            The method id of the passed method.
	 * @param isStatic
	 *            Defines whether the current method is a static method.
	 * @throws CannotCompileException
	 *             When the additional instructions could not be compiled by Javassist.
	 * @throws NotFoundException
	 *             When {@link Throwable} cannot be found in the default {@link ClassPool}.
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
			method.addCatch(hookDispatcherTarget + ".dispatchOnThrowInBody(" + methodId + "l, null, $args, $e);" + hookDispatcherTarget + ".dispatchFirstMethodAfterBody(" + methodId
					+ "l, null, $args, null);" + hookDispatcherTarget + ".dispatchSecondMethodAfterBody(" + methodId + "l, null, $args, null);" + "throw $e; ", type);
		}
	}

	/**
	 * The passed {@link CtConstructor} is instrumented with an internal <code>try-catch</code>
	 * block to get an event when an exception is thrown in a constructor body.
	 * 
	 * @see info.novatec.inspectit.javassist.CtConstructor#addCatch(String, CtClass)
	 * 
	 * @param constructor
	 *            The {@link CtConstructor} where additional instructions are added.
	 * @param constructorId
	 *            The constructor id of the passed constructor.
	 * @throws CannotCompileException
	 *             When the additional instructions could not be compiled by Javassist.
	 * @throws NotFoundException
	 *             When {@link Throwable} cannot be found in the default {@link ClassPool}.
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
	 * If <code>instrument()</code> is called in <code>CtMethod</code>, the method body is scanned
	 * from the beginning to the end. Whenever an expression, such as a Handler of an Exception is
	 * found, <code>edit()</code> is called in <code>ExprEditor</code>. <code>edit()</code> can
	 * inspect and modify the given expression. The modification is reflected on the original method
	 * body. If <code>edit()</code> does nothing, the original method body is not changed.
	 * 
	 * @see info.novatec.inspectit.javassist.expr.ExprEditor
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public static class MethodExprEditor extends ExprEditor {

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
		public void edit(Handler handler) throws CannotCompileException {
			if (!handler.isFinally()) {
				// $1 is the exception object
				handler.insertBefore(hookDispatcherTarget + ".dispatchBeforeCatch(" + id + "l, $1);");
			}
		}
	}

	/**
	 * If <code>instrument()</code> is called in <code>CtConstructor</code>, the constructor body is
	 * scanned from the beginning to the end. Whenever an expression, such as a Handler of an
	 * Exception is found, <code>edit()</code> is called in <code>ExprEditor</code>.
	 * <code>edit()</code> can inspect and modify the given expression. The modification is
	 * reflected on the original constructor body. If <code>edit()</code> does nothing, the original
	 * constructor body is not changed.
	 * 
	 * @see info.novatec.inspectit.javassist.expr.ExprEditor
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public static class ConstructorExprEditor extends ExprEditor {

		/**
		 * The id of the constructor which is instrumented with the ExpressionEditor.
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
		public void edit(Handler handler) throws CannotCompileException {
			if (!handler.isFinally()) {
				// $1 is the exception object
				handler.insertBefore(hookDispatcherTarget + ".dispatchConstructorBeforeCatch(" + id + "l, $1);");
			}
		}
	}

}
