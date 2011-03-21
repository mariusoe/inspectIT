package info.novatec.inspectit.storage.nio;

import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.storage.nio.read.ReadingChannelManager;
import info.novatec.inspectit.storage.nio.write.WritingChannelManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test's if the data wrote by a {@link WritingChannelManager} is readable by
 * {@link ReadingChannelManager}.
 *
 * @author Ivan Senic
 *
 */
public class ChannelManagersTest extends AbstractTestNGLogSupport {

	/**
	 * File where data will be written/read.
	 */
	private final Path file = Paths.get("test/testFile.");

	/**
	 * Write manager.
	 */
	private WritingChannelManager writingChannelManager;

	/**
	 * Read manager.
	 */
	private ReadingChannelManager readingChannelManager;

	/**
	 * Initializes the channel managers that will use default executor service.
	 */
	@BeforeClass
	public void initChannelManagers() {
		writingChannelManager = new WritingChannelManager();
		readingChannelManager = new ReadingChannelManager();
	}

	/**
	 * Tests if writing and then reading the set of fixed sizes bytes will be correct.
	 *
	 * @throws IOException
	 *             With {@link IOException}.
	 * @throws InterruptedException
	 *             With {@link InterruptedException}.
	 */
	@Test(invocationCount = 50)
	public void testWriteReadFixedSize() throws IOException, InterruptedException {
		final LinkedBlockingQueue<ByteBuffer> bufferQueue = new LinkedBlockingQueue<ByteBuffer>();

		byte[] bytes = getRandomByteArray();
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
		byteBuffer.put(bytes);

		byteBuffer.flip();
		long position = writingChannelManager.write(byteBuffer, file, new WriteReadCompletionRunnable() {
			@Override
			public void run() {
				byteBuffer.clear();
				bufferQueue.add(byteBuffer);
			}
		});

		final ByteBuffer readBuffer = bufferQueue.take();
		readingChannelManager.read(readBuffer, position, bytes.length, file, new WriteReadCompletionRunnable() {

			@Override
			public void run() {
				bufferQueue.add(readBuffer);
			}
		});

		byte[] readBytes = new byte[bytes.length];
		bufferQueue.take().get(readBytes);
		Assert.assertEquals(bytes, readBytes);

		readingChannelManager.finalize(file);
		writingChannelManager.finalize(file);
	}

	/**
	 * Tests if writing and then reading the set of unknown bytes size will be correct.
	 *
	 * @throws IOException
	 *             With {@link IOException}.
	 * @throws InterruptedException
	 *             With {@link InterruptedException}.
	 */
	@Test(invocationCount = 50)
	public void testWriteReadUnknownSize() throws IOException, InterruptedException {
		final LinkedBlockingQueue<ByteBuffer> bufferQueue = new LinkedBlockingQueue<ByteBuffer>();

		byte[] bytes = getRandomByteArray();
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
		byteBuffer.put(bytes);

		byteBuffer.flip();
		long position = writingChannelManager.write(byteBuffer, file, new WriteReadCompletionRunnable() {
			@Override
			public void run() {
				byteBuffer.clear();
				bufferQueue.add(byteBuffer);
			}
		});

		final ByteBuffer readBuffer = bufferQueue.take();
		readingChannelManager.read(readBuffer, position, 0, file, new WriteReadCompletionRunnable() {

			@Override
			public void run() {
				bufferQueue.add(readBuffer);
			}
		});

		byte[] readBytes = new byte[bytes.length];
		bufferQueue.take().get(readBytes);
		Assert.assertEquals(bytes, readBytes);

		writingChannelManager.finalize(file);
		readingChannelManager.finalize(file);
	}

	/**
	 * Deletes the created file.
	 *
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@AfterTest
	public void deleteFile() throws IOException {
		if (Files.exists(file)) {
			// make sure file is delete-able
			Assert.assertTrue(Files.deleteIfExists(file));
		}
	}

	/**
	 * Random size byte array.
	 *
	 * @return Random size byte array.
	 */
	private static byte[] getRandomByteArray() {
		Random random = new Random();
		// max 10MB
		int length = random.nextInt(20 * 1024 * 1024);
		byte[] array = new byte[length];
		random.nextBytes(array);
		return array;
	}
}
