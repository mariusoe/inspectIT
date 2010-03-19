package info.novatec.inspectit.cmr;

import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.versioning.FileBasedVersioningServiceImpl;
import info.novatec.inspectit.versioning.IVersioningService;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

/**
 * Main class of the Central Measurement Repository. The main method is used to
 * start the application.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class CMR {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CMR.class);

	/**
	 * Name of the log4j configuration file.
	 */
	private static final String LOG4J_FILE = "log4j.properties";

	/**
	 * The spring bean factory to get the registered beans.
	 */
	private static BeanFactory beanFactory;

	/**
	 * This class will start the Repository.
	 */
	private CMR() {
		
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Initializing Spring...");
		}

		BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("ctx");
		beanFactory = beanFactoryReference.getFactory();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Spring successfully initialized");
		}
		beanFactoryLocator.useBeanFactory("jetty");

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Spring WebApplicationContext successfully initialized");
		}
		
		if (LOGGER.isInfoEnabled()) {
			IVersioningService versioning = (IVersioningService) getBeanFactory().getBean("versioning");
			String currentVersion = "n/a";
			try {
				currentVersion = versioning.getVersion();
			} catch (IOException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Versioning information could not be read", e);
				}
			}
			LOGGER.info("Starting CMR in version "+currentVersion);
		}
	}

	/**
	 * Main method of class.
	 * 
	 * @param args
	 *            The arguments.
	 */
	public static void main(String[] args) {
		// Initialize log4j system
		URL url = ClassLoader.getSystemResource("config" + File.separator + LOG4J_FILE);
		if (null != url) {
			PropertyConfigurator.configure(url);
		} else {
			String inspectitConfig = System.getProperty("inspectit.config");
			String pathToLog4jConfig;
			if (null != inspectitConfig && !"".equals(inspectitConfig.trim())) {
				pathToLog4jConfig = inspectitConfig + File.separator + LOG4J_FILE;
			} else {
				pathToLog4jConfig = "config" + File.separator + LOG4J_FILE;
			}
			PropertyConfigurator.configureAndWatch(pathToLog4jConfig);
		}

		long start = System.nanoTime();

		LOGGER.info("Central Measurement Repository is starting up!");
		LOGGER.info("==============================================");

		new CMR();

		LOGGER.info("CMR started in " + Converter.nanoToMilliseconds(System.nanoTime() - start) + " ms");
	}

	/**
	 * Returns the spring bean factory.
	 * 
	 * @return The spring bean factory.
	 */
	public static BeanFactory getBeanFactory() {
		return beanFactory;
	}

}
