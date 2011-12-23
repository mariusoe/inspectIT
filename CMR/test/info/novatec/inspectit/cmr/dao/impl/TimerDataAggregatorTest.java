package info.novatec.inspectit.cmr.dao.impl;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.data.DatabaseAggregatedTimerData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.mockito.ArgumentMatcher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of {@link TimerDataAggregator}.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataAggregatorTest extends AbstractTestNGLogSupport {

	/**
	 * {@link TimerDataAggregator} to test.
	 */
	private TimerDataAggregator aggregator;

	private StatelessSession session;

	/**
	 * Initialize.
	 */
	@BeforeClass
	public void init() {
		SessionFactory factory = mock(SessionFactory.class);
		session = mock(StatelessSession.class);
		Transaction transaction = mock(Transaction.class);
		when(factory.openStatelessSession()).thenReturn(session);
		when(session.beginTransaction()).thenReturn(transaction);

		aggregator = new TimerDataAggregator(factory);
		aggregator.aggregationPeriod = 5l;
		aggregator.cacheCleanSleepingPeriod = 10;
		aggregator.maxElements = 100;
	}

	/**
	 * Test for the validity of aggregation.
	 */
	@Test(enabled = false)
	public void aggregation() {
		long timestampValue = new Date().getTime();
		long platformIdent = new Random().nextLong();

		final long count = 2;
		final double min = 1;
		final double max = 2;
		final double average = 1.5;
		final double duration = 3;

		TimerData timerData = new TimerData();
		timerData.setTimeStamp(new Timestamp(timestampValue));
		timerData.setPlatformIdent(platformIdent);
		timerData.setCount(count);
		timerData.setExclusiveCount(count);
		timerData.setDuration(duration);
		timerData.setCpuDuration(duration);
		timerData.setExclusiveDuration(duration);
		timerData.calculateMin(min);
		timerData.calculateCpuMin(min);
		timerData.calculateExclusiveMin(min);
		timerData.calculateMax(max);
		timerData.calculateCpuMax(max);
		timerData.calculateExclusiveMax(max);
		timerData.setMethodIdent(50L);

		TimerData timerData2 = new TimerData();
		timerData2.setTimeStamp(new Timestamp(timestampValue * 2));
		timerData2.setPlatformIdent(platformIdent);
		timerData2.setCount(count);
		timerData2.setExclusiveCount(count);
		timerData2.setDuration(duration);
		timerData2.setCpuDuration(duration);
		timerData2.setExclusiveDuration(duration);
		timerData2.calculateMin(min);
		timerData2.calculateCpuMin(min);
		timerData2.calculateExclusiveMin(min);
		timerData2.calculateMax(max);
		timerData2.calculateCpuMax(max);
		timerData2.calculateExclusiveMax(max);
		timerData2.setMethodIdent(100L);

		final int elements = 1000;

		for (int i = 0; i < elements / 2; i++) {
			aggregator.processTimerData(timerData);
		}

		for (int i = 0; i < elements / 2; i++) {
			aggregator.processTimerData(timerData2);
		}

		verify(session, timeout(10000).times(2)).insert(argThat(new ArgumentMatcher<TimerData>() {
			@Override
			public boolean matches(Object argument) {
				if (!DatabaseAggregatedTimerData.class.equals(argument.getClass())) {
					return false;
				}
				TimerData timerData = (TimerData) argument;

				Assert.assertEquals(0, timerData.getCount() % count);

				Assert.assertEquals(min, timerData.getMin());
				Assert.assertEquals(max, timerData.getMax());
				Assert.assertEquals(average, timerData.getAverage());
				Assert.assertEquals(average, timerData.getDuration() / timerData.getCount());

				Assert.assertEquals(min, timerData.getCpuMin());
				Assert.assertEquals(max, timerData.getCpuMax());
				Assert.assertEquals(average, timerData.getCpuAverage());
				Assert.assertEquals(average, timerData.getCpuDuration() / timerData.getCount());

				Assert.assertEquals(min, timerData.getExclusiveMin());
				Assert.assertEquals(max, timerData.getExclusiveMax());
				Assert.assertEquals(average, timerData.getExclusiveAverage());

				return true;
			}
		}));
	}

	/**
	 * Verify the zero interactions with setters of {@link TimerData} object passed to the
	 * aggregator.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void noSetterInteractions() {
		TimerData timerData = mock(TimerData.class);
		when(timerData.getTimeStamp()).thenReturn(new Timestamp(new Date().getTime()));
		when(timerData.getPlatformIdent()).thenReturn(10L);
		when(timerData.getMethodIdent()).thenReturn(20L);

		aggregator.processTimerData(timerData);

		verify(timerData, times(0)).setCount(anyLong());
		verify(timerData, times(0)).setCpuDuration(anyDouble());
		verify(timerData, times(0)).calculateCpuMax(anyDouble());
		verify(timerData, times(0)).calculateCpuMin(anyDouble());
		verify(timerData, times(0)).setDuration(anyDouble());
		verify(timerData, times(0)).setExclusiveCount(anyLong());
		verify(timerData, times(0)).setExclusiveDuration(anyDouble());
		verify(timerData, times(0)).calculateExclusiveMax(anyDouble());
		verify(timerData, times(0)).calculateExclusiveMin(anyDouble());
		verify(timerData, times(0)).setId(anyLong());
		verify(timerData, times(0)).calculateMax(anyDouble());
		verify(timerData, times(0)).setMethodIdent(anyLong());
		verify(timerData, times(0)).calculateMin(anyDouble());
		verify(timerData, times(0)).setParameterContentData((Set<ParameterContentData>) anyObject());
		verify(timerData, times(0)).setPlatformIdent(anyLong());
		verify(timerData, times(0)).setSensorTypeIdent(anyLong());
		verify(timerData, times(0)).setTimeStamp((Timestamp) anyObject());
		verify(timerData, times(0)).setVariance(anyDouble());
	}

}
