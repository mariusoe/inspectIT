package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * This implementation compares directly the class name, method name and
 * parameter types. 'Direct' denotes comparing them by calling the
 * <code>equals</code> method of the String objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public class DirectMatcher extends AbstractMatcher {

	/**
	 * The only constructor which needs a reference to the
	 * {@link UnregisteredSensorConfig} instance of the corresponding
	 * configuration.
	 * 
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 * @param unregisteredSensorConfig
	 *            The sensor configuration.
	 * @see AbstractMatcher
	 */
	public DirectMatcher(IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig) {
		super(classPoolAnalyzer, unregisteredSensorConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException {
		return unregisteredSensorConfig.getTargetClassName().equals(className);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMatchingMethods(ClassLoader classLoader, String className) throws NotFoundException {
		CtMethod[] methods = classPoolAnalyzer.getMethodsForClassName(classLoader, className);

		if (methods.length > 0) {
			List matchingMethods = new ArrayList();

			CtMethod method;
			for (int i = 0; i < methods.length; i++) {
				method = methods[i];
				// skip abstract and native methods
				if (!Modifier.isAbstract(method.getModifiers()) && !Modifier.isNative(method.getModifiers())) {
					if (method.getName().equals(unregisteredSensorConfig.getTargetMethodName())) {
						matchingMethods.add(method);
					}
				}
			}

			return matchingMethods;
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException {
		CtConstructor[] constructors = classPoolAnalyzer.getConstructorsForClassName(classLoader, className);

		if (constructors.length > 0) {
			// if the name of the target method name is '<init>', every
			// constructor will be added and checked
			if ("<init>".equals(unregisteredSensorConfig.getTargetMethodName())) {
				List constructorList = Arrays.asList(constructors);
				// we have to create a new arraylist here, as the list created
				// the line above does not support the remove operator in the
				// returned iterator...
				return new ArrayList(constructorList);
			}
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkParameters(List methods) throws NotFoundException {
		if (!unregisteredSensorConfig.isIgnoreSignature()) {
			List parameterTypes = unregisteredSensorConfig.getParameterTypes();
			for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
				CtBehavior behaviour = (CtBehavior) iterator.next();

				// get all the parameter types from the method
				CtClass[] args = behaviour.getParameterTypes();

				// simple check if the parameter count is equal
				if (null != parameterTypes && parameterTypes.size() == args.length) {
					// compare every parameter definition
					for (int i = 0; i < parameterTypes.size(); i++) {
						if (!parameterTypes.get(i).equals(args[i].getName())) {
							iterator.remove();
							i = parameterTypes.size();
						}
					}
				} else if (args.length >= 0) {
					iterator.remove();
				}
			}
		}
	}

}
