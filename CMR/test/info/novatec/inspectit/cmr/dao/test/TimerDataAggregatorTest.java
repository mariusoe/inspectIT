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

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of {@link TimerDataAggregator}.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-property.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model-test.xml" })
public class TimerDataAggregatorTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * Inital sleep period.
	 */
	private long initCleanPeriod;

	/**
	 * {@link TimerDataAggregator} to test.
	 */
	@Autowired
	private TimerDataAggregator aggregator;

	/**
	 * Initialize.
	 */
	@BeforeClass
	public void initOwnValues() {
		initCleanPeriod = aggregator.getCacheCleanSleepingPeriod();
	}

	/**
	 * Test for the validity of aggregation.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testAggregation() {
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

		// sleep until all objects are persisted
		try {
			Thread.sleep(initCleanPeriod * 3);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}

		int totalCount = 0;
		DetachedCriteria timerDataCriteria = DetachedCriteria.forClass(TimerData.class);
		timerDataCriteria.add(Restrictions.eq("platformIdent", platformIdent));
		List<TimerData> persisted = aggregator.getHibernateTemplate().findByCriteria(timerDataCriteria);
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
		verify(timerData, times(0)).setInvocationParentsIdSet((Set<?>) anyObject());
		verify(timerData, times(0)).calculateMax(anyDouble());
		verify(timerData, times(0)).setMethodIdent(anyLong());
		verify(timerData, times(0)).calculateMin(anyDouble());
		verify(timerData, times(0)).setObjectsInInvocationsCount(anyLong());
		verify(timerData, times(0)).setParameterContentData((Set<?>) anyObject());
		verify(timerData, times(0)).setPlatformIdent(anyLong());
		verify(timerData, times(0)).setSensorTypeIdent(anyLong());
		verify(timerData, times(0)).setTimeStamp((Timestamp) anyObject());
		verify(timerData, times(0)).setVariance(anyDouble());
	}
}
