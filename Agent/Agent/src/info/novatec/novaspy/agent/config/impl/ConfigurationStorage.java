package info.novatec.novaspy.agent.config.impl;

import info.novatec.novaspy.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.novaspy.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.novaspy.agent.config.IConfigurationStorage;
import info.novatec.novaspy.agent.config.PriorityEnum;
import info.novatec.novaspy.agent.config.StorageException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default configuration storage implementation which stores everything in
 * the memory.
 * <p>
 * TODO: Event mechanism is needed so that new definitions can be added and
 * other components are notified that something has been added.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public class ConfigurationStorage implements IConfigurationStorage {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConfigurationStorage.class.getName());

	/**
	 * The class pool analyzer.
	 */
	private final IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * The inheritance analyzer.
	 */
	private final IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * The repository configuration is used to store the needed information to
	 * connect to a remote CMR.
	 */
	private RepositoryConfig repository;

	/**
	 * The name of the agent.
	 */
	private String agentName;

	/**
	 * The used buffer strategy.
	 */
	private StrategyConfig bufferStrategy;

	/**
	 * The list of sending strategies. Default size is set to 1 as it's unlikely
	 * that more than one is defined.
	 */
	private List sendingStrategies = new ArrayList(1);

	/**
	 * The default size of the method sensor type list.
	 */
	private static final int METHOD_LIST_SIZE = 10;

	/**
	 * The list of method sensor types. Contains objects of type
	 * {@link MethodSensorTypeConfig}.
	 */
	private List methodSensorTypes = new ArrayList(METHOD_LIST_SIZE);

	/**
	 * The default size of the platform sensor type list.
	 */
	private static final int PLATFORM_LIST_SIZE = 10;

	/**
	 * The list of platform sensor types. Contains objects of type
	 * {@link PlatformSensorTypeConfig}.
	 */
	private List platformSensorTypes = new ArrayList(PLATFORM_LIST_SIZE);

	/**
	 * A list containing all the sensor definitions from the configuration.
	 */
	private List unregisteredSensorConfigs = new ArrayList();

	/**
	 * A list containing all exception sensor types. Currently there is only one
	 * exception sensor type.
	 */
	private List exceptionSensorTypes = new ArrayList();

	/**
	 * A list containing all the sensor definitions from the configuration of
	 * the Exception Sensor.
	 */
	private List exceptionSensorConfigs = new ArrayList();

	private boolean exceptionSensorActivated;

	/**
	 * Default constructor which takes 2 parameter.
	 * 
	 * @param classPoolAnalyzer
	 *            The class pool analyzer used by the sensor configuration.
	 * @param inheritanceAnalyzer
	 *            The inheritance analyzer used by the sensor configuration.
	 */
	public ConfigurationStorage(IClassPoolAnalyzer classPoolAnalyzer, IInheritanceAnalyzer inheritanceAnalyzer) {
		this.classPoolAnalyzer = classPoolAnalyzer;
		this.inheritanceAnalyzer = inheritanceAnalyzer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRepository(String host, int port) throws StorageException {
		if ((null == host) || "".equals(host)) {
			throw new StorageException("Repository host name cannot be null or empty!");
		}

		if (port < 1) {
			throw new StorageException("Repository port has to be greater than 0!");
		}

		this.repository = new RepositoryConfig(host, port);

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Repository definition added. Host: " + host + " Port: " + port);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RepositoryConfig getRepositoryConfig() {
		return repository;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAgentName(String name) throws StorageException {
		if ((null == name) || "".equals(name)) {
			throw new StorageException("Agent name cannot be null or empty!");
		}

		agentName = name;

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Agent name set to: " + name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAgentName() {
		return agentName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBufferStrategy(String clazzName, Map settings) throws StorageException {
		if ((null == clazzName) || "".equals(clazzName)) {
			throw new StorageException("Buffer strategy class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.EMPTY_MAP;
		}

		this.bufferStrategy = new StrategyConfig(clazzName, settings);

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Buffer strategy set to: " + clazzName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public StrategyConfig getBufferStrategyConfig() {
		return bufferStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSendingStrategy(String clazzName, Map settings) throws StorageException {
		if ((null == clazzName) || "".equals(clazzName)) {
			throw new StorageException("Sending strategy class name cannot be null or empty!");
		}

		for (Iterator iterator = sendingStrategies.iterator(); iterator.hasNext();) {
			StrategyConfig config = (StrategyConfig) iterator.next();
			if (clazzName.equals(config.getClazzName())) {
				throw new StorageException("Sending strategy class is already registered!");
			}
		}

		if (null == settings) {
			settings = Collections.EMPTY_MAP;
		}

		sendingStrategies.add(new StrategyConfig(clazzName, settings));

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Sending strategy added: " + clazzName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSendingStrategyConfigs() {
		return Collections.unmodifiableList(sendingStrategies);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodSensorType(String sensorTypeName, String sensorTypeClass, PriorityEnum priority, Map settings) throws StorageException {
		if ((null == sensorTypeName) || "".equals(sensorTypeName)) {
			throw new StorageException("Method sensor type name cannot be null or empty!");
		}

		if ((null == sensorTypeClass) || "".equals(sensorTypeClass)) {
			throw new StorageException("Method sensor type class name cannot be null or empty!");
		}

		if (null == priority) {
			throw new StorageException("Method sensor type priority cannot be null!");
		}

		if (null == settings) {
			settings = Collections.EMPTY_MAP;
		}

		MethodSensorTypeConfig sensorTypeConfig = new MethodSensorTypeConfig();
		sensorTypeConfig.setName(sensorTypeName);
		sensorTypeConfig.setClassName(sensorTypeClass);
		sensorTypeConfig.setPriority(priority);
		sensorTypeConfig.setParameters(settings);

		methodSensorTypes.add(sensorTypeConfig);

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Method sensor type added: " + sensorTypeName + " prio: " + priority.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List getMethodSensorTypes() {
		return Collections.unmodifiableList(methodSensorTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorType(String sensorTypeClass, Map settings) throws StorageException {
		if ((null == sensorTypeClass) || "".equals(sensorTypeClass)) {
			throw new StorageException("Platform sensor type class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.EMPTY_MAP;
		}

		PlatformSensorTypeConfig sensorTypeConfig = new PlatformSensorTypeConfig();
		sensorTypeConfig.setClassName(sensorTypeClass);
		sensorTypeConfig.setParameters(settings);

		platformSensorTypes.add(sensorTypeConfig);

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Platform sensor type added: " + sensorTypeClass);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List getPlatformSensorTypes() {
		return Collections.unmodifiableList(platformSensorTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensor(String sensorName, String sensorTypeName, String targetClassName, String targetMethodName, List parameterList, boolean ignoreSignature, Map settings) throws StorageException {
		if ((null == sensorName) || "".equals(sensorName)) {
			throw new StorageException("Sensor name cannot be null or empty!");
		}

		if ((null == sensorTypeName) || "".equals(sensorTypeName)) {
			throw new StorageException("Sensor type name for the sensor cannot be null or empty!");
		}

		if ((null == targetClassName) || "".equals(targetClassName)) {
			throw new StorageException("Target class name cannot be null or empty!");
		}

		if ((null == targetMethodName) || "".equals(targetMethodName)) {
			throw new StorageException("Target method name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.EMPTY_MAP;
		}

		UnregisteredSensorConfig sensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);

		// check for a virtual definition
		if (ignoreSignature) {
			sensorConfig.setVirtual(true);
		}

		// check if we are dealing with a superclass definition
		if (settings.containsKey("superclass") && settings.get("superclass").equals("true")) {
			sensorConfig.setSuperclass(true);
		}

		// check if we are dealing with a interface definition
		if (settings.containsKey("interface") && settings.get("interface").equals("true")) {
			sensorConfig.setInterface(true);
		}

		if (settings.containsKey("field")) {
			sensorConfig.setPropertyAccess(true);
			List fieldAccessorList = (List) settings.get("field");

			for (Iterator iterator = fieldAccessorList.iterator(); iterator.hasNext();) {
				String fieldDefinition = (String) iterator.next();
				String[] fieldDefinitionParts = fieldDefinition.split(";");
				String name = fieldDefinitionParts[0];
				PropertyAccessor.PropertyPathStart start = new PropertyAccessor.PropertyPathStart();
				start.setName(name);
				start.setClassOfExecutedMethod(true);

				String[] steps = fieldDefinitionParts[1].split("\\.");
				PropertyAccessor.PropertyPath parentPath = start;
				for (int i = 0; i < steps.length; i++) {
					String step = steps[i];
					PropertyAccessor.PropertyPath path = new PropertyAccessor.PropertyPath();
					path.setName(step);
					parentPath.setPathToContinue(path);
					parentPath = path;
				}

				sensorConfig.getPropertyAccessorList().add(start);
			}
		}

		if (settings.containsKey("property")) {
			sensorConfig.setPropertyAccess(true);
			List propertyAccessorList = (List) settings.get("property");

			for (Iterator iterator = propertyAccessorList.iterator(); iterator.hasNext();) {
				String fieldDefinition = (String) iterator.next();
				String[] fieldDefinitionParts = fieldDefinition.split(";");
				int position = Integer.parseInt(fieldDefinitionParts[0]);
				String name = fieldDefinitionParts[1];
				PropertyAccessor.PropertyPathStart start = new PropertyAccessor.PropertyPathStart();
				start.setName(name);
				start.setSignaturePosition(position);

				if (3 == fieldDefinitionParts.length) {
					String[] steps = fieldDefinitionParts[2].split("\\.");
					PropertyAccessor.PropertyPath parentPath = start;
					for (int i = 0; i < steps.length; i++) {
						String step = steps[i];
						PropertyAccessor.PropertyPath path = new PropertyAccessor.PropertyPath();
						path.setName(step);
						parentPath.setPathToContinue(path);
						parentPath = path;
					}
				}

				sensorConfig.getPropertyAccessorList().add(start);
			}
		}

		// Now set all the given parameters
		sensorConfig.setTargetClassName(targetClassName);
		sensorConfig.setTargetMethodName(targetMethodName);
		if ("<init>".equals(targetMethodName)) {
			sensorConfig.setConstructor(true);
		}
		sensorConfig.setSensorName(sensorName);
		sensorConfig.setIgnoreSignature(ignoreSignature);
		sensorConfig.setParameterTypes(parameterList);
		sensorConfig.setSettings(settings);
		sensorConfig.setSensorTypeConfig(getMethodSensorTypeConfigForName(sensorTypeName));
		sensorConfig.completeConfiguration();

		unregisteredSensorConfigs.add(sensorConfig);

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Sensor configuration added: " + sensorConfig.toString());
		}
	}

	/**
	 * Returns the matching {@link MethodSensorTypeConfig} for the passed name.
	 * 
	 * @param sensorTypeName
	 *            The name to look for.
	 * @return The {@link MethodSensorTypeConfig} which name is equal to the
	 *         passed sensor type name in the method parameter.
	 * @throws StorageException
	 *             Throws the storage exception if no method sensor type
	 *             configuration can be found.
	 */
	private MethodSensorTypeConfig getMethodSensorTypeConfigForName(String sensorTypeName) throws StorageException {
		for (Iterator iterator = methodSensorTypes.iterator(); iterator.hasNext();) {
			MethodSensorTypeConfig config = (MethodSensorTypeConfig) iterator.next();
			if (config.getName().equals(sensorTypeName)) {
				return config;
			}
		}

		throw new StorageException("Could not find method sensor type with name: " + sensorTypeName);
	}

	/**
	 * Returns the matching {@link MethodSensorTypeConfig} of the Exception
	 * Sensor for the passed name.
	 * 
	 * @param sensorTypeName
	 *            The name to look for.
	 * @return The {@link MethodSensorTypeConfig} which name is equal to the
	 *         passed sensor type name in the method parameter.
	 * @throws StorageException
	 *             Throws the storage exception if no method sensor type
	 *             configuration can be found.
	 */
	private MethodSensorTypeConfig getExceptionSensorTypeConfigForName(String sensorTypeName) throws StorageException {
		for (Iterator iterator = exceptionSensorTypes.iterator(); iterator.hasNext();) {
			MethodSensorTypeConfig config = (MethodSensorTypeConfig) iterator.next();
			if (config.getName().equals(sensorTypeName)) {
				return config;
			}
		}

		throw new StorageException("Could not find exception sensor type with name: " + sensorTypeName);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getUnregisteredSensorConfigs() {
		return Collections.unmodifiableList(unregisteredSensorConfigs);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getExceptionSensorTypes() {
		return Collections.unmodifiableList(exceptionSensorTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getExceptionSensorConfigs() {
		return Collections.unmodifiableList(exceptionSensorConfigs);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorType(String sensorTypeClass, Map settings) throws StorageException {
		if ((null == sensorTypeClass) || "".equals(sensorTypeClass)) {
			throw new StorageException("Exception sensor type class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.EMPTY_MAP;
		}

		MethodSensorTypeConfig sensorTypeConfig = new MethodSensorTypeConfig();
		sensorTypeConfig.setName(sensorTypeClass);
		sensorTypeConfig.setClassName(sensorTypeClass);
		sensorTypeConfig.setParameters(settings);

		exceptionSensorTypes.add(sensorTypeConfig);

		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Exception sensor type added: " + sensorTypeClass);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorTypeParameter(String sensorTypeName, String targetClassName, boolean isVirtual, Map settings) throws StorageException {
		if ((null == sensorTypeName) || "".equals(sensorTypeName)) {
			throw new StorageException("Sensor type name for the sensor cannot be null or empty!");
		}

		if ((null == targetClassName) || "".equals(targetClassName)) {
			throw new StorageException("Target class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.EMPTY_MAP;
		}

		UnregisteredSensorConfig sensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);

		sensorConfig.setVirtual(isVirtual);

		// check if we are dealing with a superclass definition
		if (settings.containsKey("superclass") && settings.get("superclass").equals("true")) {
			sensorConfig.setSuperclass(true);
		}

		// check if we are dealing with a interface definition
		if (settings.containsKey("interface") && settings.get("interface").equals("true")) {
			sensorConfig.setInterface(true);
		}

		// Now set all the given parameters
		sensorConfig.setTargetClassName(targetClassName);
		sensorConfig.setSettings(settings);
		sensorConfig.setSensorTypeConfig(getExceptionSensorTypeConfigForName(sensorTypeName));
		sensorConfig.setTargetMethodName("");
		sensorConfig.completeConfiguration();

		exceptionSensorConfigs.add(sensorConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExceptionSensorActivated() {
		return exceptionSensorConfigs.size() > 0;
	}
}
