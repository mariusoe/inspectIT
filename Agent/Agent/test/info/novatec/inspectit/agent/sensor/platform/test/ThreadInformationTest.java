package info.novatec.inspectit.agent.sensor.platform.test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.ThreadInformation;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ThreadInformationData;

import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ThreadInformationTest extends AbstractLogSupport {

	private ThreadInformation threadInfo;

	@Mock
	private ThreadMXBean threadObj;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		threadInfo = new ThreadInformation(idManager);

		// we have to set the real threadObj on the mocked one
		Field field = threadInfo.getClass().getDeclaredField("threadObj");
		field.setAccessible(true);
		field.set(threadInfo, threadObj);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		int daemonThreadCount = 5;
		int threadCount = 13;
		int peakThreadCount = 25;
		long totalStartedThreadCount = 55L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(threadObj.getDaemonThreadCount()).thenReturn(daemonThreadCount);
		when(threadObj.getThreadCount()).thenReturn(threadCount);
		when(threadObj.getPeakThreadCount()).thenReturn(peakThreadCount);
		when(threadObj.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		threadInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof ThreadInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		ThreadInformationData threadData = (ThreadInformationData) sensorData;
		assertEquals(threadData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(threadData.getMinDaemonThreadCount(), daemonThreadCount);
		assertEquals(threadData.getMaxDaemonThreadCount(), daemonThreadCount);
		assertEquals(threadData.getTotalDaemonThreadCount(), daemonThreadCount);

		assertEquals(threadData.getMinPeakThreadCount(), peakThreadCount);
		assertEquals(threadData.getMaxPeakThreadCount(), peakThreadCount);
		assertEquals(threadData.getTotalPeakThreadCount(), peakThreadCount);

		assertEquals(threadData.getMinThreadCount(), threadCount);
		assertEquals(threadData.getMaxThreadCount(), threadCount);
		assertEquals(threadData.getTotalThreadCount(), threadCount);

		assertEquals(threadData.getMinTotalStartedThreadCount(), totalStartedThreadCount);
		assertEquals(threadData.getMaxTotalStartedThreadCount(), totalStartedThreadCount);
		assertEquals(threadData.getTotalTotalStartedThreadCount(), totalStartedThreadCount);
	}
	
	@Test
	public void twoDataSets() throws IdNotAvailableException {
		int daemonThreadCount = 5;
		int daemonThreadCount2 = 6;
		int threadCount = 13;
		int threadCount2 = 15;
		int peakThreadCount = 25;
		int peakThreadCount2 = 25;
		long totalStartedThreadCount = 55L;
		long totalStartedThreadCount2 = 60L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		when(threadObj.getDaemonThreadCount()).thenReturn(daemonThreadCount);
		when(threadObj.getThreadCount()).thenReturn(threadCount);
		when(threadObj.getPeakThreadCount()).thenReturn(peakThreadCount);
		when(threadObj.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount);
		
		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		threadInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof ThreadInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		ThreadInformationData threadData = (ThreadInformationData) sensorData;
		assertEquals(threadData.getCount(), 1);

		// as there was only one data object min/max/total values must be the
		// same
		assertEquals(threadData.getMinDaemonThreadCount(), daemonThreadCount);
		assertEquals(threadData.getMaxDaemonThreadCount(), daemonThreadCount);
		assertEquals(threadData.getTotalDaemonThreadCount(), daemonThreadCount);

		assertEquals(threadData.getMinPeakThreadCount(), peakThreadCount);
		assertEquals(threadData.getMaxPeakThreadCount(), peakThreadCount);
		assertEquals(threadData.getTotalPeakThreadCount(), peakThreadCount);

		assertEquals(threadData.getMinThreadCount(), threadCount);
		assertEquals(threadData.getMaxThreadCount(), threadCount);
		assertEquals(threadData.getTotalThreadCount(), threadCount);

		assertEquals(threadData.getMinTotalStartedThreadCount(), totalStartedThreadCount);
		assertEquals(threadData.getMaxTotalStartedThreadCount(), totalStartedThreadCount);
		assertEquals(threadData.getTotalTotalStartedThreadCount(), totalStartedThreadCount);
		
		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		when(threadObj.getDaemonThreadCount()).thenReturn(daemonThreadCount2);
		when(threadObj.getThreadCount()).thenReturn(threadCount2);
		when(threadObj.getPeakThreadCount()).thenReturn(peakThreadCount2);
		when(threadObj.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount2);
		
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(threadData);

		threadInfo.update(coreService, sensorTypeIdent);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof ThreadInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		threadData = (ThreadInformationData) sensorData;
		assertEquals(threadData.getCount(), 2);

		assertEquals(threadData.getMinDaemonThreadCount(), daemonThreadCount);
		assertEquals(threadData.getMaxDaemonThreadCount(), daemonThreadCount2);
		assertEquals(threadData.getTotalDaemonThreadCount(), daemonThreadCount + daemonThreadCount2);

		assertEquals(threadData.getMinPeakThreadCount(), peakThreadCount);
		assertEquals(threadData.getMaxPeakThreadCount(), peakThreadCount2);
		assertEquals(threadData.getTotalPeakThreadCount(), peakThreadCount + peakThreadCount2);

		assertEquals(threadData.getMinThreadCount(), threadCount);
		assertEquals(threadData.getMaxThreadCount(), threadCount2);
		assertEquals(threadData.getTotalThreadCount(), threadCount + threadCount2);

		assertEquals(threadData.getMinTotalStartedThreadCount(), totalStartedThreadCount);
		assertEquals(threadData.getMaxTotalStartedThreadCount(), totalStartedThreadCount2);
		assertEquals(threadData.getTotalTotalStartedThreadCount(), totalStartedThreadCount + totalStartedThreadCount2);
	}

	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		int daemonThreadCount = 5;
		int threadCount = 13;
		int peakThreadCount = 25;
		long totalStartedThreadCount = 55L;
		long sensorTypeIdent = 13L;

		when(threadObj.getDaemonThreadCount()).thenReturn(daemonThreadCount);
		when(threadObj.getThreadCount()).thenReturn(threadCount);
		when(threadObj.getPeakThreadCount()).thenReturn(peakThreadCount);
		when(threadObj.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenThrow(new IdNotAvailableException("expected"));

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		threadInfo.update(coreService, sensorTypeIdent);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
	}

	protected Level getLogLevel() {
		return Level.FINEST;
	}
}
