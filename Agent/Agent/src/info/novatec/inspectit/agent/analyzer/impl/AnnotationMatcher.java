package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.NotFoundException;
import info.novatec.inspectit.javassist.bytecode.AnnotationsAttribute;
import info.novatec.inspectit.javassist.bytecode.AttributeInfo;
import info.novatec.inspectit.javassist.bytecode.annotation.Annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This matcher filers that classes and methods based on the annotation class name defined in the
 * {@link UnregisteredSensorConfig}. If the annotation supplied is targeting a Class, then all
 * methods that are provided by delegate matchers of a Class that has annotation will be
 * instrumented. Otherwise, if the Annotation is targeting the method, only methods that have this
 * annotation will be instrumented.
 * 
 * @author Ivan Senic
 * 
 */
public class AnnotationMatcher extends AbstractMatcher {

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
		CtClass clazz = classPoolAnalyzer.getClassPool(classLoader).get(className);
		List matchingMethods = delegateMatcher.getMatchingMethods(classLoader, className);

		List classAttributesList = clazz.getClassFile().getAttributes();
		boolean classHasAnnotation = checkForAnnotation(classAttributesList, unregisteredSensorConfig.getAnnotationClassName());

		if (!classHasAnnotation) {
			List notMatchingMethods = null;
			Iterator iterator = matchingMethods.iterator();
			while (iterator.hasNext()) {
				CtMethod method = (CtMethod) iterator.next();
				List methodAttributesList = method.getMethodInfo().getAttributes();
				boolean methodHasAnnotation = checkForAnnotation(methodAttributesList, unregisteredSensorConfig.getAnnotationClassName());
				if (!methodHasAnnotation) {
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
		CtClass clazz = classPoolAnalyzer.getClassPool(classLoader).get(className);
		List matchingConstructors = delegateMatcher.getMatchingConstructors(classLoader, className);

		List classAttributesList = clazz.getClassFile().getAttributes();
		boolean classHasAnnotation = checkForAnnotation(classAttributesList, unregisteredSensorConfig.getAnnotationClassName());

		if (!classHasAnnotation) {
			List notMatchingConstructors = null;
			Iterator iterator = matchingConstructors.iterator();
			while (iterator.hasNext()) {
				CtConstructor constructor = (CtConstructor) iterator.next();
				List constructorAttributesList = constructor.getMethodInfo().getAttributes();
				boolean constructorHasAnnotation = checkForAnnotation(constructorAttributesList, unregisteredSensorConfig.getAnnotationClassName());
				if (!constructorHasAnnotation) {
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
	 * Checks if in the list of {@link AttributeInfo} objects exists any
	 * {@link AnnotationsAttribute} object that has information existence of the wanted annotation.
	 * Note that the attribute list should be acquired by {@link ClassFile#getAttributes()},
	 * {@link MethodInfo#getAttributes()} or {@link FieldInfo#getAttributes()} methods.
	 * 
	 * @param attributesList
	 *            List of attributes.
	 * @param annotationClassName
	 *            Name of the annotation to find.
	 * @return True if annotation could be located, false otherwise.
	 */
	private boolean checkForAnnotation(List attributesList, String annotationClassName) {
		for (int i = 0; i < attributesList.size(); i++) {
			AttributeInfo attributeInfo = (AttributeInfo) attributesList.get(i);
			if (attributeInfo instanceof AnnotationsAttribute) {
				AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) attributeInfo;
				Annotation[] annotations = annotationsAttribute.getAnnotations();
				for (int j = 0; j < annotations.length; j++) {
					if (annotations[j].getTypeName().equals(annotationClassName)) {
						return true;
					}
				}
				break;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkParameters(List methods) throws NotFoundException {
		delegateMatcher.checkParameters(methods);
	}

}
