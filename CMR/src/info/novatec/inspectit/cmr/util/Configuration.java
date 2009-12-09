package info.novatec.inspectit.cmr.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Class to set all configuration parameters for the server.
 * 
 * @author Patrice Bouillet
 * 
 */
public class Configuration implements InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(Configuration.class);

	/**
	 * Defines the enhanced invocation storage mode.
	 */
	private boolean enhancedInvocationStorageMode = false;

	/**
	 * @param enhancedInvocationStorageMode
	 *            the enhancedInvocationStorageMode to set
	 */
	public void setEnhancedInvocationStorageMode(boolean enhancedInvocationStorageMode) {
		this.enhancedInvocationStorageMode = enhancedInvocationStorageMode;
	}

	/**
	 * @return the enhancedInvocationStorageMode
	 */
	public boolean isEnhancedInvocationStorageMode() {
		return enhancedInvocationStorageMode;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Starting configuration module...");
			LOGGER.info("||-Enhanced invocation storage mode: " + enhancedInvocationStorageMode);
		}
	}

}
