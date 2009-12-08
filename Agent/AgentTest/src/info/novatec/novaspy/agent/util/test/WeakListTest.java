package info.novatec.novaspy.agent.util.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.novaspy.util.WeakList;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WeakListTest {

	private WeakList weakList;

	@BeforeMethod
	public void initTestClass() {
		weakList = new WeakList();
	}

	@Test
	public void oneElement() {
		Object object = new Object();
		weakList.add(object);
		Object returnValue = weakList.get(0);

		assertNotNull(returnValue);
		assertSame(returnValue, object);
	}

	@Test
	public void forceGC() {
		Object object = new Object();
		weakList.add(object);
		object = null;
		System.gc();
		Object returnValue = weakList.get(0);

		assertNull(returnValue);
	}

	@Test
	public void mixedList() {
		Object objectOne = new Object();
		Object objectTwo = new Object();
		Object objectThree = new Object();
		Object objectFour = new Object();
		Object objectFive = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);
		weakList.add(objectThree);
		weakList.add(objectFour);
		weakList.add(objectFive);

		objectTwo = null;
		objectFour = null;

		System.gc();

		int size = weakList.size();
		// this should be five as the weak references are not cleaned up yet
		assertEquals(size, 5);

		weakList.removeAllNullElements();
		size = weakList.size();
		// now it should be three
		assertEquals(size, 3);

		assertEquals(weakList.get(0), objectOne);
		assertEquals(weakList.get(1), objectThree);
		assertEquals(weakList.get(2), objectFive);
	}

	@Test
	public void getHardReferences() {
		Object objectOne = new Object();
		Object objectTwo = new Object();
		Object objectThree = new Object();
		Object objectFour = new Object();
		Object objectFive = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);
		weakList.add(objectThree);
		weakList.add(objectFour);
		weakList.add(objectFive);

		objectTwo = null;
		objectFour = null;

		System.gc();

		@SuppressWarnings("unchecked")
		List<Object> list = weakList.getHardReferences();
		assertEquals(list.size(), 3);

		assertEquals(list.get(0), objectOne);
		assertEquals(list.get(1), objectThree);
		assertEquals(list.get(2), objectFive);
	}

	@Test
	public void clearNoGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();
		Object objectThree = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);
		weakList.add(objectThree);

		weakList.clear();

		assertEquals(weakList.size(), 0);
	}

	@Test
	public void clearWithGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();
		Object objectThree = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);
		weakList.add(objectThree);

		objectOne = null;
		objectTwo = null;
		objectThree = null;

		System.gc();

		weakList.clear();

		assertEquals(weakList.size(), 0);
	}

	@Test
	public void containsNoGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);

		objectOne = null;

		assertTrue(weakList.contains(objectTwo));
	}

	@Test
	public void containsWithGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);

		objectOne = null;

		System.gc();

		assertTrue(weakList.contains(objectTwo));
	}

	@Test(expectedExceptions = { IndexOutOfBoundsException.class })
	public void outOfBounds() {
		assertNull(weakList.get(5));
	}

}
