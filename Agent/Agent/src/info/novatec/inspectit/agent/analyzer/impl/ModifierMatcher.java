package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.Modifier;
import info.novatec.inspectit.javassist.NotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The modifier matcher is used to check if the modifier of class methods correspond to the modifier
 * value set in the {@link UnregisteredSensorConfig}. Furthermore, all the call to this class are
 * delegated to delegate matcher specified in the constructor.
 * 
 * @author Ivan Senic
 * 
 */
public class ModifierMatcher extends AbstractMatcher {

	/**
	 * Flag marker for DEFAULT modifier. Needed to keep track if the default is also specified in
	 * the list of modifiers.
	 */
	public static final int DEFAULT = 0x8000;

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
	public ModifierMatcher(IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig, IMatcher delegateMatcher) {
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
		List matchingMethods = delegateMatcher.getMatchingMethods(classLoader, className);
		List notMatchingMethods = null;

		Iterator iterator = matchingMethods.iterator();
		while (iterator.hasNext()) {
			CtMethod method = (CtMethod) iterator.next();
			boolean modiferMatched = false;
			if (method.getModifiers() == unregisteredSensorConfig.getModifiers()) {
				modiferMatched = true;
			} else if (Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			} else if (Modifier.isProtected(method.getModifiers()) && Modifier.isProtected(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			} else if (Modifier.isPrivate(method.getModifiers()) && Modifier.isPrivate(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			} else if (Modifier.isPackage(method.getModifiers()) && isDefault(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			}

			if (!modiferMatched) {
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
				// if list can not perform remove do it manually
				List returnList = new ArrayList();
				returnList.addAll(matchingMethods);
				returnList.removeAll(notMatchingMethods);
				return returnList;
			}
		}

		return matchingMethods;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException {
		List matchingConstructors = delegateMatcher.getMatchingConstructors(classLoader, className);
		List notMatchingConstructors = null;

		Iterator iterator = matchingConstructors.iterator();
		while (iterator.hasNext()) {
			CtConstructor constructor = (CtConstructor) iterator.next();
			boolean modiferMatched = false;
			if (constructor.getModifiers() == unregisteredSensorConfig.getModifiers()) {
				modiferMatched = true;
			} else if (Modifier.isPublic(constructor.getModifiers()) && Modifier.isPublic(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			} else if (Modifier.isProtected(constructor.getModifiers()) && Modifier.isProtected(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			} else if (Modifier.isPrivate(constructor.getModifiers()) && Modifier.isPrivate(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			} else if (Modifier.isPackage(constructor.getModifiers()) && isDefault(unregisteredSensorConfig.getModifiers())) {
				modiferMatched = true;
			}

			if (!modiferMatched) {
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
				// if list can not perform remove do it manually
				List returnList = new ArrayList();
				returnList.addAll(matchingConstructors);
				returnList.removeAll(notMatchingConstructors);
				return returnList;
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

	/**
	 * Returns if the DEFAULT modifier bit is present in given modifier int value.
	 * 
	 * @param mod
	 *            Modifier as int value.
	 * @return True if DEFAULT modifier is present.
	 */
	public static boolean isDefault(int mod) {
		return (mod & DEFAULT) != 0;
	}
}
