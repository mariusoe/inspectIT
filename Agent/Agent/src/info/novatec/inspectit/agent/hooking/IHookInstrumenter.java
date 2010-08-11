package info.novatec.inspectit.agent.hooking;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.hooking.impl.HookException;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;

/**
 * The hook instrumenter interface defines methods to add method and constructor hooks.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public interface IHookInstrumenter {

	/**
	 * Adds the three hooks (one before, two after) to the passed {@link CtMethod}. Afterwards, the
	 * dispatcher stores the information so that the appropriate sensor types are called whenever
	 * the method is called.
	 * 
	 * @param method
	 *            The method which is instrumented.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} class which holds the information which sensor
	 *            types are called for this method.
	 * @throws HookException
	 *             A {@link HookException} is thrown if something unexpected happens, like the
	 *             instrumentation was not completed successfully.
	 */
	void addMethodHook(CtMethod method, RegisteredSensorConfig rsc) throws HookException;

	/**
	 * Adds two hooks to the passed {@link CtConstructor}. Afterwards, the dispatcher stores the
	 * information so that the appropriate sensor types are called whenever the constructor is
	 * called.
	 * 
	 * @param constructor
	 *            The constructor which is instrumented.
	 * @param rsc
	 *            The {@link RegisteredSensorConfig} class which holds the information which sensor
	 *            types are called for this method.
	 * @throws HookException
	 *             A {@link HookException} is thrown if something unexpected happens, like the
	 *             instrumentation was not completed successfully.
	 */
	void addConstructorHook(CtConstructor constructor, RegisteredSensorConfig rsc) throws HookException;
}