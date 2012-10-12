package info.novatec.inspectit.storage.nio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;

import java.nio.ByteBuffer;
import java.util.Random;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link ByteBufferProvider}.
 * 
 * @author Ivan Senic
 * 
 */
public class ByteBufferProviderTest extends AbstractTestNGLogSupport {

	/**
	 * To be tested.
	 */
	private ByteBufferProvider byteBufferProvider;

	/**
	 * Creates new instance before each test.
	 */
	@BeforeMethod
	public void init() {
		byteBufferProvider = new ByteBufferProvider();
	}

	/**
	 * Test that the created capacity of the buffer will be as wanted.
	 */
	@Test(invocationCount = 5)
	public void capacity() {
		int maxCapacity = 1000;
		Random random = new Random();
		int wantedCapacity = random.nextInt(maxCapacity);
		byteBufferProvider.setBufferSize(wantedCapacity);
		byteBufferProvider.setPoolMaxCapacity(maxCapacity);
		byteBufferProvider.init();
		ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
		assertThat(buffer, is(notNullValue()));
		assertThat(buffer.capacity(), is(equalTo(wantedCapacity)));
	}

	/**
	 * Tests that no buffer will be created after max pool capacity has been reached.
	 */
	@Test
	public void creationUntilMax() {
		int maxCapacity = 3;
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(maxCapacity);
		byteBufferProvider.init();
		for (int i = 0; i < maxCapacity; i++) {
			ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
			assertThat(buffer, is(notNullValue()));
		}
		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(0)));
	}

	/**
	 * Tests that a buffer will not be returned to the queue after a release when the available
	 * capacity is above or equal to min capacity.
	 */
	@Test
	public void relaseAfterMin() {
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(3);
		byteBufferProvider.setPoolMinCapacity(1);
		byteBufferProvider.init();

		ByteBuffer buffer1 = byteBufferProvider.acquireByteBuffer();
		ByteBuffer buffer2 = byteBufferProvider.acquireByteBuffer();
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(2L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(0L)));

		byteBufferProvider.releaseByteBuffer(buffer1);
		byteBufferProvider.releaseByteBuffer(buffer2);
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(1L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(1L)));

		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(1)));
	}

	/**
	 * Tests that acquire and release of the buffer will have the correct side effects.
	 */
	@Test
	public void acquireAndRelease() {
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(2);
		byteBufferProvider.setPoolMinCapacity(1);
		byteBufferProvider.init();

		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(0L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(0L)));

		ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
		assertThat(buffer, is(notNullValue()));
		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(0)));
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(1L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(0L)));

		byteBufferProvider.releaseByteBuffer(buffer);
		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(1)));
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(1L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(1L)));
	}
}