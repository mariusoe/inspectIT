package info.novatec.inspectit.agent.buffer.test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.buffer.impl.SizeBufferStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SizeBufferStrategyTest {

	private IBufferStrategy bufferStrategy;

	@BeforeMethod
	public void initTestClass() {
		bufferStrategy = new SizeBufferStrategy();
	}

	@Test
	public void addAndRetrieve() {
		bufferStrategy.addMeasurements(Collections.EMPTY_LIST);

		assertTrue(bufferStrategy.hasNext());
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) bufferStrategy.next();
		assertNotNull(list);
		assertSame(list, Collections.EMPTY_LIST);

		assertFalse(bufferStrategy.hasNext());
	}

	@Test
	public void emptyBuffer() {
		assertFalse(bufferStrategy.hasNext());
	}

	@Test(expectedExceptions = { NoSuchElementException.class })
	public void noSuchElementException() {
		bufferStrategy.next();
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void addNullMeasurement() {
		bufferStrategy.addMeasurements(null);
	}

	@Test(expectedExceptions = { NoSuchElementException.class })
	public void exceptionAfterDoubleRetrieve() {
		bufferStrategy.addMeasurements(Collections.EMPTY_LIST);
		bufferStrategy.next();
		bufferStrategy.next();
	}

	@Test
	public void callInit() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("size", "3");
		bufferStrategy.init(Collections.EMPTY_MAP);
	}

	@Test
	public void addElementFullStack() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("size", "3");
		bufferStrategy.init(settings);

		List<Object> listOne = new ArrayList<Object>(0);
		List<Object> listTwo = new ArrayList<Object>(0);
		List<Object> listThree = new ArrayList<Object>(0);
		List<Object> listFour = new ArrayList<Object>(0);
		List<Object> listFive = new ArrayList<Object>(0);

		bufferStrategy.addMeasurements(listOne);
		bufferStrategy.addMeasurements(listTwo);
		bufferStrategy.addMeasurements(listThree);
		bufferStrategy.addMeasurements(listFour);
		bufferStrategy.addMeasurements(listFive);

		assertSame(bufferStrategy.next(), listFive);
		assertSame(bufferStrategy.next(), listFour);
		assertSame(bufferStrategy.next(), listThree);

		assertFalse(bufferStrategy.hasNext());
	}

}
