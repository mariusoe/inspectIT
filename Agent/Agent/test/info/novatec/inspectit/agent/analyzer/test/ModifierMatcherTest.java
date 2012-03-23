package info.novatec.inspectit.agent.analyzer.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.analyzer.impl.ModifierMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.test.MockInit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ModifierMatcherTest extends MockInit {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	private IMatcher delegateMatcher;

	public ModifierMatcherTest() {
		super();
	}

	private ModifierMatcherTest(int dummy) {
		super();
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, mock(IInheritanceAnalyzer.class));
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(false);
		unregisteredSensorConfig.setSuperclass(false);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.Test");
		unregisteredSensorConfig.setTargetMethodName("t*Method");
		unregisteredSensorConfig.setParameterTypes(Collections.EMPTY_LIST);
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.EMPTY_MAP);

		delegateMatcher = mock(IMatcher.class);
		matcher = new ModifierMatcher(classPoolAnalyzer, unregisteredSensorConfig, delegateMatcher);
	}

	private void testPrivateMethod() {
	}

	protected void testProtectedMethod() {
	}

	public void testPublicMethod() {
	}

	void testDefaultMethod() {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPrivate() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PRIVATE);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertNotNull(ctMethodList);
		assertTrue(!ctMethodList.isEmpty());
		for (CtMethod method : ctMethodList) {
			assertTrue(Modifier.isPrivate(method.getModifiers()));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProtected() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PROTECTED);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertNotNull(ctMethodList);
		assertTrue(!ctMethodList.isEmpty());
		for (CtMethod method : ctMethodList) {
			assertTrue(Modifier.isProtected(method.getModifiers()));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPublic() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PUBLIC);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertNotNull(ctMethodList);
		assertTrue(!ctMethodList.isEmpty());
		for (CtMethod method : ctMethodList) {
			assertTrue(Modifier.isPublic(method.getModifiers()));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDefault() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(ModifierMatcher.DEFAULT);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertNotNull(ctMethodList);
		assertTrue(!ctMethodList.isEmpty());
		for (CtMethod method : ctMethodList) {
			assertTrue(Modifier.isPackage(method.getModifiers()));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCombined() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PRIVATE | Modifier.PROTECTED);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertNotNull(ctMethodList);
		assertTrue(!ctMethodList.isEmpty());
		for (CtMethod method : ctMethodList) {
			assertTrue(Modifier.isPrivate(method.getModifiers()) || Modifier.isProtected(method.getModifiers()));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConstructor() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PRIVATE);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingConstructors(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctConstructors));

		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, "info.novatec.test.Test");
		assertNotNull(ctConstructorList);
		assertTrue(!ctConstructorList.isEmpty());
		for (CtConstructor constructor : ctConstructorList) {
			assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		}

	}
}
