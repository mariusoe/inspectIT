package info.novatec.inspectit.cmr.test;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.BeforeSuite;

/**
 * This abstract class provides general logging support for the test classes that need normal spring
 * context.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractTestNGLogSupport {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractTestNGLogSupport.class);

	/**
	 * Name and path of the log4j configuration file.
	 */
	private static final String LOG4J_FILE = "/config/log4j.properties";

	/**
	 * Init logging.
	 */
	@BeforeSuite
	public void initLogging() {
		// Initialize log4j system
		try {
			Properties p = new Properties();
			p.load(AbstractTestNGLogSupport.class.getResourceAsStream(LOG4J_FILE));
			PropertyConfigurator.configure(p);
		} catch (IOException e) {
			LOGGER.error("Could not load log4j.properties file: " + e.getMessage());
		}
	}
}
