package info.novatec.inspectit.cmr.cache.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.RuntimeInformationData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.javamex.classmexer.MemoryUtil;
import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

/**
 * This test class provides test for the method of {@link IObjectSizes} that calculate the size of
 * the core Java classes.
 * 
 * @author Ivan Senic
 * 
 */
public class MemoryCalculationTest extends AbstractTestNGLogSupport {

	/**
	 * Our classes to be tested.
	 */
	public static final Object[][] TESTING_CLASSES = new Object[][] { { TimerData.class }, { SqlStatementData.class }, { ExceptionSensorData.class }, { InvocationSequenceData.class },
			{ ClassLoadingInformationData.class }, { CompilationInformationData.class }, { MemoryInformationData.class }, { RuntimeInformationData.class }, { SystemInformationData.class },
			{ ThreadInformationData.class }, { HttpTimerData.class } };

	/**
	 * Amount that we add to each hash map because of the entry, key and value set safety.
	 */
	private static final long HASH_MAP_SAFTY_DELTA = 16;

	/**
	 * {@link IObjectSizes}.
	 */
	private IObjectSizes objectSizes;

	/**
	 * Gets the proper instance of the {@link IObjectSizes} because it s dependent on the system
	 * test is run on.
	 * 
	 * @throws Exception
	 *             If Exception occurs.
	 */
	@BeforeClass
	public void initCorrectObjectSizesInstance() throws Exception {
		objectSizes = new ObjectSizesFactory().getObject();
	}

