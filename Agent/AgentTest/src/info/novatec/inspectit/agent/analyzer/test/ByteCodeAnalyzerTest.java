package info.novatec.inspectit.agent.analyzer.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.analyzer.impl.ByteCodeAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.ThrowableMatcher;
import info.novatec.inspectit.agent.analyzer.test.classes.MyTestException;
import info.novatec.inspectit.agent.analyzer.test.classes.TestClass;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.hooking.IHookInstrumenter;
import info.novatec.inspectit.agent.sensor.exception.ExceptionSensor;
import info.novatec.inspectit.agent.sensor.exception.IExceptionSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.javassist.CannotCompileException;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.NotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ByteCodeAnalyzerTest extends AbstractLogSupport {

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IHookInstrumenter hookInstrumenter;

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	private ByteCodeAnalyzer byteCodeAnalyzer;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		byteCodeAnalyzer = new ByteCodeAnalyzer(configurationStorage, hookInstrumenter, classPoolAnalyzer);
	}

	private byte[] getByteCode(String className) throws NotFoundException, IOException, CannotCompileException {
		CtClass ctClass = ClassPool.getDefault().get(className);
		return ctClass.toBytecode();
	}

	@Test
	public void nothingToDo() throws NotFoundException, IOException, CannotCompileException {
		String className = TestClass.class.getName();
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		// as no instrumentation happened, we get a null object
		assertNull(instrumentedByteCode);
	}

	@Test
	public void simpleClassAndMethod() throws NotFoundException, IOException, CannotCompileException {
		String className = TestClass.class.getName();
		String methodName = "voidNullParameter";
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig unregisteredSensorConfig = mock(UnregisteredSensorConfig.class);
		when(unregisteredSensorConfig.getSensorName()).thenReturn("simpleClassAndMethod");
		when(unregisteredSensorConfig.getTargetClassName()).thenReturn(className);
		when(unregisteredSensorConfig.getTargetMethodName()).thenReturn(methodName);
		MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(methodSensorTypeConfig.getClassName()).thenReturn("");
		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(methodSensorTypeConfig.getSensorType()).thenReturn(methodSensor);
		when(unregisteredSensorConfig.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);
		IMatcher matcher = mock(IMatcher.class);
		List<CtMethod> ctMethods = new ArrayList<CtMethod>();
		ctMethods.add(ClassPool.getDefault().getMethod(className, methodName));
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(ctMethods);
		when(unregisteredSensorConfig.getMatcher()).thenReturn(matcher);
		unregisteredSensorConfigs.add(unregisteredSensorConfig);

		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(unregisteredSensorConfigs);

		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		assertNotNull(instrumentedByteCode);
		// nothing was really instrumented, thus the byte code has to be the
		// same
		assertEquals(instrumentedByteCode, byteCode);
	}

	@Test
	public void methodWithOneParameter() throws NotFoundException, IOException, CannotCompileException {
		String className = TestClass.class.getName();
		String methodName = "voidOneParameter";
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig unregisteredSensorConfig = mock(UnregisteredSensorConfig.class);
		when(unregisteredSensorConfig.getSensorName()).thenReturn("methodWithOneParameter");
		when(unregisteredSensorConfig.getTargetClassName()).thenReturn(className);
		when(unregisteredSensorConfig.getTargetMethodName()).thenReturn(methodName);
		MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(methodSensorTypeConfig.getClassName()).thenReturn("");
		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(methodSensorTypeConfig.getSensorType()).thenReturn(methodSensor);
		when(unregisteredSensorConfig.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);
		IMatcher matcher = mock(IMatcher.class);
		List<CtMethod> ctMethods = new ArrayList<CtMethod>();
		ctMethods.add(ClassPool.getDefault().getMethod(className, methodName));
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(ctMethods);
		when(unregisteredSensorConfig.getMatcher()).thenReturn(matcher);
		unregisteredSensorConfigs.add(unregisteredSensorConfig);

		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(unregisteredSensorConfigs);

		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		assertNotNull(instrumentedByteCode);
		// nothing was really instrumented, thus the byte code has to be the
		// same
		assertEquals(instrumentedByteCode, byteCode);
	}

	@Test
	public void exceptionSensorNotActivated() throws NotFoundException, IOException, CannotCompileException {
		String className = MyTestException.class.getName();
		ClassLoader classLoader = MyTestException.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		when(configurationStorage.isExceptionSensorActivated()).thenReturn(false);
		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		// actual class was not a subclass of Throwable, so nothing to
		// instrument
		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
		assertNull(instrumentedByteCode);
	}

	@Test
	public void exceptionSensorActivated() throws NotFoundException, IOException, CannotCompileException {
		String className = MyTestException.class.getName();
		ClassLoader classLoader = MyTestException.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		when(inheritanceAnalyzer.subclassOf(className, "java.lang.Throwable", classPool)).thenReturn(true);
		IExceptionSensor exceptionSensor = mock(ExceptionSensor.class);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn(exceptionSensor.getClass().getName());
		when(sensorTypeConfig.getClassName()).thenReturn(exceptionSensor.getClass().getName());
		when(sensorTypeConfig.getSensorType()).thenReturn(exceptionSensor);
		List<MethodSensorTypeConfig> exceptionSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		exceptionSensorTypes.add(sensorTypeConfig);
		when(configurationStorage.getMethodSensorTypes()).thenReturn(exceptionSensorTypes);
		when(configurationStorage.getExceptionSensorTypes()).thenReturn(exceptionSensorTypes);
		IMatcher superclassMatcher = mock(IMatcher.class);
		when(superclassMatcher.compareClassName(classLoader, className)).thenReturn(true);

		Map<String, String> settings = new HashMap<String, String>();
		settings.put("superclass", "true");

		List<UnregisteredSensorConfig> exceptionSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig config = mock(UnregisteredSensorConfig.class);
		when(config.isConstructor()).thenReturn(true);
		when(config.isInterface()).thenReturn(false);
		when(config.isSuperclass()).thenReturn(true);
		when(config.isVirtual()).thenReturn(false);
		when(config.isIgnoreSignature()).thenReturn(true);
		when(config.getSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(config.getSensorName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(config.getSettings()).thenReturn(settings);
		ThrowableMatcher matcher = new ThrowableMatcher(inheritanceAnalyzer, classPoolAnalyzer, config, superclassMatcher);
		when(config.getMatcher()).thenReturn(matcher);
		exceptionSensorConfigs.add(config);

		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(exceptionSensorConfigs);

		// exception sensor was activated, so the current Throwable class was
		// instrumented
		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
		assertNotNull(instrumentedByteCode);
	}
}
