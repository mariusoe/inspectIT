package de.novatec.inspectit.plugins.websphere;

import info.novatec.inspectit.agent.PicoAgent;

import java.util.logging.Logger;

import com.ibm.websphere.classloader.ClassLoaderPlugin;

/**
 * Plugin used by the Websphere Application Server to intercept the class
 * loading.
 * 
 * @author Patrice Bouillet
 * 
 */
public class WasWeavingPlugin implements ClassLoaderPlugin {

	/**
	 * The logger of this class.
	 */
	private static Logger logger = Logger.getLogger(WasWeavingPlugin.class.getName());

	/**
	 * {@inheritDoc}
	 */
	public byte[] preDefineApplicationClass(String className, byte[] byteCode) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			byte[] copy = PicoAgent.getInstance().inspectByteCode(byteCode, className, classLoader);
			if (null != copy) {
				return copy;
			} else {
				return byteCode;
			}
		} catch (Throwable ex) {
			logger.severe("Error occured while dealing with class: " + className + " " + ex.getMessage());
			return byteCode;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] preDefineRuntimeClass(String className, byte[] bytes) {
		return bytes;
	}

}
