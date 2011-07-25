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
import info.novatec.inspectit.agent.sensor.platform.MemoryInformation;
import info.novatec.inspectit.agent.sensor.platform.provider.MemoryInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.MemoryInformationData;

import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MemoryInformationTest extends AbstractLogSupport {

	private MemoryInformation memoryInfo;

	@Mock
	private MemoryInfoProvider memoryBean;

	@Mock
	private OperatingSystemInfoProvider osBean;

	@Mock
	private MemoryUsage heapMemoryUsage;

	@Mock
	private MemoryUsage nonHeapMemoryUsage;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		memoryInfo = new MemoryInformation(idManager);

		// we have to replace the real osBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		Field field = memoryInfo.getClass().getDeclaredField("osBean");
		field.setAccessible(true);
		field.set(memoryInfo, osBean);

		// we have to replace the real memoryBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		field = memoryInfo.getClass().getDeclaredField("memoryBean");
		field.setAccessible(true);
		field.set(memoryInfo, memoryBean);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		long freePhysicalMemory = 37566L;
		long freeSwapSpace = 578300L;
		long committedVirtualMemorySize = 12345L;
		long usedHeapMemorySize = 3827L;
		long usedNonHeapMemorySize = 12200L;
		long committedHeapMemorySize = 5056L;
		long committedNonHeapMemorySize = 14016L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);
		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory);
		when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace);
		when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize);
		when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize);
		when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize);
		when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize);
		when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		memoryInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof MemoryInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		MemoryInformationData memoryData = (MemoryInformationData) sensorData;
		assertEquals(memoryData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(memoryData.getMinComittedHeapMemorySize(), committedHeapMemorySize);
		assertEquals(memoryData.getMaxComittedHeapMemorySize(), committedHeapMemorySize);
		assertEquals(memoryData.getTotalComittedHeapMemorySize(), committedHeapMemorySize);

		assertEquals(memoryData.getMinComittedNonHeapMemorySize(), committedNonHeapMemorySize);
		assertEquals(memoryData.getMaxComittedNonHeapMemorySize(), committedNonHeapMemorySize);
		assertEquals(memoryData.getTotalComittedNonHeapMemorySize(), committedNonHeapMemorySize);

		assertEquals(memoryData.getMinComittedVirtualMemSize(), committedVirtualMemorySize);
		assertEquals(memoryData.getMaxComittedVirtualMemSize(), committedVirtualMemorySize);
		assertEquals(memoryData.getTotalComittedVirtualMemSize(), committedVirtualMemorySize);

		assertEquals(memoryData.getMinFreePhysMemory(), freePhysicalMemory);
		assertEquals(memoryData.getMaxFreePhysMemory(), freePhysicalMemory);
		assertEquals(memoryData.getTotalFreePhysMemory(), freePhysicalMemory);

		assertEquals(memoryData.getMinFreeSwapSpace(), freeSwapSpace);
		assertEquals(memoryData.getMaxFreeSwapSpace(), freeSwapSpace);
		assertEquals(memoryData.getTotalFreeSwapSpace(), freeSwapSpace);

		assertEquals(memoryData.getMinUsedHeapMemorySize(), usedHeapMemorySize);
		assertEquals(memoryData.getMaxUsedHeapMemorySize(), usedHeapMemorySize);
		assertEquals(memoryData.getTotalUsedHeapMemorySize(), usedHeapMemorySize);

		assertEquals(memoryData.getMinUsedNonHeapMemorySize(), usedNonHeapMemorySize);
		assertEquals(memoryData.getMaxUsedNonHeapMemorySize(), usedNonHeapMemorySize);
		assertEquals(memoryData.getTotalUsedNonHeapMemorySize(), usedNonHeapMemorySize);
	}

	@Test
	public void twoDataSets() throws IdNotAvailableException {
		long freePhysicalMemory = 37566L;
		long freePhysicalMemory2 = 37000L;
		long freeSwapSpace = 578300L;
		long freeSwapSpace2 = 578000L;
		long committedVirtualMemorySize = 12345L;
		long committedVirtualMemorySize2 = 12300L;
		long usedHeapMemorySize = 3827L;
		long usedHeapMemorySize2 = 4000L;
		long usedNonHeapMemorySize = 12200L;
		long usedNonHeapMemorySize2 = 13000L;
		long committedHeapMemorySize = 5056L;
		long committedHeapMemorySize2 = 4000L;
		long committedNonHeapMemorySize = 14016L;
		long committedNonHeapMemorySize2 = 13000L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);
		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory);
		when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace);
		when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize);
		when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize);
		when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize);
		when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize);
		when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		memoryInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof MemoryInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		MemoryInformationData memoryData = (MemoryInformationData) sensorData;
		assertEquals(memoryData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(memoryData.getMinComittedHeapMemorySize(), committedHeapMemorySize);
		assertEquals(memoryData.getMaxComittedHeapMemorySize(), committedHeapMemorySize);
		assertEquals(memoryData.getTotalComittedHeapMemorySize(), committedHeapMemorySize);

		assertEquals(memoryData.getMinComittedNonHeapMemorySize(), committedNonHeapMemorySize);
		assertEquals(memoryData.getMaxComittedNonHeapMemorySize(), committedNonHeapMemorySize);
		assertEquals(memoryData.getTotalComittedNonHeapMemorySize(), committedNonHeapMemorySize);

		assertEquals(memoryData.getMinComittedVirtualMemSize(), committedVirtualMemorySize);
		assertEquals(memoryData.getMaxComittedVirtualMemSize(), committedVirtualMemorySize);
		assertEquals(memoryData.getTotalComittedVirtualMemSize(), committedVirtualMemorySize);

		assertEquals(memoryData.getMinFreePhysMemory(), freePhysicalMemory);
		assertEquals(memoryData.getMaxFreePhysMemory(), freePhysicalMemory);
		assertEquals(memoryData.getTotalFreePhysMemory(), freePhysicalMemory);

		assertEquals(memoryData.getMinFreeSwapSpace(), freeSwapSpace);
		assertEquals(memoryData.getMaxFreeSwapSpace(), freeSwapSpace);
		assertEquals(memoryData.getTotalFreeSwapSpace(), freeSwapSpace);

		assertEquals(memoryData.getMinUsedHeapMemorySize(), usedHeapMemorySize);
		assertEquals(memoryData.getMaxUsedHeapMemorySize(), usedHeapMemorySize);
		assertEquals(memoryData.getTotalUsedHeapMemorySize(), usedHeapMemorySize);

		assertEquals(memoryData.getMinUsedNonHeapMemorySize(), usedNonHeapMemorySize);
		assertEquals(memoryData.getMaxUsedNonHeapMemorySize(), usedNonHeapMemorySize);
		assertEquals(memoryData.getTotalUsedNonHeapMemorySize(), usedNonHeapMemorySize);

		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory2);
		when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace2);
		when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize2);
		when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize2);
		when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize2);
		when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize2);
		when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize2);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(memoryData);
		memoryInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof MemoryInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		memoryData = (MemoryInformationData) sensorData;
		assertEquals(memoryData.getCount(), 2);

		// as there was only one data object min/max/total values must be the
		// same
		assertEquals(memoryData.getMinComittedHeapMemorySize(), committedHeapMemorySize2);
		assertEquals(memoryData.getMaxComittedHeapMemorySize(), committedHeapMemorySize);
		assertEquals(memoryData.getTotalComittedHeapMemorySize(), committedHeapMemorySize + committedHeapMemorySize2);

		assertEquals(memoryData.getMinComittedNonHeapMemorySize(), committedNonHeapMemorySize2);
		assertEquals(memoryData.getMaxComittedNonHeapMemorySize(), committedNonHeapMemorySize);
		assertEquals(memoryData.getTotalComittedNonHeapMemorySize(), committedNonHeapMemorySize + committedNonHeapMemorySize2);

		assertEquals(memoryData.getMinComittedVirtualMemSize(), committedVirtualMemorySize2);
		assertEquals(memoryData.getMaxComittedVirtualMemSize(), committedVirtualMemorySize);
		assertEquals(memoryData.getTotalComittedVirtualMemSize(), committedVirtualMemorySize + committedVirtualMemorySize2);

		assertEquals(memoryData.getMinFreePhysMemory(), freePhysicalMemory2);
		assertEquals(memoryData.getMaxFreePhysMemory(), freePhysicalMemory);
		assertEquals(memoryData.getTotalFreePhysMemory(), freePhysicalMemory + freePhysicalMemory2);

		assertEquals(memoryData.getMinFreeSwapSpace(), freeSwapSpace2);
		assertEquals(memoryData.getMaxFreeSwapSpace(), freeSwapSpace);
		assertEquals(memoryData.getTotalFreeSwapSpace(), freeSwapSpace + freeSwapSpace2);

		assertEquals(memoryData.getMinUsedHeapMemorySize(), usedHeapMemorySize);
		assertEquals(memoryData.getMaxUsedHeapMemorySize(), usedHeapMemorySize2);
		assertEquals(memoryData.getTotalUsedHeapMemorySize(), usedHeapMemorySize + usedHeapMemorySize2);

		assertEquals(memoryData.getMinUsedNonHeapMemorySize(), usedNonHeapMemorySize);
		assertEquals(memoryData.getMaxUsedNonHeapMemorySize(), usedNonHeapMemorySize2);
		assertEquals(memoryData.getTotalUsedNonHeapMemorySize(), usedNonHeapMemorySize + usedNonHeapMemorySize2);
	}

	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		long freePhysicalMemory = 37566L;
		long freeSwapSpace = 578300L;
		long committedVirtualMemorySize = 12345L;
		long usedHeapMemorySize = 3827L;
		long usedNonHeapMemorySize = 12200L;
		long committedHeapMemorySize = 5056L;
		long committedNonHeapMemorySize = 14016L;
		long sensorTypeIdent = 13L;

		when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

		when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory);
		when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace);
		when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize);
		when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize);
		when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize);
		when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize);
		when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenThrow(new IdNotAvailableException("expected"));

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		memoryInfo.update(coreService, sensorTypeIdent);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
	}

	protected Level getLogLevel() {
		return Level.FINEST;
	}
}
