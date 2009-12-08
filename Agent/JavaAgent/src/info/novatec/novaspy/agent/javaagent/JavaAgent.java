package info.novatec.novaspy.agent.javaagent;

import info.novatec.novaspy.agent.PicoAgent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

/**
 * The JavaAgent is used since Java 5.0 to instrument classes before they are
 * actually loaded by the VM.
 * 
 * This method is used by specifying the -javaagent attribute on the command
 * line. Example: <code>-javaagent:novaspy-javaagent.jar</code>
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
	 * In case that multiple classes are loaded at the same time, which happens
	 * in some cases, even though the JVM specification prohibits that (the case
	 * at hand was starting ant)
	 */
	private static volatile boolean operationInProgress = false;

	/**
	 * The premain method will be executed before anything else.
	 * 
	 * @param agentArgs
	 *            Some arguments.
	 * @param inst
	 *            The instrumentation instance is used to add a transformer
	 *            which will do the actual instrumentation.
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new JavaAgent());
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] transform(final ClassLoader classLoader, final String className, final Class<?> clazz, final ProtectionDomain pd, final byte[] data) throws IllegalClassFormatException {
		try {
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
}
