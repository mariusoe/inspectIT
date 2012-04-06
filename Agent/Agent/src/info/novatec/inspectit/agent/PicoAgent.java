package info.novatec.inspectit.agent;

import info.novatec.inspectit.agent.analyzer.IByteCodeAnalyzer;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatchPattern;
import info.novatec.inspectit.agent.analyzer.impl.ByteCodeAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.ClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.InheritanceAnalyzer;
import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.config.IConfigurationReader;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.ParserException;
import info.novatec.inspectit.agent.config.impl.ConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.FileConfigurationReader;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor;
import info.novatec.inspectit.agent.config.impl.StrategyConfig;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.impl.RMIConnection;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.impl.CoreService;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.hooking.IHookDispatcher;
import info.novatec.inspectit.agent.hooking.IHookInstrumenter;
import info.novatec.inspectit.agent.hooking.impl.HookDispatcher;
import info.novatec.inspectit.agent.hooking.impl.HookInstrumenter;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.StatementStorage;
import info.novatec.inspectit.agent.sensor.platform.IPlatformSensor;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.util.Timer;
import info.novatec.inspectit.versioning.FileBasedVersioningServiceImpl;
import info.novatec.inspectit.versioning.IVersioningService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.alternatives.CachingPicoContainer;
import org.picocontainer.defaults.ComponentParameter;

