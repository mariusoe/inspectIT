package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.LoaderClassPath;
import info.novatec.inspectit.javassist.Modifier;
import info.novatec.inspectit.javassist.NotFoundException;
import info.novatec.inspectit.util.WeakList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 * This class provides some methods to help to work with the {@link ClassPool}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ClassPoolAnalyzer implements IClassPoolAnalyzer {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ClassPoolAnalyzer.class.getName());

	/**
	 * A weak list to save references to the class loaders.
	 */
	private static WeakList classLoaders = new WeakList();

	/**
	 * Contains a mapping from the {@link ClassPool} to the {@link ClassLoader} objects.
	 */
	private static Map map = new WeakHashMap();

	/**
	 * {@inheritDoc}
	 */
	public CtMethod[] getMethodsForClassName(ClassLoader classLoader, String className) {
		try {
			CtClass cc = this.getClassPool(classLoader).get(className);

			// exclude interface classes, cannot instrument them!
			if (!Modifier.isInterface(cc.getModifiers())) {
				return cc.getDeclaredMethods();
			}
		} catch (NotFoundException e) {
			LOGGER.severe("NotFoundException caught for class: " + className);
		} catch (RuntimeException e) {
			LOGGER.severe("Class which generated a runtime exception: " + className);
			LOGGER.severe(e.getMessage());
		}

		return new CtMethod[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public CtConstructor[] getConstructorsForClassName(ClassLoader classLoader, String className) {
		try {
			CtClass cc = this.getClassPool(classLoader).get(className);
			List constructorList = new ArrayList();

			// exclude interface classes, cannot instrument them!
			if (!Modifier.isInterface(cc.getModifiers())) {
				CtConstructor[] constructors = cc.getDeclaredConstructors();
				for (int i = 0; i < constructors.length; i++) {
					CtConstructor ctConstructor = constructors[i];
					// only add real constructors, no class initializers
					if (ctConstructor.isConstructor()) {
						constructorList.add(ctConstructor);
					}
				}
				return (CtConstructor[]) constructorList.toArray(new CtConstructor[constructorList.size()]);
			}
		} catch (NotFoundException e) {
			LOGGER.severe("NotFoundException caught for class: " + className);
		} catch (RuntimeException e) {
			LOGGER.severe("Class which generated a runtime exception: " + className);
			LOGGER.severe(e.getMessage());
		}

		return new CtConstructor[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassPool addClassLoader(ClassLoader classLoader) {
		if (null == classLoader) {
			return ClassPool.getDefault();
		}
		if (!classLoaders.contains(classLoader)) {
			return this.copyHierarchy(classLoader);
		}

		return (ClassPool) map.get(classLoader);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassPool getClassPool(ClassLoader classLoader) {
		if (null == classLoader) {
			return ClassPool.getDefault();
		}
		ClassPool cp = (ClassPool) map.get(classLoader);
		// Return the default classpool if we don't have the mapping yet.
		if (null == cp) {
			cp = this.addClassLoader(classLoader);
		}
		return cp;
	}

	/**
	 * Copy the hierarchy from the given classloader and build new classpool objects.
	 * 
	 * @param classLoader
	 *            The class loader.
	 * @return The newly created ClassPool referring to this class loader.
	 */
	private ClassPool copyHierarchy(ClassLoader classLoader) {
		// Check if current class loader was already seen before
		if (!classLoaders.contains(classLoader)) {
			classLoaders.add(classLoader);
		}

		ClassPool cp = null;
		if (null != classLoader.getParent() && !classLoaders.contains(classLoader.getParent())) {
			// If the class loader has got a parent one and was not seen before
			// -> initialize that one first
			cp = new ClassPool(this.copyHierarchy(classLoader.getParent()));
		} else if (null != classLoader.getParent()) {
			// Parent class loader was seen and initialized before, we only care
			// about the current one and set the parent class loader.
			cp = new ClassPool((ClassPool) map.get(classLoader.getParent()));
		} else {
			// Class loader has got no parent ( bootstrap class loader )
			cp = new ClassPool(ClassPool.getDefault());
		}

		cp.insertClassPath(new LoaderClassPath(classLoader));
		// Map the class loader to the ClassPool
		map.put(classLoader, cp);

		return cp;
	}

}
