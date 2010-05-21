package info.novatec.inspectit.agent.analyzer.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import info.novatec.inspectit.agent.analyzer.impl.SuperclassMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.test.MockInit;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SuperclassMatcherTest extends MockInit {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, mock(IInheritanceAnalyzer.class));
		unregisteredSensorConfig.setSensorName("testSensor");
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(false);
		unregisteredSensorConfig.setSuperclass(true);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.AbstractTest");
		unregisteredSensorConfig.setTargetMethodName("testMethod");
		List<String> parameterList = new ArrayList<String>();
		parameterList.add("java.lang.String");
		unregisteredSensorConfig.setParameterTypes(parameterList);
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.EMPTY_MAP);

		matcher = new SuperclassMatcher(inheritanceAnalyzer, classPoolAnalyzer, unregisteredSensorConfig);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void compareClassName() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		CtClass ctClass = mock(CtClass.class);
		when(ctClass.getName()).thenReturn("info.novatec.test.AbstractTest");
		when(iterator.next()).thenReturn(ctClass);

		String className = "info.novatec.test.Test";
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, className)).thenReturn(iterator);
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), className);
		assertTrue(compareResult);

		verifyZeroInteractions(classPoolAnalyzer);
		verify(inheritanceAnalyzer, times(1)).getSuperclassIterator(classLoader, className);
		verifyNoMoreInteractions(inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void compareClassNameVirtual() throws NotFoundException {
		unregisteredSensorConfig.setVirtual(true);
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.*");
		// needed as the virtual setting is checked in the constructor of the
		// interface matcher
		matcher = new SuperclassMatcher(inheritanceAnalyzer, classPoolAnalyzer, unregisteredSensorConfig);

		ClassLoader classLoader = this.getClass().getClassLoader();
		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		CtClass ctClass = mock(CtClass.class);
		when(ctClass.getName()).thenReturn("info.novatec.test.AbstractTest");
		when(iterator.next()).thenReturn(ctClass);

		String className = "info.novatec.test.Test";
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, className)).thenReturn(iterator);
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), className);
		assertTrue(compareResult);

		verifyZeroInteractions(classPoolAnalyzer);
		verify(inheritanceAnalyzer, times(1)).getSuperclassIterator(classLoader, className);
		verifyNoMoreInteractions(inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void failCompareClassName() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(false);

		when(inheritanceAnalyzer.getSuperclassIterator(eq(classLoader), anyString())).thenReturn(iterator);
		boolean compareResult = matcher.compareClassName(classLoader, "info.novatec.test.Fail");
		assertFalse(compareResult);
		compareResult = matcher.compareClassName(classLoader, "info.novatec.Fail");
		assertFalse(compareResult);
		compareResult = matcher.compareClassName(classLoader, "info.novatec.*");
		assertFalse(compareResult);
		compareResult = matcher.compareClassName(classLoader, "");
		assertFalse(compareResult);

		verifyZeroInteractions(classPoolAnalyzer);
		verify(inheritanceAnalyzer, times(4)).getSuperclassIterator(eq(classLoader), anyString());
		verifyNoMoreInteractions(inheritanceAnalyzer);
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
