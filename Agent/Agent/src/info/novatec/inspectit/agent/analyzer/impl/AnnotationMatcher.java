package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This matcher filers that classes and methods based on the annotation class name defined in the
 * {@link UnregisteredSensorConfig}. If the annotation supplied is targeting a Class, then all
 * methods that are provided by deletage matchers of a Class that has annotation will be
 * instrumented. Otherwise, if the Annotation is targeting the method, only methods that have this
 * annotation will be instrumented.
 * 
 * @author Ivan Senic
 * 
 */
public class AnnotationMatcher extends AbstractMatcher {
	
	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AnnotationMatcher.class.getName());

	/**
	 * Set of annotation names that have already been logged to the user.
	 */
	private static Set loggedUnavailableAnnotations = new HashSet();
	
	/**
	 * The {@link IMatcher} delegator object to route the calls of all methods to.
	 */
	private IMatcher delegateMatcher;
	
	/**
	 * The only constructor which needs a reference to the {@link UnregisteredSensorConfig} instance
	 * of the corresponding configuration.
	 * 
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 * @param unregisteredSensorConfig
	 *            The sensor configuration.
	 * @param delegateMatcher
	 *            The {@link IMatcher} delegator object to route the calls of all methods to.
	 * @see AbstractMatcher
	 */
	public AnnotationMatcher(IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig, IMatcher delegateMatcher) {
		super(classPoolAnalyzer, unregisteredSensorConfig);

		this.delegateMatcher = delegateMatcher;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException {
		return delegateMatcher.compareClassName(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMatchingMethods(ClassLoader classLoader, String className) throws NotFoundException {
		Class annotationClass;
		try {
			annotationClass = classLoader.loadClass(unregisteredSensorConfig.getAnnotationClassName());
		} catch (ClassNotFoundException exception) {
			if (loggedUnavailableAnnotations.add(unregisteredSensorConfig.getAnnotationClassName())) {
				LOGGER.warning("Annotation " + unregisteredSensorConfig.getAnnotationClassName() + " can not be found on the classpath. Underlying instrumentation will be skipped.");
			}
			return Collections.EMPTY_LIST;
		}

		CtClass clazz = classPoolAnalyzer.getClassPool(classLoader).get(className);
		List matchingMethods = delegateMatcher.getMatchingMethods(classLoader, className);

		if (!clazz.hasAnnotation(annotationClass)) {
			List notMatchingMethods = null;
			Iterator iterator = matchingMethods.iterator();
			while (iterator.hasNext()) {
				CtMethod method = (CtMethod) iterator.next();
				if (!method.hasAnnotation(annotationClass)) {
					if (null == notMatchingMethods) {
						notMatchingMethods = new ArrayList();
					}
					notMatchingMethods.add(method);
				}
			}
			if (null != notMatchingMethods) {
				try {
					matchingMethods.removeAll(notMatchingMethods);
				} catch (UnsupportedOperationException exception) {
					// list not supporting remove, do manually
					List returnList = new ArrayList();
					returnList.addAll(matchingMethods);
					returnList.removeAll(notMatchingMethods);
					return returnList;
				}
			}
		}

		return matchingMethods;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException {
		Class annotationClass;
		try {
			annotationClass = classLoader.loadClass(unregisteredSensorConfig.getAnnotationClassName());
		} catch (ClassNotFoundException exception) {
			if (loggedUnavailableAnnotations.add(unregisteredSensorConfig.getAnnotationClassName())) {
				LOGGER.warning("Annotation " + unregisteredSensorConfig.getAnnotationClassName() + " can not be found on the classpath. Underlying instrumentation will be skipped.");
			}
			return Collections.EMPTY_LIST;
		}

		CtClass clazz = classPoolAnalyzer.getClassPool(classLoader).get(className);
		List matchingConstructors = delegateMatcher.getMatchingConstructors(classLoader, className);

		if (!clazz.hasAnnotation(annotationClass)) {
			List notMatchingConstructors = null;
			Iterator iterator = matchingConstructors.iterator();
			while (iterator.hasNext()) {
				CtConstructor constructor = (CtConstructor) iterator.next();
				if (!constructor.hasAnnotation(annotationClass)) {
					if (null == notMatchingConstructors) {
						notMatchingConstructors = new ArrayList();
					}
					notMatchingConstructors.add(constructor);
				}
			}
			if (null != notMatchingConstructors) {
				try {
					matchingConstructors.removeAll(notMatchingConstructors);
				} catch (UnsupportedOperationException exception) {
					// list not supporting remove, do manually
					List returnList = new ArrayList();
					returnList.addAll(matchingConstructors);
					returnList.removeAll(notMatchingConstructors);
					return returnList;
				}
			}
		}

		return matchingConstructors;
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkParameters(List methods) throws NotFoundException {
		delegateMatcher.checkParameters(methods);
	}

}
