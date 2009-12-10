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
import info.novatec.inspectit.agent.analyzer.test.classes.MyTestException;
import info.novatec.inspectit.agent.analyzer.test.classes.TestClass;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.hooking.IHookInstrumenter;
import info.novatec.inspectit.agent.sensor.exception.ExceptionTracingSensor;
import info.novatec.inspectit.agent.sensor.exception.IExceptionTracingSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.test.AbstractLogSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

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

	private ByteCodeAnalyzer byteCodeAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		byteCodeAnalyzer = new ByteCodeAnalyzer(configurationStorage, hookInstrumenter, classPoolAnalyzer, inheritanceAnalyzer);
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

		// when(configurationStorage.isExceptionSensorActivated()).thenReturn(
		// false);
		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		when(inheritanceAnalyzer.subclassOf(className, "java.lang.Throwable", classPool)).thenReturn(false);

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
		IExceptionTracingSensor exceptionSensor = mock(ExceptionTracingSensor.class);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn(exceptionSensor.getClass().getName());
		when(sensorTypeConfig.getClassName()).thenReturn(exceptionSensor.getClass().getName());
		List<MethodSensorTypeConfig> exceptionSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		exceptionSensorTypes.add(sensorTypeConfig);
		when(configurationStorage.getExceptionSensorTypes()).thenReturn(exceptionSensorTypes);
		IMatcher matcher = mock(IMatcher.class);

		Map<String, String> settings = new HashMap<String, String>();
		settings.put("superclass", "true");

		List<UnregisteredSensorConfig> exceptionSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig config = mock(UnregisteredSensorConfig.class);
		when(config.isConstructor()).thenReturn(false);
		when(config.isInterface()).thenReturn(false);
		when(config.isSuperclass()).thenReturn(true);
		when(config.isVirtual()).thenReturn(false);
		when(config.getSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(config.getMatcher()).thenReturn(matcher);
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);

		exceptionSensorConfigs.add(config);
		when(configurationStorage.getExceptionSensorConfigs()).thenReturn(exceptionSensorConfigs);

		// exception sensor was activated, so the current Throwable class was
		// instrumented
		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
		assertNotNull(instrumentedByteCode);
	}

}
