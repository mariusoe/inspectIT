package info.novatec.inspectit.agent.sensor.platform.test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.method.timer.TimerHook;
import info.novatec.inspectit.agent.sensor.platform.CpuInformation;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.CpuInformationData;

import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sun.management.OperatingSystemMXBean;

public class CpuSensorTest extends AbstractLogSupport {
	
	private CpuInformation cpuInfo;
	
	@Mock
	private OperatingSystemMXBean osObj;
	
	@Mock
	private RuntimeMXBean runtimeObj;
	
	@Mock
	private IIdManager idManager;
	
	@Mock
	private ICoreService coreService;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		cpuInfo = new CpuInformation(idManager, runtimeObj, osObj); 
	}
	
	@Test
	public void firstCPUInfoReceived() throws IdNotAvailableException {
		int availableProc = 1;
		long processCpuTime = 2L;
		long uptime = 5L;
		long sensorType = 13L;
		long platformIdent = 11L;
				
		when(runtimeObj.getUptime()).thenReturn(uptime);
		when(osObj.getAvailableProcessors()).thenReturn(availableProc);
		when(osObj.getProcessCpuTime()).thenReturn(processCpuTime);
		
		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorType)).thenReturn(sensorType);
					
		// no current data object is available
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(null);
						
		cpuInfo.update(coreService, sensorType);
		
		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());
		
		// Cast the parameter to the expected concrete class:
		SystemSensorData parameter = sensorDataCaptor.getValue();
		assertTrue(parameter instanceof CpuInformationData);
		assertEquals(parameter.getPlatformIdent(), platformIdent);
		assertEquals(parameter.getSensorTypeIdent(), sensorType);
		
		CpuInformationData data = (CpuInformationData) parameter;
		assertEquals(data.getCount(), 1);
		
		// CPU usage can only be deduced after two sets of data are captured
		assertEquals(data.getMaxCpuUsage(), 0f, 0.01f);
		assertEquals(data.getMinCpuUsage(), 0f, 0.01f);
		assertEquals(data.getTotalCpuUsage(), 0f, 0.01f);
		
		// CPU usage can only be deduced after two sets of data are captured
		//TODO: this is still incorrect!
		// assertEquals(data.getMaxProcessCpuTime(), 0f);
		// assertEquals(data.getMinProcessCpuTime(), 0f);
		assertEquals(data.getTotalProcessCpuTime(), processCpuTime);	
	}
	
	@Test
	public void twoDataSets () throws IdNotAvailableException {
		int availableProc = 1;
		
		// process cpu time is provided as nanoseconds
		long processCpuTime1 = 200L * 1000 * 1000; // ns representation of 200ms
		long processCpuTime2 = 500L * 1000 * 1000; // ns representation of 500ms 
		
		// uptime is provided in milliseconds
		long uptime1 = 500L; // 500ms
		long uptime2 = 1100L; // 1100ms
		long sensorType = 13L;
		long platformIdent = 11L;
		
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		SystemSensorData parameter = null;
		
		when(runtimeObj.getUptime()).thenReturn(uptime1).thenReturn(uptime2);
		when(osObj.getAvailableProcessors()).thenReturn(availableProc);
		when(osObj.getProcessCpuTime()).thenReturn(processCpuTime1).thenReturn(processCpuTime2);
		
		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorType)).thenReturn(sensorType);
					
		// no current data object is available, second call provides an initialized version. The
		// second call provides the parameter that was internally registered.
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(null);
						
		
		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		
		cpuInfo.update(coreService, sensorType);
		
		// -> The service must create a new one and add it to the storage
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());
		
		// Cast the parameter to the expected concrete class:
		parameter = sensorDataCaptor.getValue();
		assertTrue(parameter instanceof CpuInformationData);
		assertEquals(parameter.getPlatformIdent(), platformIdent);
		assertEquals(parameter.getSensorTypeIdent(), sensorType);
		
		CpuInformationData data = (CpuInformationData) parameter;
		assertEquals(data.getCount(), 1);
		
		// CPU usage can only be deduced after two sets of data are captured
		assertEquals(data.getMaxCpuUsage(), 0f, 0.01f);
		assertEquals(data.getMinCpuUsage(), 0f, 0.01f);
		assertEquals(data.getTotalCpuUsage(), 0f, 0.01f);
		
		// CPU usage can only be deduced after two sets of data are captured
		//TODO: this is still incorrect!
		// assertEquals(data.getMaxProcessCpuTime(), 0f);
		// assertEquals(data.getMinProcessCpuTime(), 0f);
		assertEquals(data.getTotalProcessCpuTime(), processCpuTime1);	
		
		
		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		
		
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(parameter);
		cpuInfo.update(coreService, sensorType);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());
		
		// Cast the parameter to the expected concrete class:
		parameter = sensorDataCaptor.getValue();
		assertTrue(parameter instanceof CpuInformationData);
		assertEquals(parameter.getPlatformIdent(), platformIdent);
		assertEquals(parameter.getSensorTypeIdent(), sensorType);
		
		data = (CpuInformationData) parameter;
		assertEquals(data.getCount(), 2);
		
		// CPU usage can only be deduced after two sets of data are captured
		long process = (processCpuTime2 - processCpuTime1);
		long upAsNano = ((uptime2-uptime1) * 1000 * 1000);
		float expectedUsage = (float) process / upAsNano * 100; 
		assertEquals(data.getMaxCpuUsage(), expectedUsage, 0.01f);
		assertEquals(data.getMinCpuUsage(), 0, 0.01f); // the first data sets was 0
		assertEquals(data.getTotalCpuUsage(), expectedUsage, 0.01f);
		
		// CPU usage can only be deduced after two sets of data are captured
		//TODO: this is still incorrect!
		// assertEquals(data.getMaxProcessCpuTime(), 0f);
		// assertEquals(data.getMinProcessCpuTime(), 0f);
		assertEquals(data.getTotalProcessCpuTime(), processCpuTime2);	
	}
	
	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		int availableProc = 1;
		long processCpuTime = 2L;
		long uptime = 5L;
		long sensorType = 13L;
		long platformIdent = 11L;
				
		when(runtimeObj.getUptime()).thenReturn(uptime);
		when(osObj.getAvailableProcessors()).thenReturn(availableProc);
		when(osObj.getProcessCpuTime()).thenReturn(processCpuTime);
		
		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorType)).thenThrow(new IdNotAvailableException("expected"));
					
		// no current data object is available
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(null);
						
		cpuInfo.update(coreService, sensorType);
		
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());
	}
	
	@Override
	protected Level getLogLevel() {
		return Level.FINEST;
	}

}
