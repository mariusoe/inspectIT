package info.novatec.inspectit.agent.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * This abstract class is used if the logging level needs to be changed. The
 * default of {@link Level#INFO} is most of the time not used.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractLogSupport extends MockInit {

	/**
	 * The temporary log file.
	 */
	private File file;

	/**
	 * Initializes the formatter. This method is called from the TestNG driver
	 * and should not be called by the test implementations.
	 * 
	 * @throws Exception
	 */
	@BeforeClass(alwaysRun = true)
	public final void initFormatter() throws Exception {
		file = File.createTempFile("log", "");
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		writer.println("handlers= java.util.logging.ConsoleHandler");
		Level level = getLogLevel();
		if (null == level) {
			level = Level.OFF;
		}
		writer.println(".level= " + level);
		writer.println("java.util.logging.ConsoleHandler.level = ALL");
		writer.println("java.util.logging.ConsoleHandler.formatter = info.novatec.inspectit.util.MessageFormatFormatter");
		writer.flush();
		writer.close();

		System.setProperty("java.util.logging.config.file", file.getAbsolutePath());
	}

	/**
	 * Needs to be implemented by all subclasses and returns the log level the
	 * test is using.
	 * 
	 * @return The log level.
	 */
	protected abstract Level getLogLevel();

	/**
	 * Deletes the temporary log file if it exists.
	 */
	@AfterClass(alwaysRun = true)
	public final void deleteFile() {
		if (null != file) {
			file.delete();
		}
	}

}
