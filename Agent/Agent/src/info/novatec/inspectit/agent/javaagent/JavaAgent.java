package info.novatec.inspectit.agent.javaagent;

import info.novatec.inspectit.agent.PicoAgent;
import info.novatec.inspectit.javassist.ClassPool;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * The JavaAgent is used since Java 5.0 to instrument classes before they are actually loaded by the
 * VM.
 * 
 * This method is used by specifying the -javaagent attribute on the command line. Example:
 * <code>-javaagent:inspecit-agent.jar</code>
 * 
 * @author Patrice Bouillet
 * 
 */
public class JavaAgent implements ClassFileTransformer {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(JavaAgent.class.getName());

	/**
	 * In case that multiple classes are loaded at the same time, which happens in some cases, even
	 * though the JVM specification prohibits that (the case at hand was starting ant).
	 */
	private static volatile boolean operationInProgress = false;

	/**
	 * The reference to the instrumentation class.
	 */
	private static Instrumentation instrumentation;

	/**
	 * Defines if we can instrument core classes.
	 */
	private static boolean instrumentCoreClasses = false;

	/**
	 * The premain method will be executed before anything else.
	 * 
	 * @param agentArgs
	 *            Some arguments.
	 * @param inst
	 *            The instrumentation instance is used to add a transformer which will do the actual
	 *            instrumentation.
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;

		LOGGER.info("inspectIT Agent: Starting initialization...");
		checkForCorrectSetup();

		// Starting up the real agent
		PicoAgent.getInstance();
		LOGGER.info("inspectIT Agent: Initialization complete...");

		// now we are analysing the already loaded classes by the jvm to instrument those classes,
		// too
		analyzeAlreadyLoadedClasses();
		inst.addTransformer(new JavaAgent());
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain pd, byte[] data) throws IllegalClassFormatException {
		try {
			// early return if some conditions fail
			if (null == data || data.length == 0 || null == className || "".equals(className)) {
				// - no data = we cannot construct the class and analyze it
				// - no class name = we don't know how the name of the class is and so the whole
				// analysis will fail
				return data;
			}

			// skip analyzing if we cannot instrument core classes.
			if (instrumentCoreClasses == false & null == classLoader) {
				return data;
			}

			// now the real inspectit agent will handle this class
			if (!operationInProgress) {
				operationInProgress = true;
				String modifiedClassName = className.replaceAll("/", ".");
				byte[] instrumentedData = PicoAgent.getInstance().inspectByteCode(data, modifiedClassName, classLoader);
				operationInProgress = false;
				return instrumentedData;
			}
			// LOGGER.severe("Parallel loading of classes: Skipping class "+className);
			return data;
		} catch (Throwable ex) {
			LOGGER.severe("Error occured while dealing with class: " + className + " " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks for the correct setup of the jvm parameters or tries to append the inspectit agent to
	 * the bootstrap class loader search automatically (Java 6+ required).
	 */
	private static void checkForCorrectSetup() {
		try {
			// we can utilize the mechanism to add the inspectit-agent to the bootstrap classloader
			// through the instrumentation api.
			Method append = instrumentation.getClass().getDeclaredMethod("appendToBootstrapClassLoaderSearch", JarFile.class);
			CodeSource cs = JavaAgent.class.getProtectionDomain().getCodeSource();
			if (null != cs) {
				LOGGER.info("inspectIT Agent: Advanced instrumentation capabilities detected...");
				JarFile jarFile = new JarFile(cs.getLocation().getFile());
				append.setAccessible(true);
				append.invoke(instrumentation, jarFile);

				instrumentCoreClasses = true;
			} else {
				LOGGER.info("inspectIT Agent: Advanced instrumentation capabilities not detected due to missing code source...");
			}
		} catch (NoSuchMethodException e) {
			LOGGER.info("inspectIT Agent: Advanced instrumentation capabilities not detected...");
		} catch (SecurityException e) {
			LOGGER.info("inspectIT Agent: Advanced instrumentation capabilities not detected due to security constraints...");
		} catch (Exception e) {
			LOGGER.severe("Something unexpected happened while trying to get advanced instrumentation capabilities!");
			e.printStackTrace();
		}
		if (!instrumentCoreClasses) {
			// 2. try
			// find out if the bootclasspath option is set
			List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
			for (String arg : inputArgs) {
				if (arg.contains("Xbootclasspath") && arg.contains("inspectit-agent.jar")) {
					instrumentCoreClasses = true;
					LOGGER.info("inspectIT Agent: Xbootclasspath setting found, activating core class instrumentation...");
					break;
				}
			}
		}
	}

	/**
	 * Analyzes all the classes which are already loaded by the jvm. This only works if the
	 * -Xbootclasspath option is being set in addition as we are instrumenting core classes which
	 * are directly connected to the bootstrap classloader.
	 */
	private static void analyzeAlreadyLoadedClasses() {
		try {
			if (instrumentation.isRedefineClassesSupported()) {
				if (instrumentCoreClasses) {
					for (Class<?> loadedClass : instrumentation.getAllLoadedClasses()) {
						String clazzName = loadedClass.getCanonicalName();
						if (null != clazzName) {
							try {
								clazzName = getClassNameForJavassist(loadedClass);
								byte[] modified = PicoAgent.getInstance().inspectByteCode(null, clazzName, loadedClass.getClassLoader());
								if (null != modified) {
									ClassDefinition classDefinition = new ClassDefinition(loadedClass, modified);
									instrumentation.redefineClasses(new ClassDefinition[] {classDefinition});
								}
							} catch (ClassNotFoundException e) {
								LOGGER.severe(e.getMessage());
							} catch (UnmodifiableClassException e) {
								LOGGER.severe(e.getMessage());
							}
						}
					}
					LOGGER.info("inspectIT Agent: Instrumentation of core classes finished...");
				} else {
					LOGGER.info("inspectIT Agent: Core classes cannot be instrumented, please add -Xbootclasspath/a:<path_to_agent.jar> to the JVM parameters!");
				}
			} else {
				LOGGER.info("Redefinition of Classes is not support in this JVM!");
			}
		} catch (Throwable t) {
			LOGGER.severe("The process of class redefinitions produced an error: " + t.getMessage());
			LOGGER.severe("If you are running on an IBM JVM, please ignore this error as the JVM does not support this feature!");
			LOGGER.throwing(JavaAgent.class.getCanonicalName(), "analyzeAlreadyLoadedClasses", t);
		}
	}

	/**
	 * See {@link ClassPool#get(String)} why it is needed to replace the '.' with '$' for inner
	 * classes.
	 * 
	 * @param clazz
	 *            The class to get the name from.
	 * @return the name to be passed to javassist.
	 */
	private static String getClassNameForJavassist(Class<?> clazz) {
		String clazzName = clazz.getCanonicalName();
		while (null != clazz.getEnclosingClass()) {
			clazz = clazz.getEnclosingClass();
		}

		if (!clazzName.equals(clazz.getCanonicalName())) {
			String enclosingClasses = clazzName.substring(clazz.getCanonicalName().length());
			enclosingClasses = enclosingClasses.replaceAll("\\.", "\\$");
			clazzName = clazz.getCanonicalName() + enclosingClasses;
		}

		return clazzName;
	}

}
