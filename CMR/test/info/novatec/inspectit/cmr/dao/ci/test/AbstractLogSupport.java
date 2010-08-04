package info.novatec.inspectit.cmr.dao.ci.test;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;

/**
 * This abstract class provides general logging support for the test classes of
 * the Configuration Interface.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public abstract class AbstractLogSupport extends AbstractTransactionalTestNGSpringContextTests {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractLogSupport.class);

	/**
	 * Name and path of the log4j configuration file.
	 */
	private static final String LOG4J_FILE = "/config/log4j.properties";

	@BeforeSuite
	public void initLogging() {
		// Initialize log4j system
		try {
			Properties p = new Properties();
			p.load(AbstractLogSupport.class.getResourceAsStream(LOG4J_FILE));
			PropertyConfigurator.configure(p);
		} catch (IOException e) {
			LOGGER.error("Could not load log4j.properties file: " + e.getMessage());
		}

		LOGGER.info("Log4j was initialized properly!");
		LOGGER.info("===============================");
	}
}
