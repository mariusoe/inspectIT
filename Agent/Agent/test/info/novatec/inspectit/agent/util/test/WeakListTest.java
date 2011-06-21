package info.novatec.inspectit.agent.util.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.util.WeakList;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WeakListTest {

	private WeakList<Object> weakList;

	@BeforeMethod
	public void initTestClass() {
		weakList = new WeakList<Object>();
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
