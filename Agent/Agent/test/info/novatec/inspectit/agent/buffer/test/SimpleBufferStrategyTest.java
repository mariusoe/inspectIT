package info.novatec.inspectit.agent.buffer.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy;
import info.novatec.inspectit.communication.MethodSensorData;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleBufferStrategyTest {

	private IBufferStrategy<MethodSensorData> bufferStrategy;

	@BeforeMethod
	public void initTestClass() {
		bufferStrategy = new SimpleBufferStrategy();
	}

	@Test
	public void addAndRetrieve() {
		bufferStrategy.addMeasurements(Collections.<MethodSensorData> emptyList());

		assertThat(bufferStrategy.hasNext(), is(true));
		List<MethodSensorData> list = bufferStrategy.next();
		assertThat(list, is(notNullValue()));
		assertThat(list, is(equalTo(Collections.<MethodSensorData> emptyList())));

		assertThat(bufferStrategy.hasNext(), is(false));
	}

	@Test
	public void emptyBuffer() {
		assertThat(bufferStrategy.hasNext(), is(false));
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
		bufferStrategy.addMeasurements(Collections.<MethodSensorData> emptyList());
		bufferStrategy.next();
		bufferStrategy.next();
	}

	@Test
	public void callInit() {
		bufferStrategy.init(Collections.<String, String> emptyMap());
	}

}
