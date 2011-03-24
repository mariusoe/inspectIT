package info.novatec.inspectit.agent.analyzer.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.analyzer.impl.AnnotationMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.test.MockInit;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.CtConstructor;
import info.novatec.inspectit.javassist.CtMethod;
import info.novatec.inspectit.javassist.NotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnotationMatcherTest extends MockInit {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	private IMatcher delegateMatcher;

	public AnnotationMatcherTest() {
		super();
	}

	@TestAnnotation
	public AnnotationMatcherTest(int dummy) {
		super();
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, mock(IInheritanceAnalyzer.class));
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(false);
		unregisteredSensorConfig.setSuperclass(false);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName(this.getClass().getName());
		unregisteredSensorConfig.setTargetMethodName("*");
		unregisteredSensorConfig.setParameterTypes(Collections.EMPTY_LIST);
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.EMPTY_MAP);

		delegateMatcher = mock(IMatcher.class);
		matcher = new AnnotationMatcher(classPoolAnalyzer, unregisteredSensorConfig, delegateMatcher);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAnnotatedMethods() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());
		
		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();
		
		when(delegateMatcher.getMatchingMethods(classLoader, this.getClass().getName())).thenReturn(Arrays.asList(ctMethods));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		
		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, this.getClass().getName());
		assertNotNull(ctMethodList);
		assertTrue(!ctMethodList.isEmpty());
		for (CtMethod method : ctMethodList) {
			assertTrue(method.hasAnnotation(TestAnnotation.class));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAnnotatedConstructors() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());
		
		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();
		
		when(delegateMatcher.getMatchingConstructors(classLoader, this.getClass().getName())).thenReturn(Arrays.asList(ctConstructors));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		
		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, this.getClass().getName());
		assertNotNull(ctConstructorList);
		assertTrue(!ctConstructorList.isEmpty());
		for (CtConstructor constructor : ctConstructorList) {
			assertTrue(constructor.hasAnnotation(TestAnnotation.class));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAnnotatedClass() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());
		
		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(TestClass.class.getName());
		
		// test the methods of annotated class (all should be loaded)
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();
		when(delegateMatcher.getMatchingMethods(classLoader, TestClass.class.getName())).thenReturn(Arrays.asList(ctMethods));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		
		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, TestClass.class.getName());
		assertNotNull(ctMethodList);
		assertTrue(!ctMethodList.isEmpty());
		assertTrue(ctMethodList.size() == ctMethods.length);
		
		// test the constructors of annotated class (all should be loaded)
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();
		when(delegateMatcher.getMatchingConstructors(classLoader, TestClass.class.getName())).thenReturn(Arrays.asList(ctConstructors));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		
		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, TestClass.class.getName());
		assertNotNull(ctConstructorList);
		assertTrue(!ctConstructorList.isEmpty());
		assertTrue(ctConstructorList.size() == ctConstructors.length);
	}
	
	public static @interface TestAnnotation {

	}
	
	@TestAnnotation
	public void testMethod() {
		
	}

	@TestAnnotation
	public static class TestClass {
		
		public void dummyMethod() {
		}
		
	}
}
