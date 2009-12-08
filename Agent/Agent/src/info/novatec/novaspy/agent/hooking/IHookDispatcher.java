package info.novatec.novaspy.agent.hooking;

import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;

/**
 * The hook dispatcher interface defines methods to add method and constructor
 * mappings and methods to dispatch the calls from the instrumented methods in
 * the target application.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public interface IHookDispatcher {

	/**
	 * Adds a method mapping to the dispatcher.
	 * 
	 * @param id
	 *            The id of the mapping.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object of this mapping.
	 */
	void addMethodMapping(long id, RegisteredSensorConfig rsc);

	/**
	 * Adds a constructor mapping to the dispatcher.
	 * 
	 * @param id
	 *            The id of the mapping.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} object of this mapping.
	 */
	void addConstructorMapping(long id, RegisteredSensorConfig rsc);

	/**
	 * Dispatches the 'before' method statement.
	 * 
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 */
	void dispatchMethodBeforeBody(long id, Object object, Object[] parameters);

	/**
	 * Dispatches the first 'after' method statement.
	 * 
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 * @param returnValue
	 *            The return value of the method.
	 */
	void dispatchFirstMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue);

	/**
	 * Dispatches the second 'after' method statement.
	 * 
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 * @param returnValue
	 *            The return value of the method.
	 */
	void dispatchSecondMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue);

	/**
	 * Dispatches the constructor of a {@link Throwable} class.
	 * 
	 * @param id
	 *            The id of the constructor creating the {@link Throwable}
	 *            object.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 * @param parameters
	 *            The parameters of the constructor.
	 */
	void dispatchConstructorOfThrowable(long id, Object exceptionObject, Object[] parameters);

	/**
	 * Dispatches the 'addCatch' statement of a method.
	 * 
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class.
	 * @param parameters
	 *            The parameters of the method.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject);

	/**
	 * Dispatches the handler of a {@link Throwable}.
	 * 
	 * @param id
	 *            The id of the method where the {@link Throwable} is handled.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchBeforeCatch(long id, Object exceptionObject);

	/**
	 * Dispatches the 'addCatch' statement of a constructor.
	 * 
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class.
	 * @param parameters
	 *            The parameters of the constructor.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchConstructorOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject);

	/**
	 * Dispatches the handler of a {@link Throwable}.
	 * 
	 * @param id
	 *            The id of the constructor where the {@link Throwable} is
	 *            handled.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchConstructorBeforeCatch(long id, Object exceptionObject);

	/**
	 * Dispatches the 'before' constructor statement.
	 * 
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 */
	void dispatchConstructorBeforeBody(long id, Object object, Object[] parameters);

	/**
	 * Dispatches the 'after' constructor statement.
	 * 
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 */
	void dispatchConstructorAfterBody(long id, Object object, Object[] parameters);

}