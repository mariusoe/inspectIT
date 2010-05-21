package info.novatec.inspectit.agent.analyzer.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.InheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.test.classes.AbstractSubTest;
import info.novatec.inspectit.agent.analyzer.test.classes.AbstractTest;
import info.novatec.inspectit.agent.analyzer.test.classes.ISubTest;
import info.novatec.inspectit.agent.analyzer.test.classes.ITest;
import info.novatec.inspectit.agent.analyzer.test.classes.ITestTwo;
import info.novatec.inspectit.agent.analyzer.test.classes.MyTestError;
import info.novatec.inspectit.agent.analyzer.test.classes.MyTestException;
import info.novatec.inspectit.agent.analyzer.test.classes.TestClass;
import info.novatec.inspectit.agent.test.MockInit;
import info.novatec.inspectit.javassist.ClassPool;
import info.novatec.inspectit.javassist.CtClass;
import info.novatec.inspectit.javassist.NotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class InheritanceAnalyzerTest extends MockInit {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	private InheritanceAnalyzer inheritanceAnalyzer;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		inheritanceAnalyzer = new InheritanceAnalyzer(classPoolAnalyzer);
	}

	@Test
	public void getSuperclassIterator() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = TestClass.class.getClassLoader();
		String className = TestClass.class.getName();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		// main test
		@SuppressWarnings("unchecked")
		Iterator<CtClass> iterator = inheritanceAnalyzer.getSuperclassIterator(classLoader, className);
		assertNotNull(iterator);
		assertTrue(iterator.hasNext());
		CtClass superclass = iterator.next();
		assertEquals(superclass.getName(), AbstractSubTest.class.getName());

		assertTrue(iterator.hasNext());
		superclass = iterator.next();
		assertEquals(superclass.getName(), AbstractTest.class.getName());

		assertTrue(iterator.hasNext());
		superclass = iterator.next();
		assertEquals(superclass.getName(), Object.class.getName());

		assertFalse(iterator.hasNext());

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void getInterfaceIterator() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = TestClass.class.getClassLoader();
		String className = TestClass.class.getName();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		List<CtClass> interfaceList = new ArrayList<CtClass>(3);
		interfaceList.add(ClassPool.getDefault().get(ITestTwo.class.getName()));
		interfaceList.add(ClassPool.getDefault().get(ISubTest.class.getName()));
		interfaceList.add(ClassPool.getDefault().get(ITest.class.getName()));

		// main test
		@SuppressWarnings("unchecked")
		Iterator<CtClass> iterator = inheritanceAnalyzer.getInterfaceIterator(classLoader, className);
		assertNotNull(iterator);

		assertTrue(iterator.hasNext());
		CtClass interfaceCtClass = iterator.next();
		assertTrue(interfaceList.contains(interfaceCtClass));
		interfaceList.remove(interfaceCtClass);

		assertTrue(iterator.hasNext());
		interfaceCtClass = iterator.next();
		assertTrue(interfaceList.contains(interfaceCtClass));
		interfaceList.remove(interfaceCtClass);

		assertTrue(iterator.hasNext());
		interfaceCtClass = iterator.next();
		assertTrue(interfaceList.contains(interfaceCtClass));
		interfaceList.remove(interfaceCtClass);

		assertFalse(iterator.hasNext());

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void superclassNotFound() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getSuperclassIterator(classLoader, "xxx");
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void interfaceNotFound() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getInterfaceIterator(classLoader, "xxx");
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void superclassNullClassName() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getSuperclassIterator(classLoader, null);
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void interfaceNullClassName() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getInterfaceIterator(classLoader, null);
	}

	@Test
	public void superclassEmptyResult() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = Object.class.getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		@SuppressWarnings("unchecked")
		Iterator<CtClass> iterator = inheritanceAnalyzer.getSuperclassIterator(classLoader, Object.class.getName());
		assertNotNull(iterator);
		assertFalse(iterator.hasNext());

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void interfaceEmptyResult() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = Object.class.getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		@SuppressWarnings("unchecked")
		Iterator<CtClass> iterator = inheritanceAnalyzer.getInterfaceIterator(classLoader, Object.class.getName());
		assertNotNull(iterator);
		assertFalse(iterator.hasNext());

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void subclassOfThrowable() {
		// set up everything
		ClassLoader classLoader = MyTestException.class.getClassLoader();
		String className = MyTestException.class.getName();
		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		// main test
		boolean subclassOfThrowable = inheritanceAnalyzer.subclassOf(className, "java.lang.Throwable", classPool);
		assertTrue(subclassOfThrowable);

		boolean subclassOfException = inheritanceAnalyzer.subclassOf(className, "java.lang.Exception", classPool);
		assertTrue(subclassOfException);
	}

	@Test
	public void subclassOfError() {
		// set up everything
		ClassLoader classLoader = MyTestError.class.getClassLoader();
		String className = MyTestError.class.getName();
		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		boolean subclassOfError = inheritanceAnalyzer.subclassOf(className, "java.lang.Error", classPool);
		assertTrue(subclassOfError);
	}

}