/**
 * The {@link PicoAgent} is used by the javaagent to analyze the passed bytecode if its needed to be
 * instrumented. The {@link #getInstance()} method returns the singleton instance of this class.
 * <p>
 * The {@link #inspectByteCode(byte[], String, ClassLoader)} is the method which should be called by
 * the javaagent. The method returns null if nothing has to be changed or something happened
 * unexpectedly.
 * <p>
 * This class is named <b>Pico</b>Agent as its using the Pico Container to handle the different
 * components in the Agent.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PicoAgent implements IAgent {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(PicoAgent.class.getName());

	/**
	 * The pico container.
	 */
	private MutablePicoContainer pico;

	/**
	 * The hook dispatcher used by the instrumented methods.
	 */
	private IHookDispatcher hookDispatcher;

	/**
	 * Set to <code>true</code> if something happened while trying to initialize the pico container.
	 */
	private boolean initializationError = false;

	/**
	 * Constructor initializing this agent.
	 */
	public PicoAgent() {
		this.init();
	}

	/**
	 * Initialize this class, more specific the Pico container. It will register every needed
	 * component.
	 */
	private void init() {
		try {
			// Use the caching mechanism. This is where a component has a single
			// instance in the container rather that a new one created each time
			// the container is asked for that type
			pico = new CachingPicoContainer();
			pico.registerComponentImplementation(Timer.class);
			pico.registerComponentImplementation(StatementStorage.class);
			pico.registerComponentImplementation(IInheritanceAnalyzer.class, InheritanceAnalyzer.class);
			pico.registerComponentImplementation(IClassPoolAnalyzer.class, ClassPoolAnalyzer.class);
			pico.registerComponentImplementation(IConfigurationStorage.class, ConfigurationStorage.class);
			pico.registerComponentImplementation(IPropertyAccessor.class, PropertyAccessor.class);
			pico.registerComponentImplementation(IConfigurationReader.class, FileConfigurationReader.class);
			pico.registerComponentImplementation(IIdManager.class, IdManager.class);
			pico.registerComponentImplementation(IConnection.class, RMIConnection.class);
			pico.registerComponentImplementation(IVersioningService.class, FileBasedVersioningServiceImpl.class);

			// we have to load the configuration before we register everything
			// else, otherwise some classes won't be available (buffer and
			// sending strategies for example).
			IConfigurationReader configurationReader = (IConfigurationReader) pico.getComponentInstance(IConfigurationReader.class);
			configurationReader.load();

			// now we need to extract the saved information from the
			// configuration storage, otherwise it won't be available in the
			// container for other components.
			IConfigurationStorage configurationStorage = (IConfigurationStorage) pico.getComponentInstance(IConfigurationStorage.class);
			IBufferStrategy<MethodSensorData> bufferStrategy = this.initBufferStrategy(configurationStorage.getBufferStrategyConfig());
			pico.registerComponentInstance(IBufferStrategy.class, bufferStrategy);

			for (StrategyConfig config : configurationStorage.getSendingStrategyConfigs()) {
				ISendingStrategy sendingStrategy = this.initSendingStrategy(config);
				pico.registerComponentInstance(sendingStrategy);
			}

			for (PlatformSensorTypeConfig config : configurationStorage.getPlatformSensorTypes()) {
				IPlatformSensor platformSensor = this.initPlatformSensor(config);
				config.setSensorType(platformSensor);
			}

			for (MethodSensorTypeConfig config : configurationStorage.getMethodSensorTypes()) {
				IMethodSensor methodSensor = this.initMethodSensor(config);
				config.setSensorType(methodSensor);
			}

			// register now all other components
			pico.registerComponentImplementation(IByteCodeAnalyzer.class, ByteCodeAnalyzer.class);
			pico.registerComponentImplementation(ICoreService.class, CoreService.class, new Parameter[] { new ComponentParameter(), new ComponentParameter(), new ComponentParameter(),
					new ComponentParameter(ISendingStrategy.class, false) });
			pico.registerComponentImplementation(IHookInstrumenter.class, HookInstrumenter.class);
			pico.registerComponentImplementation(IHookDispatcher.class, HookDispatcher.class);

			pico.start();

			// Provide the version number output during the startup of the agent
			if (LOGGER.isLoggable(Level.INFO)) {
				String currentVersion = "n/a";
				try {
					currentVersion = ((IVersioningService) pico.getComponentInstance(IVersioningService.class)).getVersion();
				} catch (IOException e) {
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.log(Level.FINE, "Versioning information could not be read", e);
					}
				}
				LOGGER.info("Using agent version " + currentVersion);
			}

			hookDispatcher = (IHookDispatcher) pico.getComponentInstance(IHookDispatcher.class);
		} catch (ParserException parserException) {
			LOGGER.severe("The parser produced an exception!");
			LOGGER.throwing("PicoAgent", "init", parserException);
			parserException.printStackTrace();
			initializationError = true;
		} catch (Throwable throwable) {
			LOGGER.severe("Something unexpected was caught!");
			LOGGER.throwing("PicoAgent", "init", throwable);
			throwable.printStackTrace();
			initializationError = true;
		}

	}

	/**
	 * Initializes the buffer strategy saved in the configuration storage.
	 * 
	 * @param bufferStrategyConfig
	 *            The buffer strategy configuration.
	 * @return The instantiated buffer strategy.
	 * @throws Exception
	 *             Root exception thrown if something happens while trying to instantiate the buffer
	 *             strategy.
	 */
	private IBufferStrategy<MethodSensorData> initBufferStrategy(StrategyConfig bufferStrategyConfig) throws Exception {
		Class<?> clazz = Class.forName(bufferStrategyConfig.getClazzName());
		@SuppressWarnings("unchecked")
		IBufferStrategy<MethodSensorData> bufferStrategy = (IBufferStrategy<MethodSensorData>) clazz.newInstance();
		bufferStrategy.init(bufferStrategyConfig.getSettings());

		return bufferStrategy;
	}

	/**
	 * Initializes the sending strategy saved in the configuration storage.
	 * 
	 * @param sendingStrategyConfig
	 *            The sending strategy configuration.
	 * @return The instantiated sending strategy.
	 * @throws Exception
	 *             Root exception thrown if something happens while trying to instantiate the
	 *             sending strategy.
	 */
	private ISendingStrategy initSendingStrategy(StrategyConfig sendingStrategyConfig) throws Exception {
		Class<?> clazz = Class.forName(sendingStrategyConfig.getClazzName());
		ISendingStrategy sendingStrategy = (ISendingStrategy) clazz.newInstance();
		sendingStrategy.init(sendingStrategyConfig.getSettings());

		return sendingStrategy;
	}

	/**
	 * Initializes the platform sensor saved in the configuration storage.
	 * 
	 * @param config
	 *            The platform sensor type configuration.
	 * @return The instantiated buffer strategy.
	 * @throws Exception
	 *             Root exception thrown if something happens while trying to instantiate the
	 *             platform sensor type.
	 */
	private IPlatformSensor initPlatformSensor(PlatformSensorTypeConfig config) throws Exception {
		Class<?> platformSensorClass = Class.forName(config.getClassName());

		// Workaround to instantiate the class with the correct parameters
		pico.registerComponentImplementation(platformSensorClass);
		IPlatformSensor platformSensor = (IPlatformSensor) pico.getComponentInstance(platformSensorClass);
		platformSensor.init(config.getParameters());

		return platformSensor;
	}

	/**
	 * Initializes the method sensor saved in the configuration storage.
	 * 
	 * @param config
	 *            The method sensor type configuration.
	 * @return The instantiated buffer strategy.
	 * @throws Exception
	 *             Root exception thrown if something happens while trying to instantiate the method
	 *             sensor type.
	 */
	private IMethodSensor initMethodSensor(MethodSensorTypeConfig config) throws Exception {
		Class<?> methodSensorClass = Class.forName(config.getClassName());

		// Workaround to instantiate the class with the correct parameters
		pico.registerComponentImplementation(methodSensorClass);
		IMethodSensor methodSensor = (IMethodSensor) pico.getComponentInstance(methodSensorClass);
		methodSensor.init(config.getParameters());

		return methodSensor;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] inspectByteCode(byte[] byteCode, String className, ClassLoader classLoader) {
		// if an error in the init method was caught, we'll do nothing here.
		// This prevents further errors.
		if (initializationError) {
			return byteCode;
		}

		// ignore all classes which fit to the patterns in the configuration
		IConfigurationStorage configurationStorage = (IConfigurationStorage) pico.getComponentInstance(IConfigurationStorage.class);
		List<IMatchPattern> ignoreClassesPatterns = configurationStorage.getIgnoreClassesPatterns();
		for (IMatchPattern matchPattern : ignoreClassesPatterns) {
			if (matchPattern.match(className)) {
				return byteCode;
			}
		}

		IByteCodeAnalyzer byteCodeAnalyzer = (IByteCodeAnalyzer) pico.getComponentInstance(IByteCodeAnalyzer.class);
		try {
			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
			return instrumentedByteCode;
		} catch (Throwable throwable) {
			LOGGER.severe("Something unexpected happened while trying to analyze or instrument the bytecode with the class name: " + className);
			throwable.printStackTrace();
			return byteCode;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IHookDispatcher getHookDispatcher() {
		return hookDispatcher;
	}

}
