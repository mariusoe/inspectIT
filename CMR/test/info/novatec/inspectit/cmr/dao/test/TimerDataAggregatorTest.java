package info.novatec.inspectit.cmr.dao.test;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.dao.impl.TimerDataAggregator;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of {@link TimerDataAggregator}.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-property.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml" })
public class TimerDataAggregatorTest extends AbstractTransactionalTestNGLogSupport {

	private static final int CLEANING_PERIOD = 10000;

	private long initCleanPeriod;

	@Autowired
	private TimerDataAggregator aggregator;

	@BeforeClass
	public void initOwnValues() {
		initCleanPeriod = aggregator.getCacheCleanSleepingPeriod();
		aggregator.setCacheCleanSleepingPeriod(CLEANING_PERIOD);
	}

	/**
	 * Test for the validity of aggregation.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testAggregation() {
		TimerData timerData = mock(TimerData.class);
		long timestampValue = new Date().getTime();
		long platformIdent = new Random().nextLong();
		when(timerData.getPlatformIdent()).thenReturn(platformIdent);

		final long count = 2;
		final double min = 1;
		final double max = 2;
		final double average = 1.5;
		final double duration = 3;

		when(timerData.getCount()).thenReturn(count);
		when(timerData.getAverage()).thenReturn(average);
		when(timerData.getMin()).thenReturn(min);
		when(timerData.getMax()).thenReturn(max);
		when(timerData.getDuration()).thenReturn(duration);

		when(timerData.getCpuAverage()).thenReturn(average);
		when(timerData.getCpuMin()).thenReturn(min);
		when(timerData.getCpuMax()).thenReturn(max);
		when(timerData.getCpuDuration()).thenReturn(duration);

		when(timerData.getExclusiveCount()).thenReturn(count);
		when(timerData.getExclusiveMin()).thenReturn(min);
		when(timerData.getExclusiveMax()).thenReturn(max);
		when(timerData.getExclusiveDuration()).thenReturn(duration);

		final int elements = 1000;

		when(timerData.getTimeStamp()).thenReturn(new Timestamp(timestampValue));
		when(timerData.getMethodIdent()).thenReturn(50L);
		for (int i = 0; i < elements / 2; i++) {
			aggregator.processTimerData(timerData);
		}

		when(timerData.getTimeStamp()).thenReturn(new Timestamp(timestampValue * 2));
		when(timerData.getMethodIdent()).thenReturn(100L);
		for (int i = 0; i < elements / 2; i++) {
			aggregator.processTimerData(timerData);
		}

		// sleep until all objects are persisted
		try {
			Thread.sleep(initCleanPeriod + CLEANING_PERIOD * 2);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}

		int totalCount = 0;
		List<TimerData> persisted = aggregator.getHibernateTemplate().loadAll(TimerData.class);
		for (TimerData persistedTimerData : persisted) {
			if (persistedTimerData.getPlatformIdent() == platformIdent) {
				Assert.assertEquals(0, persistedTimerData.getCount() % count);

				Assert.assertEquals(min, persistedTimerData.getMin());
				Assert.assertEquals(max, persistedTimerData.getMax());
				Assert.assertEquals(average, persistedTimerData.getAverage());
				Assert.assertEquals(average, persistedTimerData.getDuration() / persistedTimerData.getCount());

				Assert.assertEquals(min, persistedTimerData.getCpuMin());
				Assert.assertEquals(max, persistedTimerData.getCpuMax());
				Assert.assertEquals(average, persistedTimerData.getCpuAverage());
				Assert.assertEquals(average, persistedTimerData.getCpuDuration() / persistedTimerData.getCount());

				Assert.assertEquals(min, persistedTimerData.getExclusiveMin());
				Assert.assertEquals(max, persistedTimerData.getExclusiveMax());
				Assert.assertEquals(average, persistedTimerData.getExclusiveAverage());

				totalCount += persistedTimerData.getCount();
				aggregator.getHibernateTemplate().delete(persistedTimerData);
			}
		}

		Assert.assertEquals(count * elements, totalCount);
	}

	/**
	 * Verify the zero interactions with setters of {@link TimerData} object passed to the
	 * aggregator.
	 */
	@Test
	public void testNoSetterInteractions() {
		TimerData timerData = mock(TimerData.class);
		when(timerData.getTimeStamp()).thenReturn(new Timestamp(new Date().getTime()));
		when(timerData.getPlatformIdent()).thenReturn(10L);
		when(timerData.getMethodIdent()).thenReturn(20L);

		aggregator.processTimerData(timerData);

		verify(timerData, times(0)).setAverage(anyDouble());
		verify(timerData, times(0)).setCount(anyLong());
		verify(timerData, times(0)).setCpuAverage(anyDouble());
		verify(timerData, times(0)).setCpuDuration(anyDouble());
		verify(timerData, times(0)).setCpuMax(anyDouble());
		verify(timerData, times(0)).setCpuMin(anyDouble());
		verify(timerData, times(0)).setDuration(anyDouble());
		verify(timerData, times(0)).setExclusiveCount(anyLong());
		verify(timerData, times(0)).setExclusiveDuration(anyDouble());
		verify(timerData, times(0)).setExclusiveMax(anyDouble());
		verify(timerData, times(0)).setExclusiveMin(anyDouble());
		verify(timerData, times(0)).setId(anyLong());
		verify(timerData, times(0)).setInvocationParentsIdSet((Set<?>) anyObject());
		verify(timerData, times(0)).setMax(anyDouble());
		verify(timerData, times(0)).setMethodIdent(anyLong());
		verify(timerData, times(0)).setMin(anyDouble());
		verify(timerData, times(0)).setObjectsInInvocationsCount(anyLong());
		verify(timerData, times(0)).setParameterContentData((Set<?>) anyObject());
		verify(timerData, times(0)).setPlatformIdent(anyLong());
		verify(timerData, times(0)).setSensorTypeIdent(anyLong());
		verify(timerData, times(0)).setTimeStamp((Timestamp) anyObject());
		verify(timerData, times(0)).setVariance(anyDouble());
	}
}
