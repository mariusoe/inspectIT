package info.novatec.inspectit.agent.analyzer.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.analyzer.impl.IndirectMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.test.MockInit;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndirectMatcherTest extends MockInit {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, mock(IInheritanceAnalyzer.class));
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(false);
		unregisteredSensorConfig.setSuperclass(false);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.*");
		unregisteredSensorConfig.setTargetMethodName("t*Method");
		List<String> parameterList = new ArrayList<String>();
		parameterList.add("*String");
		unregisteredSensorConfig.setParameterTypes(parameterList);
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.EMPTY_MAP);

		matcher = new IndirectMatcher(classPoolAnalyzer, unregisteredSensorConfig);
	}

	@Test
	public void compareClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "info.novatec.test.Test");
		assertTrue(compareResult);

		verifyZeroInteractions(classPoolAnalyzer);
	}

	@Test
	public void failCompareClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "info.novatec.fail.Test");
		assertFalse(compareResult);

		verifyZeroInteractions(classPoolAnalyzer);
	}

	@Test
	public void emptyClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "");
		assertFalse(compareResult);

		verifyZeroInteractions(classPoolAnalyzer);
	}

	@Test
	public void regexClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "*");
		assertFalse(compareResult);

		verifyZeroInteractions(classPoolAnalyzer);
	}

	public void testMethod() {
	}

	public void testMethod(String msg) {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMatchingMethods() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		CtMethod[] ctMethods = new CtMethod[2];
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		ctMethods[0] = ctClass.getDeclaredMethod("testMethod", null);
		ctMethods[1] = ctClass.getDeclaredMethod("testMethod", new CtClass[] { classPool.get("java.lang.String") });

		// stub the getMethodsForClassName method
		when(classPoolAnalyzer.getMethodsForClassName(classLoader, "info.novatec.test.Test")).thenReturn(ctMethods);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		matcher.checkParameters(ctMethodList);
		assertNotNull(ctMethodList);
		assertEquals(ctMethodList.size(), 1);
		assertEquals(ctMethodList.get(0).getParameterTypes().length, 1);
		assertEquals(ctMethodList.get(0).getParameterTypes()[0].getName(), "java.lang.String");

		verify(classPoolAnalyzer, times(1)).getMethodsForClassName(classLoader, "info.novatec.test.Test");
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMatchingMethodsNoParameter() throws NotFoundException {
		// no parameters for this test
		unregisteredSensorConfig.setParameterTypes(Collections.EMPTY_LIST);

		ClassLoader classLoader = this.getClass().getClassLoader();
		CtMethod[] ctMethods = new CtMethod[2];
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		ctMethods[0] = ctClass.getDeclaredMethod("testMethod", null);
		ctMethods[1] = ctClass.getDeclaredMethod("testMethod", new CtClass[] { classPool.get("java.lang.String") });

		// stub the getMethodsForClassName method
		when(classPoolAnalyzer.getMethodsForClassName(classLoader, "info.novatec.test.Test")).thenReturn(ctMethods);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		matcher.checkParameters(ctMethodList);
		assertNotNull(ctMethodList);
		assertEquals(ctMethodList.size(), 1);
		assertEquals(ctMethodList.get(0).getParameterTypes().length, 0);

		verify(classPoolAnalyzer, times(1)).getMethodsForClassName(classLoader, "info.novatec.test.Test");
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMatchingMethodsIgnoreSignature() throws NotFoundException {
		// ignore the signature, now the result should contain two methods
		unregisteredSensorConfig.setIgnoreSignature(true);

		ClassLoader classLoader = this.getClass().getClassLoader();
		CtMethod[] ctMethods = new CtMethod[2];
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		ctMethods[0] = ctClass.getDeclaredMethod("testMethod", null);
		ctMethods[1] = ctClass.getDeclaredMethod("testMethod", new CtClass[] { classPool.get("java.lang.String") });

		// stub the getMethodsForClassName method
		when(classPoolAnalyzer.getMethodsForClassName(classLoader, "info.novatec.test.Test")).thenReturn(ctMethods);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertNotNull(ctMethodList);
		assertEquals(ctMethodList.size(), 2);
		assertEquals(ctMethodList.get(0).getParameterTypes().length, 0);
		assertEquals(ctMethodList.get(1).getParameterTypes().length, 1);
		assertEquals(ctMethodList.get(1).getParameterTypes()[0].getName(), "java.lang.String");

		verify(classPoolAnalyzer, times(1)).getMethodsForClassName(classLoader, "info.novatec.test.Test");
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMatchingMethodsNoMethods() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		CtMethod[] ctMethods = new CtMethod[0];

		// stub the getMethodsForClassName method
		when(classPoolAnalyzer.getMethodsForClassName(classLoader, "info.novatec.test.Test")).thenReturn(ctMethods);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertNotNull(ctMethodList);
		assertEquals(ctMethodList.size(), 0);

		verify(classPoolAnalyzer, times(1)).getMethodsForClassName(classLoader, "info.novatec.test.Test");
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

}
