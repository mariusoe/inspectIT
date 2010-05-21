package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IByteCodeAnalyzer;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.hooking.IHookInstrumenter;
import info.novatec.inspectit.agent.hooking.impl.HookException;
import info.novatec.inspectit.agent.hooking.impl.HookInstrumenter;
import info.novatec.inspectit.javassist.ByteArrayClassPath;
import info.novatec.inspectit.javassist.CannotCompileException;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtBehavior;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.NotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The default implementation of the {@link IByteCodeAnalyzer} interface. First
 * it tries to analyze the given byte code and collects all the methods which
 * need to be instrumented in a Map. This is done in the
 * {@link #analyze(byte[], String, ClassLoader)} method. Afterwards, the Map is
 * passed to the {@link #instrument(Map)} method which will do the
 * instrumentation.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public class ByteCodeAnalyzer implements IByteCodeAnalyzer {

	/**
	 * The implementation of the hook instrumenter.
	 */
	private final IHookInstrumenter hookInstrumenter;

	/**
	 * The implementation of the configuration storage where all definitions of
	 * the user are stored.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The class pool analyzer is used here to add the passed byte code to the
	 * class pool which is responsible for the class loader.
	 */
	private final IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * The inheritance analyzer is used here to add additional instructions to a
	 * constructor of a specified type.
	 */
	private final IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * The default constructor which accepts two parameters which are needed.
	 * 
	 * @param configurationStorage
	 *            The configuration storage reference.
	 * @param hookInstrumenter
	 *            The hook instrumenter reference.
	 * @param classPoolAnalyzer
	 *            The class pool analyzer reference.
	 * @param inheritanceAnalyzer
	 *            The inheritance analyzer reference.
	 */
	public ByteCodeAnalyzer(IConfigurationStorage configurationStorage, IHookInstrumenter hookInstrumenter, IClassPoolAnalyzer classPoolAnalyzer, IInheritanceAnalyzer inheritanceAnalyzer) {
		if (null == configurationStorage) {
			throw new IllegalArgumentException("Configuration storage cannot be null!");
		}
		if (null == hookInstrumenter) {
			throw new IllegalArgumentException("Hook instrumenter cannot be null!");
		}
		if (null == classPoolAnalyzer) {
			throw new IllegalArgumentException("Class pool analyzer cannot be null!");
		}
		if (null == inheritanceAnalyzer) {
			throw new IllegalArgumentException("Inheritance analyzer cannot be null!");
		}
		this.configurationStorage = configurationStorage;
		this.hookInstrumenter = hookInstrumenter;
		this.classPoolAnalyzer = classPoolAnalyzer;
		this.inheritanceAnalyzer = inheritanceAnalyzer;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] analyzeAndInstrument(byte[] byteCode, String className, ClassLoader classLoader) {
		// The reason to create a byte array class path here is to handle
		// classes created at runtime (reflection / byte code engineering
		// libraries etc.) and to get the real content of that class (think of
		// classes modified by other java agents before.)
		ClassPool classPool = classPoolAnalyzer.getClassPool(classLoader);
		ByteArrayClassPath classPath = new ByteArrayClassPath(className, byteCode);
		classPool.insertClassPath(classPath);

		try {
			byte[] instrumentedByteCode = null;

			// determines whether actual class is of type Throwable
			boolean isThrowable = inheritanceAnalyzer.subclassOf(className, "java.lang.Throwable", classPool);

			if (isThrowable) {
				for (Iterator iterator = configurationStorage.getExceptionSensorConfigs().iterator(); iterator.hasNext();) {
					UnregisteredSensorConfig exceptionSensorConfig = (UnregisteredSensorConfig) iterator.next();
					IMatcher matcher = exceptionSensorConfig.getMatcher();

					if (matcher.compareClassName(classLoader, className)) {
						// save the modified byte code
						instrumentedByteCode = instrumentThrowableConstructors(className, classPool);

						// remove the classpath and add the instrumented
						// bytecode of Throwable to the new classpath
						classPool.removeClassPath(classPath);
						ByteArrayClassPath cp = new ByteArrayClassPath(className, instrumentedByteCode);
						classPool.insertClassPath(cp);
						break;
					}
				}
			}

			Map behaviorToConfigMap = analyze(className, classLoader);

			if (!behaviorToConfigMap.isEmpty()) {
				instrumentedByteCode = instrument(behaviorToConfigMap);
			}

			return instrumentedByteCode;
		} catch (NotFoundException notFoundException) {
			notFoundException.printStackTrace();
			return null;
		} catch (IOException iOException) {
			iOException.printStackTrace();
			return null;
		} catch (CannotCompileException cannotCompileException) {
			cannotCompileException.printStackTrace();
			return null;
		} catch (HookException hookException) {
			hookException.printStackTrace();
			return null;
		} catch (StorageException storageException) {
			storageException.printStackTrace();
			return null;
		} finally {
			// Remove the byte array class path from the class pool. The class
			// loader now should know this class, thus it can be accessed
			// through the standard way.
			classPool.removeClassPath(classPath);
		}
	}

	/**
	 * This method is mainly used to instrument all constructors of a class of
	 * type {@link Throwable}. The constructors of the class are passed to the
	 * {@link HookInstrumenter} and additional instructions are added. After the
	 * instrumentation the modified byte code is returned.
	 * 
	 * @param className
	 *            The {@link CtClass} of type {@link Throwable}.
	 * @param classPool
	 *            The class pool where to search for.
	 * @return The modified byte code.
	 * @throws NotFoundException
	 *             Something could not be found.
	 * @throws HookException
	 *             The {@link IHookInstrumenter} generated an exception.
	 * @throws IOException
	 *             The byte code could not be generated.
	 * @throws CannotCompileException
	 *             The byte code could not be generated.
	 */
	private byte[] instrumentThrowableConstructors(String className, ClassPool classPool) throws NotFoundException, HookException, IOException, CannotCompileException {
		CtClass clazz = classPool.get(className);

		RegisteredSensorConfig rsc = new RegisteredSensorConfig();
		rsc.setConstructor(true);
		rsc.setTargetClassName(clazz.getSimpleName());
		rsc.setTargetPackageName(clazz.getPackageName());
		rsc.setModifiers(clazz.getModifiers());

		// iterate over the exception sensor types - currently there is only one
		for (Iterator configsIterator = configurationStorage.getExceptionSensorTypes().iterator(); configsIterator.hasNext();) {
			MethodSensorTypeConfig config = (MethodSensorTypeConfig) configsIterator.next();

			// TODO ET: sortMethodHooks throws a ClassCastException
			// rsc.addSensorTypeConfig(config, config.getName());
			rsc.setExceptionSensorTypeConfig(config);
		}

		hookInstrumenter.instrumentConstructorOfThrowable(clazz.getConstructors(), clazz, rsc);
		return clazz.toBytecode();
	}

	/**
	 * The analyze method will analyze the passed byte code, class name and
	 * class loader and returns a {@link Map} with all matching methods to be
	 * instrumented.
	 * 
	 * @param className
	 *            The name of the class.
	 * @param classLoader
	 *            The class loader of the passed class.
	 * @return Returns a {@link Map} with all found methods ({@link CtBehavior})
	 *         as the Key and a {@link List} of {@link UnregisteredSensorConfig}
	 *         as the value.
	 * @throws NotFoundException
	 *             Something could not be found.
	 * @throws StorageException
	 *             Sensor could not be added.
	 */
	private Map analyze(String className, ClassLoader classLoader) throws NotFoundException, StorageException {
		Map behaviorToConfigMap = new HashMap();

		// Iterating over all stored unregistered sensor configurations
		for (Iterator iterator = configurationStorage.getUnregisteredSensorConfigs().iterator(); iterator.hasNext();) {
			UnregisteredSensorConfig unregisteredSensorConfig = (UnregisteredSensorConfig) iterator.next();

			// try to match the class name first
			IMatcher matcher = unregisteredSensorConfig.getMatcher();
			if (matcher.compareClassName(classLoader, className)) {
				List behaviors;
				// differentiate between constructors and methods.
				if (unregisteredSensorConfig.isConstructor()) {
					// the constructors
					behaviors = matcher.getMatchingConstructors(classLoader, className);
				} else {
					// the methods
					behaviors = matcher.getMatchingMethods(classLoader, className);
				}
				matcher.checkParameters(behaviors);

				// iterating over all methods which passed the matcher
				for (Iterator behaviorIterator = behaviors.iterator(); behaviorIterator.hasNext();) {
					CtBehavior behavior = (CtBehavior) behaviorIterator.next();
					if (behaviorToConfigMap.containsKey(behavior)) {
						// access the already initialized list and store the
						// unregistered sensor configuration in it.
						List configs = (List) behaviorToConfigMap.get(behavior);
						configs.add(unregisteredSensorConfig);
					} else {
						// key does not exist already, thus we have to
						// create the list first.
						List configs = new ArrayList();
						configs.add(unregisteredSensorConfig);
						behaviorToConfigMap.put(behavior, configs);
					}
				}
			}
		}

		return behaviorToConfigMap;
	}

	/**
	 * Instruments the methods in the {@link Map} and creates the appropriate
	 * {@link RegisteredSensorConfig} classes.
	 * 
	 * @param methodToConfigMap
	 *            The initialized {@link Map} which is filled by the
	 *            {@link #analyze(byte[], String, ClassLoader)} method.
	 * @return Returns the instrumented byte code.
	 * @throws NotFoundException
	 *             Something could not be found.
	 * @throws HookException
	 *             The hook instrumenter generated an exception.
	 * @throws IOException
	 *             The byte code could not be generated.
	 * @throws CannotCompileException
	 *             The byte code could not be generated.
	 */
	private byte[] instrument(Map methodToConfigMap) throws NotFoundException, HookException, IOException, CannotCompileException {
		CtBehavior ctBehavior = null;
		for (Iterator iterator = methodToConfigMap.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			ctBehavior = (CtBehavior) entry.getKey();
			List configs = (List) entry.getValue();

			List parameterTypes = new ArrayList();
			CtClass[] parameterClasses = ctBehavior.getParameterTypes();
			for (int pos = 0; pos < parameterClasses.length; pos++) {
				parameterTypes.add((parameterClasses[pos]).getName());
			}

			RegisteredSensorConfig rsc = new RegisteredSensorConfig();
			rsc.setTargetPackageName(ctBehavior.getDeclaringClass().getPackageName());
			rsc.setTargetClassName(ctBehavior.getDeclaringClass().getSimpleName());
			rsc.setTargetMethodName(ctBehavior.getName());
			rsc.setParameterTypes(parameterTypes);
			rsc.setModifiers(ctBehavior.getModifiers());
			rsc.setCtBehavior(ctBehavior);

			for (Iterator configsIterator = configs.iterator(); configsIterator.hasNext();) {
				UnregisteredSensorConfig usc = (UnregisteredSensorConfig) configsIterator.next();
				String sensorName = replaceKeywords(usc.getSensorName(), ctBehavior.getDeclaringClass().getSimpleName(), ctBehavior.getName(), parameterTypes);
				rsc.addSensorTypeConfig(usc.getSensorTypeConfig(), sensorName);
				rsc.getSettings().putAll(usc.getSettings());

				if (usc.isPropertyAccess()) {
					rsc.setPropertyAccess(true);
					rsc.getPropertyAccessorList().addAll(usc.getPropertyAccessorList());
				}

				if (usc.isConstructor()) {
					rsc.setConstructor(true);
				}
			}

			// only when there is an Exception Sensor defined
			if (configurationStorage.getExceptionSensorTypes().size() > 0) {
				// iterate over the exception sensor types - currently there is
				// only one
				for (Iterator configsIterator = configurationStorage.getExceptionSensorTypes().iterator(); configsIterator.hasNext();) {
					MethodSensorTypeConfig config = (MethodSensorTypeConfig) configsIterator.next();
					// TODO ET: sortMethodHooks throws a ClassCastException
					// rsc.addSensorTypeConfig(config, config.getName());
					rsc.setExceptionSensorTypeConfig(config);
				}
			}

			if (!rsc.isConstructor()) {
				// return type only for methods available
				CtMethod ctMethod = (CtMethod) ctBehavior;
				rsc.setReturnType(ctMethod.getReturnType().getName());

				hookInstrumenter.addMethodHook(ctMethod, rsc);
			} else {
				hookInstrumenter.addConstructorHook((CtConstructor) ctBehavior, rsc);
			}
		}

		return ctBehavior.getDeclaringClass().toBytecode();
	}

	/**
	 * Replace all the place holders of the string which was set by the user in
	 * the configuration file.
	 * 
	 * @param sensorName
	 *            The name of the sensor where all reserved keywords will be
	 *            replaced.
	 * @param className
	 *            The name of the class.
	 * @param methodName
	 *            The name of the method.
	 * @param parameterTypes
	 *            A list containing the parameters of the method.
	 * @return The modified sensor name with all replaced keywords.
	 */
	private String replaceKeywords(String sensorName, String className, String methodName, List parameterTypes) {
		sensorName = sensorName.replaceAll("%classname%", className.replaceAll("\\$", "?"));
		sensorName = sensorName.replaceAll("%methodname%", methodName.replaceAll("\\$", "?"));
		if (parameterTypes.size() > 0) {
			sensorName = sensorName.replaceAll("%parametertypes%", parameterTypes.toString().replaceAll("\\$", "?"));
		} else {
			sensorName = sensorName.replaceAll("%parametertypes%", "");
		}

		return sensorName;
	}

}
