package info.novatec.novaspy.agent.analyzer.impl;

import info.novatec.novaspy.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.novaspy.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.novaspy.agent.analyzer.IMatcher;
import info.novatec.novaspy.agent.config.impl.UnregisteredSensorConfig;

import java.util.Iterator;
import java.util.List;

import javassist.CtClass;
import javassist.NotFoundException;

/**
 * The interface matcher implementation is used to check if the class name of
 * the configuration is equal to one of the implemented interfaces of the passed
 * class. All of the calls to these methods are mainly delegated to either an
 * {@link DirectMatcher} or an {@link IndirectMatcher}, depending if the
 * configuration is virtual (contains a pattern).
 * 
 * @author Patrice Bouillet
 * 
 */
public class InterfaceMatcher extends AbstractMatcher {

	/**
	 * The inheritance checker used to check if an interface matches.
	 */
	private final IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * The {@link IMatcher} delegator object to route the calls of all methods
	 * to.
	 */
	private IMatcher delegateMatcher;

	/**
	 * The only constructor which needs a reference to the
	 * {@link UnregisteredSensorConfig} instance of the corresponding
	 * configuration.
	 * 
	 * @param inheritanceAnalyzer
	 *            The inheritance analyzer.
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 * @param unregisteredSensorConfig
	 *            The sensor configuration.
	 * @see AbstractMatcher
	 */
	public InterfaceMatcher(IInheritanceAnalyzer inheritanceAnalyzer, IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig) {
		super(classPoolAnalyzer, unregisteredSensorConfig);

		this.inheritanceAnalyzer = inheritanceAnalyzer;

		if (unregisteredSensorConfig.isVirtual()) {
			delegateMatcher = new IndirectMatcher(classPoolAnalyzer, unregisteredSensorConfig);
		} else {
			delegateMatcher = new DirectMatcher(classPoolAnalyzer, unregisteredSensorConfig);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean compareClassName(final ClassLoader classLoader, final String className) throws NotFoundException {
		Iterator i = inheritanceAnalyzer.getInterfaceIterator(classLoader, className);
		while (i.hasNext()) {
			CtClass clazz = (CtClass) i.next();
			if (delegateMatcher.compareClassName(classLoader, clazz.getName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List getMatchingMethods(final ClassLoader classLoader, final String className) throws NotFoundException {
		return delegateMatcher.getMatchingMethods(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException {
		return delegateMatcher.getMatchingConstructors(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void checkParameters(final List methods) throws NotFoundException {
		delegateMatcher.checkParameters(methods);
	}

}
