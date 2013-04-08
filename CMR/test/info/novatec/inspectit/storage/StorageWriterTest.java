package info.novatec.inspectit.storage;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.storage.CmrStorageWriter;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.storage.StorageWriter.WriteTask;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.stream.ExtendedByteBufferOutputStream;
import info.novatec.inspectit.storage.nio.stream.StreamProvider;
import info.novatec.inspectit.storage.nio.write.WritingChannelManager;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.Output;

@SuppressWarnings("PMD")
public class StorageWriterTest {

	private StorageWriter storageWriter;

	@Mock
	private ISerializer serializer;

	@Mock
	private StreamProvider streamProvider;

	@Mock
	private ExtendedByteBufferOutputStream extendedByteBufferOutputStream;

	@Mock
	private WritingChannelManager writingChannelManager;

	@Mock
	private StorageIndexingTreeHandler storageIndexingTreeHandler;

	@Mock
	private StorageManager storageManager;

	@Mock
	private BlockingQueue<ISerializer> serializerQueue;

	@Mock
	private ScheduledExecutorService scheduledExecutorService;

	@SuppressWarnings("rawtypes")
	@Mock
	private ScheduledFuture future;

	@SuppressWarnings({ "unchecked" })
	@BeforeMethod
	public void init() throws IndexingException, InterruptedException {
		MockitoAnnotations.initMocks(this);
		storageWriter = new CmrStorageWriter();
		when(streamProvider.getExtendedByteBufferOutputStream()).thenReturn(extendedByteBufferOutputStream);
		when(storageIndexingTreeHandler.startWrite(Mockito.<WriteTask> anyObject())).thenReturn(1);
		when(storageManager.canWriteMore()).thenReturn(true);
		when(serializerQueue.take()).thenReturn(serializer);
		when(scheduledExecutorService.scheduleWithFixedDelay(Mockito.<Runnable> anyObject(), anyLong(), anyLong(), Mockito.<TimeUnit> anyObject())).thenReturn(future);
		storageWriter.indexingTreeHandler = storageIndexingTreeHandler;
		storageWriter.storageManager = storageManager;
		storageWriter.writingChannelManager = writingChannelManager;
		storageWriter.streamProvider = streamProvider;
		storageWriter.serializerQueue = serializerQueue;
		storageWriter.scheduledExecutorService = scheduledExecutorService;
		storageWriter.log = LogFactory.getLog(storageWriter.getClass());
	}

	@Test
	public void writeTaskWriteNotAllowedByStorageManager() {
		when(storageManager.canWriteMore()).thenReturn(false);
		storageWriter.new WriteTask(new TimerData(), Collections.emptyMap()).run();
		verifyZeroInteractions(storageIndexingTreeHandler, extendedByteBufferOutputStream, streamProvider, serializer, serializerQueue, writingChannelManager);
	}

	@Test
	public void writeTaskFailedIndexing() throws IndexingException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(new IndexingException("Test msg")).when(storageIndexingTreeHandler).startWrite(writeTask);

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(serializer, serializerQueue, streamProvider, writingChannelManager);
	}

	@Test
	public void writeTaskZeroChannelReturnedFromIndexing() throws IndexingException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		when(storageIndexingTreeHandler.startWrite(writeTask)).thenReturn(0);

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(serializer, serializerQueue, streamProvider, writingChannelManager);
	}

	@Test
	public void writeTaskNoSerializerAvailable() throws InterruptedException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		when(serializerQueue.take()).thenReturn(null);

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void writeTaskSerializerQueueInterrupted() throws InterruptedException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(InterruptedException.class).when(serializerQueue).take();

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void writeTaskFailedSerialization() throws SerializationException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(SerializationException.class).when(serializer).serialize(anyObject(), Mockito.<Output> anyObject(), Mockito.<Map<?, ?>> anyObject());

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
		verifyZeroInteractions(writingChannelManager);
	}

	@Test
	public void writeTaskExceptionDuringWrite() throws IOException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(IOException.class).when(writingChannelManager).write(Mockito.<ExtendedByteBufferOutputStream> anyObject(), Mockito.<Path> anyObject(),
				Mockito.<WriteReadCompletionRunnable> anyObject());

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
	}

	@Test
	public void writeTaskThrowableDuringWrite() throws IOException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(Throwable.class).when(writingChannelManager).write(Mockito.<ExtendedByteBufferOutputStream> anyObject(), Mockito.<Path> anyObject(), Mockito.<WriteReadCompletionRunnable> anyObject());

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
	}

	@Test
	public void objectWriteNoSerializerAvailable() throws InterruptedException {
		when(serializerQueue.take()).thenReturn(null);
		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void objectWriteSerializerQueueInterrupted() throws InterruptedException {
		doThrow(InterruptedException.class).when(serializerQueue).take();
		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void objectWriteFailedSerialization() throws SerializationException {
		doThrow(SerializationException.class).when(serializer).serialize(anyObject(), Mockito.<Output> anyObject(), Mockito.<Map<?, ?>> anyObject());
		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
		verifyZeroInteractions(writingChannelManager);
	}

	@Test
	public void objectWriteExceptionDuringWrite() throws IOException {
		StorageData storageData = new StorageData();
		Path testPath = Paths.get("test" + File.separator);
		when(storageManager.getStoragePath(storageData)).thenReturn(testPath);
		storageWriter.prepareForWrite(storageData);
		doThrow(IOException.class).when(writingChannelManager).write(Mockito.<ExtendedByteBufferOutputStream> anyObject(), Mockito.<Path> anyObject(),
				Mockito.<WriteReadCompletionRunnable> anyObject());

		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
		verify(writingChannelManager, times(1)).write(Mockito.<ExtendedByteBufferOutputStream> anyObject(), eq(testPath.resolve("myFile")), Mockito.<WriteReadCompletionRunnable> anyObject());
	}

	@Test
	public void objectWriteThrowableDuringWrite() throws IOException {
		StorageData storageData = new StorageData();
		Path testPath = Paths.get("test" + File.separator);
		when(storageManager.getStoragePath(storageData)).thenReturn(testPath);
		storageWriter.prepareForWrite(storageData);
		doThrow(Throwable.class).when(writingChannelManager).write(Mockito.<ExtendedByteBufferOutputStream> anyObject(), Mockito.<Path> anyObject(), Mockito.<WriteReadCompletionRunnable> anyObject());

		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
		verify(writingChannelManager, times(1)).write(Mockito.<ExtendedByteBufferOutputStream> anyObject(), eq(testPath.resolve("myFile")), Mockito.<WriteReadCompletionRunnable> anyObject());
	}

}
