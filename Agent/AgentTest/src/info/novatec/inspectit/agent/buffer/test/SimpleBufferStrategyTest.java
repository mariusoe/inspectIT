package info.novatec.novaspy.agent.buffer.test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.novaspy.agent.buffer.IBufferStrategy;
import info.novatec.novaspy.agent.buffer.impl.SimpleBufferStrategy;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleBufferStrategyTest {

	private IBufferStrategy bufferStrategy;

	@BeforeMethod
	public void initTestClass() {
		bufferStrategy = new SimpleBufferStrategy();
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

	@Test
	public void addAndRemove() {
		bufferStrategy.addMeasurements(Collections.EMPTY_LIST);
		bufferStrategy.remove();
	}

	@Test(expectedExceptions = { NoSuchElementException.class })
	public void exceptionAfterRemove() {
		bufferStrategy.addMeasurements(Collections.EMPTY_LIST);
		bufferStrategy.remove();
		bufferStrategy.next();
	}

	@Test
	public void callInit() {
		bufferStrategy.init(Collections.EMPTY_MAP);
	}

}