	/**
	 * Tests size of a simple {@link Object}.
	 */
	@Test
	public void object() {
		Object o = new Object();
		long ourSize = objectSizes.getSizeOfObjectObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(o, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Long}.
	 */
	@Test
	public void longObject() {
		Long l = Long.valueOf(1);
		long ourSize = objectSizes.getSizeOfLongObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(l, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Integer}.
	 */
	@Test
	public void integerObject() {
		Integer i = Integer.valueOf(1);
		long ourSize = objectSizes.getSizeOfIntegerObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(i, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Short}.
	 */
	@Test
	public void shortObject() {
		Short s = Short.valueOf((short) 1);
		long ourSize = objectSizes.getSizeOfShortObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(s, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Character}.
	 */
	@Test
	public void characterObject() {
		Character s = Character.valueOf('c');
		long ourSize = objectSizes.getSizeOfCharacterObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(s, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a random {@link String} with length from 1 - 100 characters.
	 */
	@Test(invocationCount = 50)
	public void string() {
		int size = (int) (Math.random() * 100);
		String s = RandomStringUtils.random(size);
		long ourSize = objectSizes.getSizeOf(s);
		long theirSize = MemoryUtil.deepMemoryUsageOf(s, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Timestamp}.
	 */
	@Test
	public void timestamp() {
		Timestamp t = new Timestamp(new Date().getTime());
		long ourSize = objectSizes.getSizeOf(t);
		long theirSize = MemoryUtil.deepMemoryUsageOf(t, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Test the array of size 0 and 1.
	 */
	@Test
	public void emptyArray() {
		Object[] array = new Object[0];
		long ourSize = objectSizes.getSizeOfArray(array.length);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));

		array = new Object[1];
		ourSize = objectSizes.getSizeOfArray(array.length);
		theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * General test for arrays of different sizes.
	 */
	@Test(invocationCount = 50)
	public void array() {
		Object[] array = new Object[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfArray(array.length);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("Empty array", ourSize, is(equalTo(theirSize)));

		for (int i = 0; i < array.length; i++) {
			array[i] = new Object();
		}
		ourSize = objectSizes.getSizeOfArray(array.length) + array.length * objectSizes.getSizeOfObjectObject();
		theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("Full array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of empty and populated {@link ArrayList} object.
	 */
	@Test(invocationCount = 50)
	public void arrayList() {
		ArrayList<Object> arrayList = new ArrayList<Object>();
		long ourSize = objectSizes.getSizeOf(arrayList);
		long theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
		assertThat("Empty list", ourSize, is(equalTo(theirSize)));

		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			arrayList.add(new Object());
		}

		ourSize = objectSizes.getSizeOf(arrayList) + size * objectSizes.getSizeOfObjectObject();
		theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
		assertThat("Random list size", ourSize, is(equalTo(theirSize)));
	}
	
	/**
	 * Tests size of empty and populated {@link ArrayList} object with initial size of 0.
	 */
	@Test(invocationCount = 50)
	public void arrayListInitializeZero() {
		ArrayList<Object> arrayList = new ArrayList<Object>(0);
		long ourSize = objectSizes.getSizeOf(arrayList, 0);
		long theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
		assertThat("Empty list", ourSize, is(equalTo(theirSize)));

		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			arrayList.add(new Object());
		}

		ourSize = objectSizes.getSizeOf(arrayList, 0) + size * objectSizes.getSizeOfObjectObject();
		theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
		assertThat("Random list size", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of empty and populated {@link HashMap} object.
	 */
	@Test(invocationCount = 50)
	public void hashMap() {
		HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
		long ourSize = objectSizes.getSizeOfHashMap(0);
		long theirSize = MemoryUtil.deepMemoryUsageOf(hashMap, VisibilityFilter.ALL);
		assertThat("Empty map", ourSize, is(equalTo(theirSize + HASH_MAP_SAFTY_DELTA)));

		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			hashMap.put(new Object(), new Object());
		}

		ourSize = objectSizes.getSizeOfHashMap(hashMap.size()) + 2 * size * objectSizes.getSizeOfObjectObject();
		theirSize = MemoryUtil.deepMemoryUsageOf(hashMap, VisibilityFilter.ALL);
		assertThat("Random map size", ourSize, is(equalTo(theirSize + HASH_MAP_SAFTY_DELTA)));
	}

	/**
	 * Tests size of empty and populated {@link HashSet} object.
	 */
	@Test(invocationCount = 50)
	public void hashSet() {
		HashSet<Object> hashSet = new HashSet<Object>();
		long ourSize = objectSizes.getSizeOfHashSet(hashSet.size());
		long theirSize = MemoryUtil.deepMemoryUsageOf(hashSet, VisibilityFilter.ALL);
		assertThat("Empty set", ourSize, is(equalTo(theirSize + HASH_MAP_SAFTY_DELTA)));

		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			hashSet.add(new Object());
		}

		ourSize = objectSizes.getSizeOfHashSet(hashSet.size()) + size * objectSizes.getSizeOfObjectObject();
		theirSize = MemoryUtil.deepMemoryUsageOf(hashSet, VisibilityFilter.ALL);
		assertThat("Random set size", ourSize, is(equalTo(theirSize + HASH_MAP_SAFTY_DELTA)));
	}

	/**
	 * Tests size of empty and populated {@link ConcurrentHashMap} object. Note that populated
	 * {@link ConcurrentHashMap} is being tested with only 1 segment. It is impossible to provide
	 * the correct size with more segments, because it is unknown what will be the distributon of
	 * elements between segments.
	 */
	@Test(invocationCount = 50)
	public void concurrentHashMap() {
		// we can only precisely calculate random amount of elements with one segment
		int concurrencyLevel = 1;
		ConcurrentHashMap<Object, Object> concurrentHashMap = new ConcurrentHashMap<Object, Object>(16, 0.75f, concurrencyLevel);
		long ourSize = objectSizes.getSizeOfConcurrentHashMap(concurrentHashMap.size(), concurrencyLevel);
		long theirSize = MemoryUtil.deepMemoryUsageOf(concurrentHashMap, VisibilityFilter.ALL);
		assertThat("Empty map", ourSize, is(equalTo(theirSize)));

		concurrentHashMap = new ConcurrentHashMap<Object, Object>(16, 0.75f, 1);
		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			concurrentHashMap.put(new Object(), new Object());
		}

		ourSize = objectSizes.getSizeOfConcurrentHashMap(concurrentHashMap.size(), 1) + size * 2 * objectSizes.getSizeOfObjectObject();
		theirSize = MemoryUtil.deepMemoryUsageOf(concurrentHashMap, VisibilityFilter.ALL);
		assertThat("Random map size of " + size, ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests the arbitrary class created for test purposes.
	 */
	@Test
	public void arbitraryClass() {
		TestClass testObject = new TestClass();
		long ourSize = testObject.getSize(objectSizes);
		long theirSize = MemoryUtil.deepMemoryUsageOf(testObject, VisibilityFilter.ALL);
		assertThat("Test class", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests the inspectIT classes instances created by reflection.
	 * <p>
	 * <b>Important:</b> The {@link SqlStatementData} class has a problem with calculation when Sun
	 * JVM is used. The 8 bytes are calculated more than needed. Thus, we need to have the "closeTo"
	 * assertion until this problem is fixed. This problem is now part of the <a
	 * href="https://jira.novatec-gmbh.de/browse/INSPECTIT-705">INSPECTIT-705</a> ticket.
	 * 
	 * @param defaultDataClass
	 *            Class to test.
	 * @throws InstantiationException
	 *             If {@link InstantiationException} occurs.
	 * @throws IllegalAccessException
	 *             If {@link IllegalAccessException} occurs.
	 */
	@Test(dataProvider = "classProvider")
	public void inspectitClasses(Class<? extends DefaultData> defaultDataClass) throws InstantiationException, IllegalAccessException {
		DefaultData object = defaultDataClass.newInstance();
		long ourSize = object.getObjectSize(objectSizes);
		long theirSize = MemoryUtil.deepMemoryUsageOf(object, VisibilityFilter.ALL);
		assertThat("Size of " + defaultDataClass.getName(), (double) ourSize, is(closeTo((double) theirSize, 8d)));
	}

	/**
	 * Provides classes to be tested.
	 * 
	 * @return Provides classes to be tested.
	 */
	@DataProvider(name = "classProvider")
	public Object[][] classprovider() {
		return TESTING_CLASSES;
	}

	@SuppressWarnings("unused")
	private static class TestClass {

		private boolean booleanField;

		private int intField;

		private long longField;

		private String str = RandomStringUtils.random((int) (Math.random() * 100));

		private long getSize(IObjectSizes objectSizes) {
			long size = objectSizes.getSizeOfObjectHeader();
			size += objectSizes.getPrimitiveTypesSize(1, 1, 1, 0, 1, 0);
			size += objectSizes.getSizeOf(str);
			return objectSizes.alignTo8Bytes(size);
		}
	}

}
