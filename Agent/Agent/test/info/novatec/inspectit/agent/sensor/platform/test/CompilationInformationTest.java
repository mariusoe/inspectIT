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
import info.novatec.inspectit.agent.sensor.platform.CompilationInformation;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.CompilationInformationData;

import java.lang.management.CompilationMXBean;
import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CompilationInformationTest extends AbstractLogSupport {
	
	private CompilationInformation compilationInfo;

	@Mock
	private CompilationMXBean compilationObj;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		compilationInfo = new CompilationInformation(idManager);

		// we have to set the real compilationObj on the mocked one
		Field field = compilationInfo.getClass().getDeclaredField("compilationObj");
		field.setAccessible(true);
		field.set(compilationInfo, compilationObj);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		long totalCompilationTime = 12345L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(compilationObj.getTotalCompilationTime()).thenReturn(totalCompilationTime);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		compilationInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof CompilationInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		CompilationInformationData compilationData = (CompilationInformationData) sensorData;
		assertEquals(compilationData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(compilationData.getMinTotalCompilationTime(), totalCompilationTime);
		assertEquals(compilationData.getMaxTotalCompilationTime(), totalCompilationTime);
		assertEquals(compilationData.getTotalTotalCompilationTime(), totalCompilationTime);
	}

	@Test
	public void twoDataSets() throws IdNotAvailableException {
		long totalCompilationTime = 12345L;
		long totalCompilationTime2 = 12359L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		when(compilationObj.getTotalCompilationTime()).thenReturn(totalCompilationTime);
		compilationInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof CompilationInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		CompilationInformationData compilationData = (CompilationInformationData) sensorData;
		assertEquals(compilationData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(compilationData.getMinTotalCompilationTime(), totalCompilationTime);
		assertEquals(compilationData.getMaxTotalCompilationTime(), totalCompilationTime);
		assertEquals(compilationData.getTotalTotalCompilationTime(), totalCompilationTime);

		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		when(compilationObj.getTotalCompilationTime()).thenReturn(totalCompilationTime2);
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(compilationData);
		compilationInfo.update(coreService, sensorTypeIdent);

		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof CompilationInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		compilationData = (CompilationInformationData) sensorData;
		assertEquals(compilationData.getCount(), 2);

		assertEquals(compilationData.getMinTotalCompilationTime(), totalCompilationTime);
		assertEquals(compilationData.getMaxTotalCompilationTime(), totalCompilationTime2);
		assertEquals(compilationData.getTotalTotalCompilationTime(), totalCompilationTime + totalCompilationTime2);
	}

	@Test
	public void compilationTimeNotAvailable() throws IdNotAvailableException {
		long totalCompilationTime = 0L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(compilationObj.getTotalCompilationTime()).thenThrow(new UnsupportedOperationException("expected"));

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

		compilationInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
		
		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof CompilationInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		CompilationInformationData compilationData = (CompilationInformationData) sensorData;
		assertEquals(compilationData.getCount(), 1);

		// as there was only one data object min/max/total the values must be the
		// same
		assertEquals(compilationData.getMinTotalCompilationTime(), totalCompilationTime);
		assertEquals(compilationData.getMaxTotalCompilationTime(), totalCompilationTime);
		assertEquals(compilationData.getTotalTotalCompilationTime(), totalCompilationTime);
	}

	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		long totalCompilationTime = 12345L;
		long sensorTypeIdent = 13L;

		when(compilationObj.getTotalCompilationTime()).thenReturn(totalCompilationTime);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenThrow(new IdNotAvailableException("expected"));

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

		compilationInfo.update(coreService, sensorTypeIdent);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
	}

	@Override
	protected Level getLogLevel() {
		return Level.FINEST;
	}
}
