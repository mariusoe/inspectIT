package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

/**
 * Factory for returning the correct instance of {@link IObjectSizes} for Spring initialization.
 * Works only when Sun VM is used.
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
		boolean is64Bit = System.getProperty("sun.arch.data.model").indexOf("64") != -1;
		if (is64Bit) {
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			List<String> arguments = runtimeMXBean.getInputArguments();
			boolean compresedOops = false;
			for (String argument : arguments) {
				if (argument.indexOf("UseCompressedOops") != -1) {
					compresedOops = true;
					break;
				}
			}
			if (compresedOops) {
				return new ObjectSizes32Bits();
			} else {
				return new ObjectSizes64Bits();
			}
		} else {
			return new ObjectSizes32Bits();
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
