package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

/**
 * Factory for returning the correct instance of {@link IObjectSizes} for Spring initialization. The
 * factory will check if the IBM JVM is used, and in that case provide the different
 * {@link IObjectSizes} objects that support IBM JVM object memory footprint. Further more the
 * factory will provide different instances for a 32bit and 64bit JVMs, and even check if the
 * compressed OOPs are used with 64bit, and also provide a support for them.
 * 
 * @author Ivan Senic
 * 
 */
public class ObjectSizesFactory implements FactoryBean {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getObject() throws Exception {
		boolean isIbm = System.getProperty("java.vendor").indexOf("IBM") != -1;

		boolean is64Bit = false;
		if (!isIbm) {
			is64Bit = System.getProperty("sun.arch.data.model").indexOf("64") != -1;
		} else {
			is64Bit = System.getProperty("java.fullversion").indexOf("x64") != -1;
		}

		boolean compresedOops = false;
		if (is64Bit) {
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			List<String> arguments = runtimeMXBean.getInputArguments();
			for (String argument : arguments) {
				if (argument.indexOf("+UseCompressedOops") != -1 || argument.indexOf("compressedrefs") != -1) {
					compresedOops = true;
					break;
				}
			}
		}

		if (is64Bit && !compresedOops) {
			if (isIbm) {
				return new ObjectSizes64BitsIbm();
			} else {
				return new ObjectSizes64Bits();
			}
		} else {
			if (isIbm) {
				return new ObjectSizes32BitsIbm();
			} else {
				return new ObjectSizes32Bits();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<IObjectSizes> getObjectType() {
		return IObjectSizes.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

}
