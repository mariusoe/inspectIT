package info.novatec.inspectit.agent.config;

import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RepositoryConfig;
import info.novatec.inspectit.agent.config.impl.StrategyConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.sensor.exception.IExceptionSensor;

import java.util.List;
import java.util.Map;

/**
 * This storage is used by all configuration readers to store the information
 * into.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public interface IConfigurationStorage {

	/**
	 * Sets the repository. Internally, a {@link RepositoryConfig} class is
	 * instantiated and filled with the proper arguments.
	 * 
	 * @param host
	 *            The host ip / name.
	 * @param port
	 *            The host port.
	 * @throws StorageException
	 *             Thrown if something with the host name or port is wrong.
	 */
	void setRepository(String host, int port) throws StorageException;

	/**
	 * Returns an instance of the {@link RepositoryConfig} class which is filled
	 * with the values by the {@link #setRepository(String, int)} method.
	 * 
	 * @return The {@link RepositoryConfig} instance.
	 */
	RepositoryConfig getRepositoryConfig();

	/**
	 * Sets the name of the Agent.
	 * 
	 * @param name
	 *            The name of the Agent to set.
	 * @throws StorageException
	 *             Thrown if something with the agent name is wrong.
	 */
	void setAgentName(String name) throws StorageException;

	/**
	 * Returns the name of the Agent.
	 * 
	 * @return The name of the Agent.
	 */
	String getAgentName();

	/**
	 * Sets the unique buffer strategy. The parameters are stored in the
	 * {@link StrategyConfig} class.
	 * 
	 * @param clazzName
	 *            The fully qualified name of the buffer strategy class.
	 * @param settings
	 *            A map containing some optional settings for the buffer.
	 * @throws StorageException
	 *             This exception is thrown if something unexpected happens
	 *             while initializing the buffer strategy.
	 */
	void setBufferStrategy(String clazzName, Map settings) throws StorageException;

	/**
	 * Returns a {@link StrategyConfig} instance containing the buffer strategy
	 * information.
	 * 
	 * @return An instance of {@link StrategyConfig}.
	 */
	StrategyConfig getBufferStrategyConfig();

	/**
	 * Adds a sending strategy. The parameters are stored in the
	 * {@link StrategyConfig} class.
	 * 
	 * @param clazzName
	 *            The fully qualified name of the sending strategy class.
	 * @param settings
	 *            A map containing some optional settings for the sending
	 *            strategy.
	 * @throws StorageException
	 *             This exception is thrown if something unexpected happens
	 *             while initializing the buffer strategy.
	 */
	void addSendingStrategy(String clazzName, Map settings) throws StorageException;

	/**
	 * Returns a {@link List} of {@link StrategyConfig} instances containing the
	 * sending strategy information.
	 * 
	 * @return A {@link List} of {@link StrategyConfig} instances.
	 */
	List getSendingStrategyConfigs();

	/**
	 * Creates and initializes a {@link MethodSensorTypeConfig} out of the given
	 * parameters. A sensor type is always unique, hence only one instance
	 * exists which is used by all installed sensors in the target application.
	 * 
	 * @param sensorTypeName
	 *            The name of the sensor type.
	 * @param sensorTypeClass
	 *            The fully qualified definition of the sensor type which is
	 *            instantiated via reflection.
	 * @param priority
	 *            The priority of the sensor type.
	 * @param settings
	 *            A map containing optional settings.
	 * @throws StorageException
	 *             This exception is thrown if something unexpected happens
	 *             while initializing the buffer strategy.
	 */
	void addMethodSensorType(String sensorTypeName, String sensorTypeClass, PriorityEnum priority, Map settings) throws StorageException;

	/**
	 * Returns a {@link List} of the {@link MethodSensorTypeConfig} classes.
	 * 
	 * @return A {@link List} of {@link MethodSensorTypeConfig} classes.
	 */
	List getMethodSensorTypes();

	/**
	 * Returns a {@link List} of {@link MethodSensorTypeConfig} classes.
	 * 
	 * @return A {@link List} of {@link MethodSensorTypeConfig} classes.
	 */
	List getExceptionSensorTypes();

	/**
	 * Returns a {@link List} of the {@link UnregisteredSensorConfig} classes of
	 * the Exception Sensor.
	 * 
	 * @return A {@link List} of {@link UnregisteredSensorConfig} classes of the
	 *         Exception Sensor.
	 */
	List getExceptionSensorConfigs();

	/**
	 * Creates and initializes a {@link MethodSensorTypeConfig} out of the given
	 * parameters. A sensor type is always unique, hence only one instance
	 * exists which is used by all installed sensors in the target application.
	 * 
	 * @param sensorTypeClass
	 *            the fully qualified definition of the sensor type which is
	 *            instantiated via reflection.
	 * @param settings
	 *            A map containing optional settings.
	 * @throws StorageException
	 *             This exception is thrown if something unexpected happens
	 *             while initializing the buffer strategy.
	 */
	void addExceptionSensorType(String sensorTypeClass, Map settings) throws StorageException;

	void addExceptionSensorTypeParameter(String sensorTypeName, String targetClassName, boolean isVirtual, Map settings) throws StorageException;

	/**
	 * Creates and initializes a {@link MethodSensorTypeConfig} out of the given
	 * parameters. A sensor type is always unique, hence only one instance
	 * exists which is used by all installed sensors in the target application.
	 * 
	 * @param sensorTypeClass
	 *            The fully qualified definition of the sensor type which is
	 *            instantiated via reflection.
	 * @param settings
	 *            A map containing optional settings.
	 * @throws StorageException
	 *             This exception is thrown if something unexpected happens
	 *             while initializing the buffer strategy.
	 */
	void addPlatformSensorType(String sensorTypeClass, Map settings) throws StorageException;

	/**
	 * Returns a {@link List} of the {@link PlatformSensorTypeConfig} classes.
	 * 
	 * @return A {@link List} of {@link PlatformSensorTypeConfig} classes.
	 */
	List getPlatformSensorTypes();

	/**
	 * Adds a new sensor definition.
	 * 
	 * @param sensorName
	 *            The name of the sensor.
	 * @param sensorTypeName
	 *            The name of the sensor type.
	 * @param targetClassName
	 *            The name of the target class.
	 * @param targetMethodName
	 *            The name of the target method.
	 * @param parameterList
	 *            The list of parameters of the target method.
	 * @param ignoreSignature
	 *            Defines if the signature does not matter for the method, hence
	 *            all methods matching the <code>targetMethodName</code> despite
	 *            their signatures are instrumented.
	 * @param settings
	 *            Additional and optional settings for this sensor definition as
	 *            a {@link Map}. The key and value has to be defined as a
	 *            standard {@link String}. <br>
	 *            Available are the keys <b>superclass</b> and <b>interface</b>
	 *            with the value <code>true</code> or <code>false</code>.
	 * @throws StorageException
	 *             This exception is thrown if something unexpected happens
	 *             while initializing the buffer strategy.
	 */
	void addSensor(String sensorName, String sensorTypeName, String targetClassName, String targetMethodName, List parameterList, boolean ignoreSignature, Map settings) throws StorageException;

	/**
	 * Returns a {@link List} of the {@link UnregisteredSensorConfig} classes.
	 * 
	 * @return A {@link List} of {@link UnregisteredSensorConfig} classes.
	 */
	List getUnregisteredSensorConfigs();

	/**
	 * Returns whether the {@link IExceptionSensor} is activated.
	 * 
	 * @return Whether the {@link IExceptionSensor} is activated.
	 */
	boolean isExceptionSensorActivated();

}