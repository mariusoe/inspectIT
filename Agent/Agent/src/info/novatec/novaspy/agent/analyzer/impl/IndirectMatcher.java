package info.novatec.novaspy.agent.analyzer.impl;

import info.novatec.novaspy.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.novaspy.agent.analyzer.IMatchPattern;
import info.novatec.novaspy.agent.config.impl.UnregisteredSensorConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * The indirect matcher is used for a sensor configuration which contains a
 * pattern somewhere in the class name, method name or one of the parameter
 * types.
 * 
 * @author Patrice Bouillet
 * 
 */
public class IndirectMatcher extends AbstractMatcher {

	/**
	 * The {@link DirectMatcher} object is needed for Strings which are not
	 * containing a pattern. Hence a small gain in performance should be
	 * accomplished.
	 */
	private DirectMatcher directMatcher;

	/**
	 * The pattern object of the class name.
	 */
	private IMatchPattern classNamePattern = null;

	/**
	 * The pattern object of the method name.
	 */
	private IMatchPattern methodNamePattern = null;

	/**
	 * All patterns of the parameters. Only maps those parameters which are
	 * really a pattern.
	 */
	private Map parameterTypesPatterns = new HashMap();

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
	public IndirectMatcher(IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig) {
		super(classPoolAnalyzer, unregisteredSensorConfig);

		directMatcher = new DirectMatcher(classPoolAnalyzer, unregisteredSensorConfig);

		if (SimpleMatchPattern.isPattern(unregisteredSensorConfig.getTargetClassName())) {
			classNamePattern = new SimpleMatchPattern(unregisteredSensorConfig.getTargetClassName());
		}

		if (SimpleMatchPattern.isPattern(unregisteredSensorConfig.getTargetMethodName())) {
			methodNamePattern = new SimpleMatchPattern(unregisteredSensorConfig.getTargetMethodName());
		}

		if (null != unregisteredSensorConfig.getParameterTypes()) {
			Iterator i = unregisteredSensorConfig.getParameterTypes().iterator();
			while (i.hasNext()) {
				String parameter = (String) i.next();
				if (SimpleMatchPattern.isPattern(parameter)) {
					parameterTypesPatterns.put(parameter, new SimpleMatchPattern(parameter));
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException {
		if (null == classNamePattern) {
			return directMatcher.compareClassName(classLoader, className);
		}

		return classNamePattern.match(className);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMatchingMethods(ClassLoader classLoader, String className) throws NotFoundException {
		if (null == methodNamePattern) {
			return directMatcher.getMatchingMethods(classLoader, className);
		}

		CtMethod[] methods = classPoolAnalyzer.getMethodsForClassName(classLoader, className);

		if ((null != methods) && (methods.length > 0)) {
			List matchingMethods = new ArrayList();

			for (int i = 0; i < methods.length; i++) {
				CtMethod method = methods[i];
				// skip abstract and native methods
				if (!Modifier.isAbstract(method.getModifiers()) && !Modifier.isNative(method.getModifiers())) {
					if (methodNamePattern.match(method.getName())) {
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
		// no pattern allowed for the constructor, has to be specified directly.
		// That is to disallow something like *init* (which would include the
		// constructor).
		if (null != methodNamePattern) {
			return Collections.EMPTY_LIST;
		}

		return directMatcher.getMatchingConstructors(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkParameters(List methods) throws NotFoundException {
		if (!unregisteredSensorConfig.isIgnoreSignature()) {
			List parameterTypes = unregisteredSensorConfig.getParameterTypes();
			if (0 == parameterTypesPatterns.size()) {
				directMatcher.checkParameters(methods);
			} else if (null != parameterTypes) {
				for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
					CtBehavior behaviour = (CtBehavior) iterator.next();
					// get all the parameter types from the behaviour
					CtClass[] ctClasses = behaviour.getParameterTypes();

					// simple check if the parameter count is equal
					if (parameterTypes.size() == ctClasses.length) {
						// compare every parameter definition
						for (int i = 0; i < parameterTypes.size(); i++) {
							if (parameterTypesPatterns.containsKey(parameterTypes.get(i))) {
								// Parameter definition is a pattern
								if (!((IMatchPattern) parameterTypesPatterns.get(parameterTypes.get(i))).match(ctClasses[i].getName())) {
									iterator.remove();
								}
							} else if (!parameterTypes.get(i).equals(ctClasses[i].getName())) {
								iterator.remove();
							}
						}
					} else {
						iterator.remove();
					}
				}
			}
		}
	}
}
