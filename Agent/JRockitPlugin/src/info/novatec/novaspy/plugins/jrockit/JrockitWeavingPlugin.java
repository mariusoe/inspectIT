package info.novatec.novaspy.plugins.jrockit;

import info.novatec.novaspy.agent.PicoAgent;

import com.bea.jvm.ClassLibrary;
import com.bea.jvm.ClassPreProcessor;
import com.bea.jvm.JVMFactory;

/**
 * This plugin is needed for the JRockit JVMs.
 * 
 * Use the following attribute to activate it:
 * "-Xmanagement:class=info.novatec.novaspy.plugins.jrockit.JrockitWeavingPlugin"
 * 
 * @author Patrice Bouillet
 * 
 */
public class JrockitWeavingPlugin implements ClassPreProcessor {

	/**
	 * Needed to check if we are currently in the process of checking/modifying
	 * a class file (JRockit... sigh).
	 */
	private static boolean operationInProgress = false;

	public JrockitWeavingPlugin() {
		ClassLibrary cl = JVMFactory.getJVM().getClassLibrary();
		cl.setClassPreProcessor(this);
	}

	public byte[] preProcess(ClassLoader classLoader, String className, byte[] classBytes) {
		try {
			if (!operationInProgress) {
				operationInProgress = true;
				byte[] modifiedClassBytes = PicoAgent.getInstance().inspectByteCode(classBytes, className.replaceAll("/", "."), classLoader);
				operationInProgress = false;
				if (null != modifiedClassBytes) {
					return modifiedClassBytes;
				}
			}

			return classBytes;
		} catch (Throwable ex) {
			return classBytes;
		}
	}

}
