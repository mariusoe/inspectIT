package info.novatec.inspectit.agent.analyzer.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.ClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.test.classes.TestClass;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtMethod;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.logging.Level;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class ClassPoolAnalyzerTest extends AbstractLogSupport {

	private IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod
	public void init() {
		classPoolAnalyzer = new ClassPoolAnalyzer();
	}

	@Test
	public void getMethodsForClassName() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(TestClass.class.getClassLoader(), TestClass.class.getName());
		assertNotNull(ctMethods);
		assertEquals(ctMethods.length, TestClass.class.getDeclaredMethods().length);
	}

	@Test
	public void getMethodsForClassNameNullClassLoader() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(null, TestClass.class.getName());
		assertNotNull(ctMethods);
		assertEquals(ctMethods.length, 0);
	}

	@Test
	public void getMethodsForClassNameNullClassName() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(TestClass.class.getClassLoader(), null);
		assertNotNull(ctMethods);
		assertEquals(ctMethods.length, 0);
	}

	@Test
	public void getMethodsForClassNameEmptyClassName() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(TestClass.class.getClassLoader(), "");
		assertNotNull(ctMethods);
		assertEquals(ctMethods.length, 0);
	}

	@Test
	public void equalClassPool() {
		ClassPool classPool = classPoolAnalyzer.addClassLoader(TestClass.class.getClassLoader());
		assertNotNull(classPool);
		ClassPool otherClassPool = classPoolAnalyzer.getClassPool(TestClass.class.getClassLoader());
		assertNotNull(otherClassPool);
		assertSame(classPool, otherClassPool);
	}

	@Test
	public void extClassLoaderParent() {
		ClassPool classPool = classPoolAnalyzer.getClassPool(TestClass.class.getClassLoader());
		assertNotNull(classPool);
		ClassPool appClassPool = classPoolAnalyzer.getClassPool(TestClass.class.getClassLoader().getParent());
		assertNotNull(appClassPool);
		assertTrue(appClassPool.getClassLoader().toString().contains("AppClassLoader"));
	}

	private class TestClassLoader extends ClassLoader {
		public TestClassLoader(ClassLoader parent) {
			super(parent);
		}
	}

	@Test
	public void classLoaderHierarchy() throws Exception {
		TestClassLoader testClassLoader = new TestClassLoader(TestClass.class.getClassLoader());
		TestClassLoader subTestClassLoader = new TestClassLoader(testClassLoader);
		TestClassLoader subSubTestClassLoader = new TestClassLoader(subTestClassLoader);

		classPoolAnalyzer.addClassLoader(subSubTestClassLoader);

		ClassPool classPool = classPoolAnalyzer.getClassPool(subSubTestClassLoader);
		assertSame(getClassLoader(classPool), subSubTestClassLoader);
		ClassPool parentClassPool = getParentClassPool(classPool);
		assertSame(getClassLoader(parentClassPool), subTestClassLoader);
		ClassPool parentParentClassPool = getParentClassPool(parentClassPool);
		assertSame(getClassLoader(parentParentClassPool), testClassLoader);
	}

	private ClassPool getParentClassPool(ClassPool classPool) throws Exception {
		// only possible through reflection :(
		Field field = ClassPool.class.getDeclaredField("parent");
		field.setAccessible(true);
		return (ClassPool) field.get(classPool);
	}

	@SuppressWarnings("unchecked")
	private ClassLoader getClassLoader(ClassPool classPool) throws Exception {
		// more reflection, yay!
		Field field = ClassPool.class.getDeclaredField("source");
		field.setAccessible(true);
		Object classPoolTail = field.get(classPool);
		field = classPoolTail.getClass().getDeclaredField("pathList");
		field.setAccessible(true);
		Object classPathList = field.get(classPoolTail);
		field = classPathList.getClass().getDeclaredField("path");
		field.setAccessible(true);
		Object classPath = field.get(classPathList);
		field = classPath.getClass().getDeclaredField("clref");
		field.setAccessible(true);
		WeakReference<ClassLoader> weakReference = (WeakReference<ClassLoader>) field.get(classPath);
		return weakReference.get();
	}

}
