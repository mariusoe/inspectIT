package info.novatec.inspectit.agent.config.impl;

import info.novatec.inspectit.agent.config.IConfigurationReader;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.ParserException;
import info.novatec.inspectit.agent.config.PriorityEnum;
import info.novatec.inspectit.agent.config.StorageException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This config reader class reads simple config files. Simple in the way as you
 * don't need any additional java libraries and every statement is in one line.
 * 
 * @author Patrice Bouillet
 * 
 */
public class FileConfigurationReader implements IConfigurationReader {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(FileConfigurationReader.class.getName());

	/**
	 * The configuration storage implementation. Used to store the information
	 * to.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The name of the property (directory) of the configuration.
	 */
	private static final String CONFIGURATION_PROPERTY = "inspectit.config";

	/**
	 * The name of the configuration file.
	 */
	private static final String CONFIGURATION_FILE = "inspectit-agent.cfg";

	// The available strings to look for a valid beginning
	private static final String CONFIG_COMMENT = "#";
	private static final String CONFIG_REPOSITORY = "repository";
	private static final String CONFIG_SEND_STRATEGY = "send-strategy";
	private static final String CONFIG_BUFFER_STRATEGY = "buffer-strategy";
	private static final String CONFIG_METHOD_SENSOR_TYPE = "method-sensor-type";
	private static final String CONFIG_PLATFORM_SENSOR_TYPE = "platform-sensor-type";
	private static final String CONFIG_SENSOR = "sensor";
	private static final String CONFIG_EXCEPTION_SENSOR = "exception-sensor";

	/**
	 * Regular expression pattern to find the signatures of the method
	 * definitions in the configuration file. Pre-compiled for faster execution.
	 */
	private final Pattern methodSignature = Pattern.compile(".*\\((.+)\\)");

	/**
	 * Regular expression pattern to find empty signatures -> '()' .
	 * Pre-compiled for faster execution.
	 */
	private final Pattern emptyMethodSignature = Pattern.compile(".*\\(\\)");

	private boolean firstCreationOfExceptionSensor = true;

	/**
	 * Default constructor which accepts one parameter.
	 * 
	 * @param configurationStorage
	 *            The configuration storage implementation.
	 */
	public FileConfigurationReader(IConfigurationStorage configurationStorage) {
		this.configurationStorage = configurationStorage;
	}

	/**
	 * The default location for this reader is the path in the system variable
	 * 'inspectit.config'.
	 * <p>
	 * {@inheritDoc}
	 */
	public void load() throws ParserException {
		String pathToConfig = System.getProperty(CONFIGURATION_PROPERTY) + File.separator + CONFIGURATION_FILE;

		// Fallback to the standard location of the inspectit configuration
		// file when no system property is specified.
		if (pathToConfig.equals("")) {
			pathToConfig = System.getProperty("user.dir") + File.separator + "inspectit" + File.separator + CONFIGURATION_PROPERTY;
		}

		// Load and parse the file
		try {
			File f = new File(pathToConfig);
			InputStream is = new FileInputStream(f);
			LOGGER.finer("Agent Configuration file found at: " + f.getAbsolutePath());
			InputStreamReader reader = new InputStreamReader(is);
			this.parse(reader);
		} catch (FileNotFoundException e) {
			LOGGER.info("Agent Configuration file not found at " + pathToConfig + ", aborting!");
			throw new ParserException("Agent Configuration file not found at " + pathToConfig);
		}
	}

