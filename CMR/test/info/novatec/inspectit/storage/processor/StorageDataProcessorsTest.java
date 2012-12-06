package info.novatec.inspectit.storage.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.impl.TimerDataAggregator;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.processor.impl.DataAggregatorProcessor;
import info.novatec.inspectit.storage.processor.impl.DataSaverProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationClonerDataProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationExtractorDataProcessor;
import info.novatec.inspectit.storage.processor.impl.TimeFrameDataProcessor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * Tests all {@link AbstractDataProcessor}s for the correct functionality.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class StorageDataProcessorsTest {

	/**
	 * Storage writer.
	 */
	private StorageWriter storageWriter;

	/**
	 * Tests that the {@link DataSaverProcessor} will not save data type if type is not registered
	 * with a processor.
	 */
	@Test
	public void testSimpleSaverExclusive() {
		storageWriter = mock(StorageWriter.class);
		List<Class<? extends DefaultData>> includedClasses = new ArrayList<Class<? extends DefaultData>>();
		includedClasses.add(InvocationSequenceData.class);
		DataSaverProcessor dataSaverProcessor = new DataSaverProcessor(includedClasses, true);
		dataSaverProcessor.setStorageWriter(storageWriter);

		TimerData timerData = new TimerData();
		assertThat(dataSaverProcessor.canBeProcessed(timerData), is(false));

		dataSaverProcessor.process(timerData);
		verifyZeroInteractions(storageWriter);
	}

	/**
	 * Tests that the {@link DataSaverProcessor} will save data type if type is not registered with
	 * a processor.
	 */
	@Test
	public void testSimpleSaverInclusive() {
		storageWriter = mock(StorageWriter.class);
		List<Class<? extends DefaultData>> includedClasses = new ArrayList<Class<? extends DefaultData>>();
		includedClasses.add(InvocationSequenceData.class);
		DataSaverProcessor dataSaverProcessor = new DataSaverProcessor(includedClasses, true);
		dataSaverProcessor.setStorageWriter(storageWriter);

		InvocationSequenceData invocation = new InvocationSequenceData();
		assertThat(dataSaverProcessor.canBeProcessed(invocation), is(true));

		dataSaverProcessor.process(invocation);
		verify(storageWriter, times(1)).write(invocation);
	}

	/**
	 * Test that the {@link InvocationClonerDataProcessor} will clone the
	 * {@link InvocationSequenceData}.
	 */
	@Test
	public void testInvocationCloner() {
		storageWriter = mock(StorageWriter.class);
		InvocationClonerDataProcessor invocationDataProcessor = new InvocationClonerDataProcessor();
		invocationDataProcessor.setStorageWriter(storageWriter);

		InvocationSequenceData invocation = new InvocationSequenceData();
		assertThat(invocationDataProcessor.canBeProcessed(invocation), is(true));

		TimerData timerData = new TimerData();
		assertThat(invocationDataProcessor.canBeProcessed(timerData), is(false));

		invocationDataProcessor.process(invocation);
		verify(storageWriter, times(1)).write(any(InvocationSequenceData.class));
	}

	/**
	 * Test that {@link InvocationExtractorDataProcessor} will extract all children of an
	 * invocation.
	 */
	@Test
	public void testInvocationExtractor() {
		storageWriter = mock(StorageWriter.class);
		AbstractDataProcessor chainedProcessor = mock(AbstractDataProcessor.class);
		List<AbstractDataProcessor> chainedList = new ArrayList<AbstractDataProcessor>();
		chainedList.add(chainedProcessor);

		InvocationExtractorDataProcessor invocationExtractorDataProcessor = new InvocationExtractorDataProcessor(chainedList);
		invocationExtractorDataProcessor.setStorageWriter(storageWriter);

		InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
		List<InvocationSequenceData> children = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData child1 = new InvocationSequenceData(new Timestamp(new Date().getTime()), 10, 10, 10);
		TimerData timerData = new TimerData();
		child1.setTimerData(timerData);

		InvocationSequenceData child2 = new InvocationSequenceData(new Timestamp(new Date().getTime()), 20, 20, 20);
		SqlStatementData sqlStatementData = new SqlStatementData();
		child2.setSqlStatementData(sqlStatementData);

		children.add(child1);
		children.add(child2);
		invocationSequenceData.setNestedSequences(children);

		assertThat(invocationExtractorDataProcessor.canBeProcessed(invocationSequenceData), is(true));
		invocationExtractorDataProcessor.process(invocationSequenceData);
		verify(chainedProcessor, times(1)).process(timerData);
		verify(chainedProcessor, times(1)).process(timerData);
		verify(chainedProcessor, times(0)).process(child1);
		verify(chainedProcessor, times(0)).process(child2);
	}

	/**
	 * Test that {@link TimeFrameDataProcessor} only passed the data that belongs to the given time
	 * frame.
	 */
	@Test
	public void testTimeframeProcessor() {
		DefaultData defaultData = mock(DefaultData.class);
		long time = 10000000;
		long past = time - 1000;
		long future = time + 1000;

		AbstractDataProcessor dataProcessor = mock(AbstractDataProcessor.class);
		List<AbstractDataProcessor> chainedProcessors = new ArrayList<AbstractDataProcessor>();
		chainedProcessors.add(dataProcessor);

		TimeFrameDataProcessor timeFrameDataProcessor = new TimeFrameDataProcessor(new Date(past), new Date(future), chainedProcessors);
		assertThat(timeFrameDataProcessor.canBeProcessed(defaultData), is(true));

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(time));
		timeFrameDataProcessor.process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(past));
		timeFrameDataProcessor.process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(future));
		timeFrameDataProcessor.process(defaultData);

		verify(dataProcessor, times(3)).process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(past - 1000));
		timeFrameDataProcessor.process(defaultData);

		Mockito.when(defaultData.getTimeStamp()).thenReturn(new Timestamp(future + 1000));
		timeFrameDataProcessor.process(defaultData);

		verify(dataProcessor, times(3)).process(defaultData);

	}

	/**
	 * Test the {@link DataAggregatorProcessor} for a correct aggregation of data.
	 */
	@Test
	public void testDataAggregatorProcessor() {
		int aggregationPeriod = 100;
		storageWriter = mock(StorageWriter.class);
		DataAggregatorProcessor<TimerData> dataAggregatorProcessor = new DataAggregatorProcessor<TimerData>(TimerData.class, aggregationPeriod, new TimerDataAggregator(), true);
		dataAggregatorProcessor.setStorageWriter(storageWriter);

		long timestampValue = new Date().getTime();
		Random random = new Random();
		long platformIdent = random.nextLong();
		long sensorTypeIdent = random.nextLong();
		long methodIdent = random.nextLong();
		TimerData timerData = new TimerData(new Timestamp(timestampValue), platformIdent, sensorTypeIdent, methodIdent);

		assertThat(dataAggregatorProcessor.canBeProcessed(timerData), is(true));

		final int elements = 1000;
		for (int i = 0; i < elements / 2; i++) {
			dataAggregatorProcessor.process(timerData);
		}

		dataAggregatorProcessor.flush();

		verify(storageWriter, times(1)).write(Mockito.<TimerData> anyObject());
	}
}
