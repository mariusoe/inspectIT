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
import info.novatec.inspectit.agent.sensor.platform.RuntimeInformation;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.RuntimeInformationData;

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RuntimeInformationTest extends AbstractLogSupport {
	
	private RuntimeInformation runtimeInfo;

	@Mock
	private RuntimeMXBean runtimeObj;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		runtimeInfo = new RuntimeInformation(idManager);

		// we have to set the real runtimeObj on the mocked one
		Field field = runtimeInfo.getClass().getDeclaredField("runtimeObj");
		field.setAccessible(true);
		field.set(runtimeInfo, runtimeObj);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		long uptime = 12345L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(runtimeObj.getUptime()).thenReturn(uptime);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		runtimeInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof RuntimeInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		RuntimeInformationData runtimeData = (RuntimeInformationData) sensorData;
		assertEquals(runtimeData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(runtimeData.getMinUptime(), uptime);
		assertEquals(runtimeData.getMaxUptime(), uptime);
		assertEquals(runtimeData.getTotalUptime(), uptime);
	}

	@Test
	public void twoDataSets() throws IdNotAvailableException {
		long uptime = 12345L;
		long uptime2 = 123559L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		when(runtimeObj.getUptime()).thenReturn(uptime);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		runtimeInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof RuntimeInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		RuntimeInformationData runtimeData = (RuntimeInformationData) sensorData;
		assertEquals(runtimeData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(runtimeData.getMinUptime(), uptime);
		assertEquals(runtimeData.getMaxUptime(), uptime);
		assertEquals(runtimeData.getTotalUptime(), uptime);

		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		when(runtimeObj.getUptime()).thenReturn(uptime2);
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(runtimeData);

		runtimeInfo.update(coreService, sensorTypeIdent);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof RuntimeInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		runtimeData = (RuntimeInformationData) sensorData;
		assertEquals(runtimeData.getCount(), 2);

		assertEquals(runtimeData.getMinUptime(), uptime);
		assertEquals(runtimeData.getMaxUptime(), uptime2);
		assertEquals(runtimeData.getTotalUptime(), uptime + uptime2);
	}

	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		long uptime = 12345L;
		long sensorTypeIdent = 13L;

		when(runtimeObj.getUptime()).thenReturn(uptime);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenThrow(new IdNotAvailableException("expected"));

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		runtimeInfo.update(coreService, sensorTypeIdent);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
	}

	protected Level getLogLevel() {
		return Level.FINEST;
	}

}