	/**
	 * Parses the given file.
	 * 
	 * @param reader
	 *            The reader to open and parse.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void parse(Reader reader) throws ParserException {
		// check for a valid Reader object
		if (null == reader) {
			throw new ParserException("Input is null! Aborting parsing.");
		}

		BufferedReader br = new BufferedReader(reader);

		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				// Skip empty and comment lines
				if (line.trim().equals("") || line.startsWith(CONFIG_COMMENT)) {
					continue;
				}

				// Split the line into tokens
				StringTokenizer tokenizer = new StringTokenizer(line, " ");
				String discriminator = tokenizer.nextToken();

				// check for the repository
				if (discriminator.equalsIgnoreCase(CONFIG_REPOSITORY)) {
					processRepositoryLine(tokenizer);
					continue;
				}

				// check for a sending strategy
				if (discriminator.equalsIgnoreCase(CONFIG_SEND_STRATEGY)) {
					processSendStrategyLine(tokenizer);
					continue;
				}

				// check for a buffer strategy
				if (discriminator.equalsIgnoreCase(CONFIG_BUFFER_STRATEGY)) {
					processBufferStrategyLine(tokenizer);
					continue;
				}

				// Check for the method sensor type
				if (discriminator.equalsIgnoreCase(CONFIG_METHOD_SENSOR_TYPE)) {
					processMethodSensorTypeLine(tokenizer);
					continue;
				}

				// Check for the platform sensor type
				if (discriminator.equalsIgnoreCase(CONFIG_PLATFORM_SENSOR_TYPE)) {
					processPlatformSensorTypeLine(tokenizer);
					continue;
				}

				// check for exception sensor type
				if (discriminator.equalsIgnoreCase(CONFIG_EXCEPTION_SENSOR)) {
					processExceptionSensorLine(tokenizer);
					continue;
				}

				// check for a sensor
				if (discriminator.equalsIgnoreCase(CONFIG_SENSOR)) {
					processSensorLine(tokenizer);
					continue;
				}

			}
		} catch (Throwable throwable) {
			LOGGER.severe("Error reading config on line : " + line);
			throw new ParserException("Error reading config on line : " + line, throwable);
		}
	}

	/**
	 * Processes a exception sensor line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor
	 *            type.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void processExceptionSensorLine(StringTokenizer tokenizer) throws ParserException {
		// the sensor name is hardcoded here, because we don't define the
		// fully-qualified name of the exception sensor in the config file.
		String sensorTypeClass = "info.novatec.inspectit.agent.sensor.exception.ExceptionTracingSensor";
		String targetClassName = tokenizer.nextToken();
		boolean isVirtual = false;

		if (targetClassName.indexOf("*") > -1) {
			isVirtual = true;
		}

		// Currently there are no settings
		Map settings = new HashMap();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}

		try {
			if (firstCreationOfExceptionSensor) {
				firstCreationOfExceptionSensor = false;
				configurationStorage.addExceptionSensorType(sensorTypeClass, settings);
			}
			configurationStorage.addExceptionSensorTypeParameter(sensorTypeClass, targetClassName, isVirtual, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the exception sensor type to the storage", e);
		}
	}

	/**
	 * Processes a method sensor type line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor
	 *            type.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void processMethodSensorTypeLine(StringTokenizer tokenizer) throws ParserException {
		String sensorTypeName = tokenizer.nextToken();
		String sensorTypeClass = tokenizer.nextToken();
		String priorityString = tokenizer.nextToken();
		PriorityEnum priority = PriorityEnum.fromInt(PriorityEnum.names().indexOf(priorityString));

		Map settings = new HashMap();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}

		try {
			configurationStorage.addMethodSensorType(sensorTypeName, sensorTypeClass, priority, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the method sensor type to the storage", e);
		}
	}

	/**
	 * Processes a platform sensor type line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor
	 *            type.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void processPlatformSensorTypeLine(StringTokenizer tokenizer) throws ParserException {
		String sensorTypeClass = tokenizer.nextToken();

		Map settings = new HashMap();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}
		try {
			configurationStorage.addPlatformSensorType(sensorTypeClass, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the platform sensor type to the storage", e);
		}
	}

	/**
	 * Processes a sensor line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void processSensorLine(StringTokenizer tokenizer) throws ParserException {
		String sensorTypeName = tokenizer.nextToken();
		String targetClassName = tokenizer.nextToken();
		String targetMethodName = tokenizer.nextToken();
		String sensorName = tokenizer.nextToken();
		boolean ignoreSignature = false;

		// Trying to match the parameter types (if there are any)
		Matcher m = methodSignature.matcher(targetMethodName);
		List parameterList = Collections.EMPTY_LIST;
		if (m.matches()) {
			String[] classes = m.group(1).split(",");
			parameterList = new ArrayList(classes.length);

			for (int i = 0; i < classes.length; i++) {
				parameterList.add((classes[i]).trim());
			}
			targetMethodName = targetMethodName.split("\\(")[0];
		} else if (emptyMethodSignature.matcher(targetMethodName).matches()) {
			targetMethodName = targetMethodName.replaceAll("\\(\\)", "");
		} else {
			ignoreSignature = true;
		}

		Map settings = new HashMap();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();

			if ("property".equals(leftSide) || "p".equals(leftSide)) {
				List propertyAccessorList = (List) settings.get("property");
				if (null == propertyAccessorList) {
					propertyAccessorList = new ArrayList();
					settings.put("property", propertyAccessorList);
				}
				propertyAccessorList.add(rightSide);
			} else if ("field".equals(leftSide) || "f".equals(leftSide)) {
				List propertyAccessorList = (List) settings.get("field");
				if (null == propertyAccessorList) {
					propertyAccessorList = new ArrayList();
					settings.put("field", propertyAccessorList);
				}
				propertyAccessorList.add(rightSide);
			} else {
				settings.put(leftSide, rightSide);
			}
		}

		try {
			configurationStorage.addSensor(sensorName, sensorTypeName, targetClassName, targetMethodName, parameterList, ignoreSignature, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the sensor to the storage", e);
		}
	}

	/**
	 * Processes a repository line and initializes it afterwards.
	 * 
	 * @param tokenizer
	 *            The string tokenizer which contains the definition of the
	 *            repository.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void processRepositoryLine(StringTokenizer tokenizer) throws ParserException {
		String host = tokenizer.nextToken();
		int port = Integer.parseInt(tokenizer.nextToken());
		String name = tokenizer.nextToken();

		try {
			configurationStorage.setAgentName(name);
			configurationStorage.setRepository(host, port);
		} catch (StorageException e) {
			throw new ParserException("Could net set the agent name or repository", e);
		}
	}

	/**
	 * Processes a send strategy line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sending
	 *            strategy.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void processSendStrategyLine(StringTokenizer tokenizer) throws ParserException {
		String sendStrategyClass = tokenizer.nextToken();

		Map settings = new HashMap();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}
		try {
			configurationStorage.addSendingStrategy(sendStrategyClass, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the sending strategy to the storage", e);
		}
	}

	/**
	 * Processes a buffer strategy line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a buffer
	 *            strategy.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config
	 *             file.
	 */
	private void processBufferStrategyLine(StringTokenizer tokenizer) throws ParserException {
		String bufferStrategyClass = tokenizer.nextToken();

		Map settings = new HashMap();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}
		try {
			configurationStorage.setBufferStrategy(bufferStrategyClass, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not set the buffer strategy in the storage", e);
		}
	}

}
