package info.novatec.inspectit.agent.config.test;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.DirectMatcher;
import info.novatec.inspectit.agent.analyzer.impl.IndirectMatcher;
import info.novatec.inspectit.agent.analyzer.impl.InterfaceMatcher;
import info.novatec.inspectit.agent.analyzer.impl.SuperclassMatcher;
import info.novatec.inspectit.agent.analyzer.impl.ThrowableMatcher;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.PriorityEnum;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.config.impl.ConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPath;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPathStart;
import info.novatec.inspectit.agent.config.impl.StrategyConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.test.AbstractLogSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigurationStorageTest extends AbstractLogSupport {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	private IConfigurationStorage configurationStorage;

	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	/**
	 * This method will be executed before every method is executed in here.
	 * This ensures that some tests don't modify the contents of the
	 * configuration storage.
	 */
	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws StorageException {
		configurationStorage = new ConfigurationStorage(classPoolAnalyzer, inheritanceAnalyzer);

		// name and repository
		configurationStorage.setAgentName("UnitTestAgent");
		configurationStorage.setRepository("localhost", 1099);

		// method sensor types
		Map<String, String> settings = new HashMap<String, String>(1);
		settings.put("mode", "optimized");
		configurationStorage.addMethodSensorType("timer", "info.novatec.inspectit.agent.sensor.method.timer.TimerSensor", PriorityEnum.MAX, settings);
		configurationStorage.addMethodSensorType("isequence", "info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceSensor", PriorityEnum.INVOC, null);

		// platform sensor types
		configurationStorage.addPlatformSensorType("info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation", null);
		configurationStorage.addPlatformSensorType("info.novatec.inspectit.agent.sensor.platform.CompilationInformation", null);
		configurationStorage.addPlatformSensorType("info.novatec.inspectit.agent.sensor.platform.RuntimeInformation", null);

		// exception sensor
		configurationStorage.addExceptionSensorType("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", null);

		// exception sensor parameters
		settings = new HashMap<String, String>();
		settings.put("superclass", "true");
		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "java.lang.Throwable", false, settings);

		settings = new HashMap<String, String>();
		settings.put("interface", "true");
		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "info.novatec.inspectit.agent.analyzer.test.classes.IException", false,
				settings);

		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "info.novatec.inspectit.agent.analyzer.test.classes.My*Exception", true,
				Collections.EMPTY_MAP);
		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "info.novatec.inspectit.agent.analyzer.test.classes.MyException", false,
				Collections.EMPTY_MAP);

		// sending strategies
		settings = new HashMap<String, String>(1);
		settings.put("time", "5000");
		configurationStorage.addSendingStrategy("info.novatec.inspectit.agent.sending.impl.TimeStrategy", settings);

		settings = new HashMap<String, String>(1);
		settings.put("size", "10");
		configurationStorage.addSendingStrategy("info.novatec.inspectit.agent.sending.impl.ListSizeStrategy", settings);

		// buffer strategy
		configurationStorage.setBufferStrategy("info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy", null);

		// sensor definitions
		configurationStorage.addSensor("timer", "*", "*", null, true, null);

		configurationStorage.addSensor("isequence", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", null, true, null);

		List<String> parameterList = new ArrayList<String>();
		parameterList.add("java.lang.String");
		configurationStorage.addSensor("timer", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", parameterList, false, null);

		settings = new HashMap<String, String>();
		settings.put("interface", "true");
		configurationStorage.addSensor("timer", "info.novatec.IService", "*Service", null, true, settings);

		settings = new HashMap<String, String>();
		settings.put("superclass", "true");
		configurationStorage.addSensor("isequence", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", null, true, settings);
		
		Map<String, List<String>> fieldSettings = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<String>();
		list.add("LastOutput;jlbOutput.text");
		fieldSettings.put("field", list);
		configurationStorage.addSensor("timer", "*", "*", null, true, fieldSettings);

		fieldSettings = new HashMap<String, List<String>>();
		list = new ArrayList<String>();
		list.add("0;Source;msg");
		fieldSettings.put("property", list);
		configurationStorage.addSensor("timer", "*", "*", null, true, fieldSettings);
		
		settings = new HashMap<String, String>();
		settings.put("annotation", "javax.ejb.StatelessBean");
		configurationStorage.addSensor("isequence", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", null, false, settings);

	}

	@Test()
	public void agentNameCheck() {
		assertEquals(configurationStorage.getAgentName(), "UnitTestAgent");

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setNullAgentName() throws StorageException {
		configurationStorage.setAgentName(null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setEmptyAgentName() throws StorageException {
		configurationStorage.setAgentName("");

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void repositoryCheck() {
		assertEquals(configurationStorage.getRepositoryConfig().getHost(), "localhost");
		assertEquals(configurationStorage.getRepositoryConfig().getPort(), 1099);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setNullRepositoryHost() throws StorageException {
		configurationStorage.setRepository(null, 1099);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setEmptyRepositoryHost() throws StorageException {
		configurationStorage.setRepository("", 1099);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void methodSensorTypesCheck() {
		List<MethodSensorTypeConfig> configs = configurationStorage.getMethodSensorTypes();
		assertNotNull(configs);
		assertEquals(configs.size(), 3);

		// first
		MethodSensorTypeConfig config = configs.get(0);
		assertEquals(config.getClassName(), "info.novatec.inspectit.agent.sensor.method.timer.TimerSensor");
		assertEquals(config.getName(), "timer");
		assertNotNull(config.getParameters());
		Map<String, String> settings = config.getParameters();
		assertEquals(settings.size(), 1);
		assertTrue(settings.containsKey("mode"));
		assertEquals(settings.get("mode"), "optimized");
		assertEquals(config.getPriority(), PriorityEnum.MAX);
		assertNull(config.getSensorType());

		// second
		config = configs.get(1);
		assertEquals(config.getClassName(), "info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceSensor");
		assertEquals(config.getName(), "isequence");
		assertNotNull(config.getParameters());
		assertEquals(config.getParameters().size(), 0);
		assertEquals(config.getPriority(), PriorityEnum.INVOC);
		assertNull(config.getSensorType());

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullMethodSensorTypeName() throws StorageException {
		configurationStorage.addMethodSensorType(null, "xxx", PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyMethodSensorTypeName() throws StorageException {
		configurationStorage.addMethodSensorType("", "xxx", PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullMethodSensorTypeClass() throws StorageException {
		configurationStorage.addMethodSensorType("xxx", null, PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyMethodSensorTypeClass() throws StorageException {
		configurationStorage.addMethodSensorType("xxx", "", PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullMethodSensorTypePriority() throws StorageException {
		configurationStorage.addMethodSensorType("xxx", "xxx", null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void platformSensorTypeCheck() {
		List<PlatformSensorTypeConfig> configs = configurationStorage.getPlatformSensorTypes();
		assertNotNull(configs);
		assertEquals(configs.size(), 3);

		// first
		PlatformSensorTypeConfig config = configs.get(0);
		assertEquals(config.getClassName(), "info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation");
		assertNotNull(config.getParameters());
		assertEquals(config.getParameters().size(), 0);
		assertNull(config.getSensorType());

		// second
		config = configs.get(1);
		assertEquals(config.getClassName(), "info.novatec.inspectit.agent.sensor.platform.CompilationInformation");
		assertNotNull(config.getParameters());
		assertEquals(config.getParameters().size(), 0);
		assertNull(config.getSensorType());

		// third
		config = configs.get(2);
		assertEquals(config.getClassName(), "info.novatec.inspectit.agent.sensor.platform.RuntimeInformation");
		assertNotNull(config.getParameters());
		assertEquals(config.getParameters().size(), 0);
		assertNull(config.getSensorType());

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullPlatformSensorTypeClass() throws StorageException {
		configurationStorage.addPlatformSensorType(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyPlatformSensorTypeClass() throws StorageException {
		configurationStorage.addPlatformSensorType("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void exceptionSensorCheck() {
		List<MethodSensorTypeConfig> configs = configurationStorage.getExceptionSensorTypes();
		assertNotNull(configs);
		assertEquals(configs.size(), 1);

		MethodSensorTypeConfig config = configs.get(0);
		assertEquals(config.getClassName(), "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		assertNotNull(config.getParameters());
		assertEquals(config.getParameters().size(), 0);
		assertNull(config.getSensorType());

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullExceptionSensor() throws StorageException {
		configurationStorage.addExceptionSensorType(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void exceptionSensorParameterCheck() {
		List<UnregisteredSensorConfig> configs = configurationStorage.getUnregisteredSensorConfigs();
		assertNotNull(configs);
		assertEquals(configs.size(), 12);

		// first
		UnregisteredSensorConfig config = configs.get(0);
		assertEquals(config.getTargetClassName(), "java.lang.Throwable");
		assertEquals(config.getTargetMethodName(), "");
		assertNull(config.getTargetPackageName());
		assertEquals(config.isConstructor(), true);
		assertEquals(config.isIgnoreSignature(), true);
		assertEquals(config.isInterface(), false);
		assertEquals(config.isVirtual(), false);
		assertEquals(config.isSuperclass(), true);
		assertSame(config.getMatcher().getClass(), ThrowableMatcher.class);
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 1);

		// second
		config = configs.get(1);
		assertEquals(config.getTargetClassName(), "info.novatec.inspectit.agent.analyzer.test.classes.IException");
		assertEquals(config.getTargetMethodName(), "");
		assertNull(config.getTargetPackageName());
		assertEquals(config.isConstructor(), true);
		assertEquals(config.isIgnoreSignature(), true);
		assertEquals(config.isInterface(), true);
		assertEquals(config.isVirtual(), false);
		assertEquals(config.isSuperclass(), false);
		assertSame(config.getMatcher().getClass(), ThrowableMatcher.class);
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 1);

		// third
		config = configs.get(2);
		assertEquals(config.getTargetClassName(), "info.novatec.inspectit.agent.analyzer.test.classes.My*Exception");
		assertEquals(config.getTargetMethodName(), "");
		assertNull(config.getTargetPackageName());
		assertEquals(config.isConstructor(), true);
		assertEquals(config.isIgnoreSignature(), true);
		assertEquals(config.isInterface(), false);
		assertEquals(config.isVirtual(), true);
		assertEquals(config.isSuperclass(), false);
		assertSame(config.getMatcher().getClass(), ThrowableMatcher.class);
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 0);

		// fourth
		config = configs.get(3);
		assertEquals(config.getTargetClassName(), "info.novatec.inspectit.agent.analyzer.test.classes.MyException");
		assertEquals(config.getTargetMethodName(), "");
		assertNull(config.getTargetPackageName());
		assertEquals(config.isConstructor(), true);
		assertEquals(config.isIgnoreSignature(), true);
		assertEquals(config.isInterface(), false);
		assertEquals(config.isVirtual(), false);
		assertEquals(config.isSuperclass(), false);
		assertSame(config.getMatcher().getClass(), ThrowableMatcher.class);
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 0);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);

	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyExceptionSensor() throws StorageException {
		configurationStorage.addExceptionSensorType("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void sendingStrategiesCheck() {
		List<StrategyConfig> strategies = configurationStorage.getSendingStrategyConfigs();
		assertNotNull(strategies);
		assertEquals(strategies.size(), 2);

		// first
		StrategyConfig config = strategies.get(0);
		assertEquals(config.getClazzName(), "info.novatec.inspectit.agent.sending.impl.TimeStrategy");
		assertNotNull(config.getSettings());
		Map<String, String> settings = config.getSettings();
		assertEquals(settings.size(), 1);
		assertTrue(settings.containsKey("time"));
		assertEquals(settings.get("time"), "5000");

		// second
		config = strategies.get(1);
		assertEquals(config.getClazzName(), "info.novatec.inspectit.agent.sending.impl.ListSizeStrategy");
		assertNotNull(config.getSettings());
		settings = config.getSettings();
		assertEquals(settings.size(), 1);
		assertTrue(settings.containsKey("size"));
		assertEquals(settings.get("size"), "10");

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSendingStrategy() throws StorageException {
		configurationStorage.addSendingStrategy(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySendingStrategy() throws StorageException {
		configurationStorage.addSendingStrategy("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void bufferStrategyCheck() {
		StrategyConfig config = configurationStorage.getBufferStrategyConfig();
		assertNotNull(config);

		assertEquals(config.getClazzName(), "info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy");
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 0);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setNullBufferStrategy() throws StorageException {
		configurationStorage.setBufferStrategy(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setEmptyBufferStrategy() throws StorageException {
		configurationStorage.setBufferStrategy("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void sensorCheck() {
		List<UnregisteredSensorConfig> configs = configurationStorage.getUnregisteredSensorConfigs();
		assertNotNull(configs);
		assertEquals(configs.size(), 12);
		
		// the first 4 configs are the ones from the exception sensor
		// first
		UnregisteredSensorConfig config = configs.get(4);
		assertEquals(config.getSensorTypeConfig().getName(), "timer");
		assertNull(config.getTargetPackageName());
		assertEquals(config.getTargetClassName(), "*");
		assertEquals(config.getTargetMethodName(), "*");
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 0);
		assertNotNull(config.getPropertyAccessorList());
		assertEquals(config.getPropertyAccessorList().size(), 0);
		assertSame(config.getMatcher().getClass(), IndirectMatcher.class);

		// second
		config = configs.get(5);
		assertEquals(config.getSensorTypeConfig().getName(), "isequence");
		assertNull(config.getTargetPackageName());
		assertEquals(config.getTargetClassName(), "info.novatec.inspectitsamples.calculator.Calculator");
		assertEquals(config.getTargetMethodName(), "actionPerformed");
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 0);
		assertNotNull(config.getPropertyAccessorList());
		assertEquals(config.getPropertyAccessorList().size(), 0);
		assertSame(config.getMatcher().getClass(), IndirectMatcher.class);

		// third
		config = configs.get(6);
		assertEquals(config.getSensorTypeConfig().getName(), "timer");
		assertNull(config.getTargetPackageName());
		assertEquals(config.getTargetClassName(), "info.novatec.inspectitsamples.calculator.Calculator");
		assertEquals(config.getTargetMethodName(), "actionPerformed");
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 1);
		assertTrue(config.getParameterTypes().contains("java.lang.String"));
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 0);
		assertNotNull(config.getPropertyAccessorList());
		assertEquals(config.getPropertyAccessorList().size(), 0);
		assertSame(config.getMatcher().getClass(), DirectMatcher.class);

		// fourth
		config = configs.get(7);
		assertEquals(config.getSensorTypeConfig().getName(), "timer");
		assertNull(config.getTargetPackageName());
		assertEquals(config.getTargetClassName(), "info.novatec.IService");
		assertEquals(config.getTargetMethodName(), "*Service");
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 1);
		assertTrue(config.getSettings().containsKey("interface"));
		assertEquals(config.getSettings().get("interface"), "true");
		assertNotNull(config.getPropertyAccessorList());
		assertEquals(config.getPropertyAccessorList().size(), 0);
		assertSame(config.getMatcher().getClass(), InterfaceMatcher.class);

		// fifth
		config = configs.get(8);
		assertEquals(config.getSensorTypeConfig().getName(), "isequence");
		assertNull(config.getTargetPackageName());
		assertEquals(config.getTargetClassName(), "info.novatec.inspectitsamples.calculator.Calculator");
		assertEquals(config.getTargetMethodName(), "actionPerformed");
		assertNotNull(config.getParameterTypes());
		assertEquals(config.getParameterTypes().size(), 0);
		assertNotNull(config.getSettings());
		assertEquals(config.getSettings().size(), 1);
		assertTrue(config.getSettings().containsKey("superclass"));
		assertEquals(config.getSettings().get("superclass"), "true");
		assertNotNull(config.getPropertyAccessorList());
		assertEquals(config.getPropertyAccessorList().size(), 0);
		assertSame(config.getMatcher().getClass(), SuperclassMatcher.class);

		// sixth
		config = configs.get(9);
		assertEquals(config.getPropertyAccessorList().size(), 1);
		assertSame(config.getPropertyAccessorList().get(0).getClass(), PropertyPathStart.class);
		PropertyPathStart start = (PropertyPathStart) config.getPropertyAccessorList().get(0);
		assertEquals(start.getName(), "LastOutput");
		assertEquals(start.getSignaturePosition(), -1);
		assertSame(start.getPathToContinue().getClass(), PropertyPath.class);
		assertEquals(start.getPathToContinue().getName(), "jlbOutput");
		assertSame(start.getPathToContinue().getPathToContinue().getClass(), PropertyPath.class);
		assertEquals(start.getPathToContinue().getPathToContinue().getName(), "text");
		assertNull(start.getPathToContinue().getPathToContinue().getPathToContinue());

		// seventh
		config = configs.get(10);
		assertEquals(config.getPropertyAccessorList().size(), 1);
		assertSame(config.getPropertyAccessorList().get(0).getClass(), PropertyPathStart.class);
		start = (PropertyPathStart) config.getPropertyAccessorList().get(0);
		assertEquals(start.getName(), "Source");
		assertEquals(start.getSignaturePosition(), 0);
		assertSame(start.getPathToContinue().getClass(), PropertyPath.class);
		assertEquals(start.getPathToContinue().getName(), "msg");
		assertNull(start.getPathToContinue().getPathToContinue());

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSensorTypeName() throws StorageException {
		configurationStorage.addSensor(null, "xxx", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySensorTypeName() throws StorageException {
		configurationStorage.addSensor("", "xxx", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSensorTargetClassName() throws StorageException {
		configurationStorage.addSensor("xxx", null, "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySensorTargetClassName() throws StorageException {
		configurationStorage.addSensor("xxx", "", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSensorTargetMethodName() throws StorageException {
		configurationStorage.addSensor("xxx", "xxx", null, null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySensorTargetMethodName() throws StorageException {
		configurationStorage.addSensor("xxx", "xxx", "", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addSensorInvalidSensorTypeName() throws StorageException {
		configurationStorage.addSensor("xxx", "xxx", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void annotationCheck() {
		List<UnregisteredSensorConfig> configs = configurationStorage.getUnregisteredSensorConfigs();
		assertNotNull(configs);
		assertEquals(configs.size(), 12);
		
		UnregisteredSensorConfig annotationConfig = configs.get(11);
		assertNotNull(annotationConfig.getAnnotationClassName());
		assertEquals(annotationConfig.getAnnotationClassName(), "javax.ejb.StatelessBean");
		
		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

}
