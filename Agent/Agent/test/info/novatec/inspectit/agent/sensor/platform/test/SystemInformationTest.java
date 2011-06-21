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
import info.novatec.inspectit.agent.sensor.platform.SystemInformation;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.SystemInformationData;

import java.lang.management.CompilationMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sun.management.OperatingSystemMXBean;

public class SystemInformationTest extends AbstractLogSupport {

	private SystemInformation systemInfo;

	@Mock
	private MemoryMXBean memoryObj;

	@Mock
	private OperatingSystemMXBean osObj;

	@Mock
	private RuntimeMXBean runtimeObj;

	@Mock
	private CompilationMXBean compilationObj;

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
		systemInfo = new SystemInformation(idManager);

		// we have to set the real objects on the mocked one
		Field field = systemInfo.getClass().getDeclaredField("memoryObj");
		field.setAccessible(true);
		field.set(systemInfo, memoryObj);

		field = systemInfo.getClass().getDeclaredField("osObj");
		field.setAccessible(true);
		field.set(systemInfo, osObj);

		field = systemInfo.getClass().getDeclaredField("runtimeObj");
		field.setAccessible(true);
		field.set(systemInfo, runtimeObj);

		field = systemInfo.getClass().getDeclaredField("compilationObj");
		field.setAccessible(true);
		field.set(systemInfo, compilationObj);
	}

	@Test
	public void oneStaticDataSet() throws IdNotAvailableException {
		long totalPhysMemory = 775000L;
		long totalSwapSpace = 555000L;
		int availableProcessors = 4;
		String architecture = "i386";
		String osName = "linux";
		String osVersion = "2.26";
		String jitCompilerName = "HotSpot Client Compiler";
		String classPath = "thisIsTheClassPath";
		String bootClassPath = "thisIsTheBootClassPath";
		String libraryPath = "thisIsTheLibraryPath";
		String vmVendor = "Sun Microsystems";
		String vmVersion = "1.5.0_15";
		String vmName = "inspectit-vm";
		String vmSpecName = "Java Virtual Machine";
		long initHeapMemorySize = 4000L;
		long maxHeapMemorySize = 10000L;
		long initNonHeapMemorySize = 12000L;
		long maxNonHeapMemorySize = 14000L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);
		when(memoryObj.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(memoryObj.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

		when(osObj.getArch()).thenReturn(architecture);
		when(osObj.getAvailableProcessors()).thenReturn(availableProcessors);
		when(osObj.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
		when(osObj.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
		when(osObj.getVersion()).thenReturn(osVersion);
		when(osObj.getName()).thenReturn(osName);
		when(compilationObj.getName()).thenReturn(jitCompilerName);
		when(runtimeObj.getClassPath()).thenReturn(classPath);
		when(runtimeObj.getBootClassPath()).thenReturn(bootClassPath);
		when(runtimeObj.getLibraryPath()).thenReturn(libraryPath);
		when(runtimeObj.getName()).thenReturn(vmName);
		when(runtimeObj.getVmVendor()).thenReturn(vmVendor);
		when(runtimeObj.getVmVersion()).thenReturn(vmVersion);
		when(runtimeObj.getSpecName()).thenReturn(vmSpecName);
		when(memoryObj.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
		when(memoryObj.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

		systemInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof SystemInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		SystemInformationData systemData = (SystemInformationData) sensorData;

		// as there was only one data object values must be the
		// same
		assertEquals(systemData.getArchitecture(), architecture);
		assertEquals(systemData.getAvailableProcessors(), availableProcessors);
		assertEquals(systemData.getBootClassPath(), bootClassPath);
		assertEquals(systemData.getClassPath(), classPath);
		assertEquals(systemData.getInitHeapMemorySize(), initHeapMemorySize);
		assertEquals(systemData.getInitNonHeapMemorySize(), initNonHeapMemorySize);
		assertEquals(systemData.getJitCompilerName(), jitCompilerName);
		assertEquals(systemData.getLibraryPath(), libraryPath);
		assertEquals(systemData.getMaxHeapMemorySize(), maxHeapMemorySize);
		assertEquals(systemData.getMaxNonHeapMemorySize(), maxNonHeapMemorySize);
		assertEquals(systemData.getOsName(), osName);
		assertEquals(systemData.getOsVersion(), osVersion);
		assertEquals(systemData.getTotalPhysMemory(), totalPhysMemory);
		assertEquals(systemData.getTotalSwapSpace(), totalSwapSpace);
		assertEquals(systemData.getVmName(), vmName);
		assertEquals(systemData.getVmSpecName(), vmSpecName);
		assertEquals(systemData.getVmVendor(), vmVendor);
		assertEquals(systemData.getVmVersion(), vmVersion);
	}

	/**
	 * This testcase combines different testcases that simulate the absense of
	 * static information. Realizing each case separately would require many
	 * code with almost no additional value.
	 * 
	 * @throws IdNotAvailableException
	 */
	@Test
	public void informationNotAvailable() throws IdNotAvailableException {
		long totalPhysMemory = 775000L;
		long totalSwapSpace = 555000L;
		int availableProcessors = 4;
		String empty = "";
		String jitCompilerName = "HotSpot Client Compiler";
		String vmName = "inspectit-vm";
		long initHeapMemorySize = 4000L;
		long maxHeapMemorySize = 10000L;
		long initNonHeapMemorySize = 12000L;
		long maxNonHeapMemorySize = 14000L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);
		when(memoryObj.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(memoryObj.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

		when(osObj.getArch()).thenThrow(new SecurityException("expected"));
		when(osObj.getAvailableProcessors()).thenReturn(availableProcessors);
		when(osObj.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
		when(osObj.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
		when(osObj.getVersion()).thenThrow(new SecurityException("expected"));
		when(osObj.getName()).thenThrow(new SecurityException("expected"));
		when(compilationObj.getName()).thenReturn(jitCompilerName);
		when(runtimeObj.getClassPath()).thenThrow(new SecurityException("expected"));
		when(runtimeObj.getBootClassPath()).thenThrow(new SecurityException("expected"));
		when(runtimeObj.getLibraryPath()).thenThrow(new SecurityException("expected"));
		when(runtimeObj.getName()).thenReturn(vmName);
		when(runtimeObj.getVmVendor()).thenThrow(new SecurityException("expected"));
		when(runtimeObj.getVmVersion()).thenThrow(new SecurityException("expected"));
		when(runtimeObj.getSpecName()).thenThrow(new SecurityException("expected"));
		when(memoryObj.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
		when(memoryObj.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		systemInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof SystemInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		SystemInformationData systemData = (SystemInformationData) sensorData;

		// as there was only one data object the values must be the
		// same
		assertEquals(systemData.getArchitecture(), empty);
		assertEquals(systemData.getAvailableProcessors(), availableProcessors);
		assertEquals(systemData.getBootClassPath(), empty);
		assertEquals(systemData.getClassPath(), empty);
		assertEquals(systemData.getInitHeapMemorySize(), initHeapMemorySize);
		assertEquals(systemData.getInitNonHeapMemorySize(), initNonHeapMemorySize);
		assertEquals(systemData.getJitCompilerName(), jitCompilerName);
		assertEquals(systemData.getLibraryPath(), empty);
		assertEquals(systemData.getMaxHeapMemorySize(), maxHeapMemorySize);
		assertEquals(systemData.getMaxNonHeapMemorySize(), maxNonHeapMemorySize);
		assertEquals(systemData.getOsName(), empty);
		assertEquals(systemData.getOsVersion(), empty);
		assertEquals(systemData.getTotalPhysMemory(), totalPhysMemory);
		assertEquals(systemData.getTotalSwapSpace(), totalSwapSpace);
		assertEquals(systemData.getVmName(), vmName);
		assertEquals(systemData.getVmSpecName(), empty);
		assertEquals(systemData.getVmVendor(), empty);
		assertEquals(systemData.getVmVersion(), empty);
	}

	@Test
	public void bootClassPathNotSupported() throws IdNotAvailableException {
		long totalPhysMemory = 775000L;
		long totalSwapSpace = 555000L;
		int availableProcessors = 4;
		String architecture = "i386";
		String osName = "linux";
		String osVersion = "2.26";
		String jitCompilerName = "HotSpot Client Compiler";
		String classPath = "thisIsTheClassPath";
		String bootClassPath = "";
		String libraryPath = "thisIsTheLibraryPath";
		String vmVendor = "Sun Microsystems";
		String vmVersion = "1.5.0_15";
		String vmName = "inspectit-vm";
		String vmSpecName = "Java Virtual Machine";
		long initHeapMemorySize = 4000L;
		long maxHeapMemorySize = 10000L;
		long initNonHeapMemorySize = 12000L;
		long maxNonHeapMemorySize = 14000L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);
		when(memoryObj.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(memoryObj.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

		when(osObj.getArch()).thenReturn(architecture);
		when(osObj.getAvailableProcessors()).thenReturn(availableProcessors);
		when(osObj.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
		when(osObj.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
		when(osObj.getVersion()).thenReturn(osVersion);
		when(osObj.getName()).thenReturn(osName);
		when(compilationObj.getName()).thenReturn(jitCompilerName);
		when(runtimeObj.getClassPath()).thenReturn(classPath);
		when(runtimeObj.getBootClassPath()).thenThrow(new UnsupportedOperationException("expected"));
		when(runtimeObj.getLibraryPath()).thenReturn(libraryPath);
		when(runtimeObj.getName()).thenReturn(vmName);
		when(runtimeObj.getVmVendor()).thenReturn(vmVendor);
		when(runtimeObj.getVmVersion()).thenReturn(vmVersion);
		when(runtimeObj.getSpecName()).thenReturn(vmSpecName);
		when(memoryObj.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
		when(memoryObj.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		systemInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof SystemInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		SystemInformationData systemData = (SystemInformationData) sensorData;

		// as there was only one data object values must be the
		// same
		assertEquals(systemData.getArchitecture(), architecture);
		assertEquals(systemData.getAvailableProcessors(), availableProcessors);
		assertEquals(systemData.getBootClassPath(), bootClassPath);
		assertEquals(systemData.getClassPath(), classPath);
		assertEquals(systemData.getInitHeapMemorySize(), initHeapMemorySize);
		assertEquals(systemData.getInitNonHeapMemorySize(), initNonHeapMemorySize);
		assertEquals(systemData.getJitCompilerName(), jitCompilerName);
		assertEquals(systemData.getLibraryPath(), libraryPath);
		assertEquals(systemData.getMaxHeapMemorySize(), maxHeapMemorySize);
		assertEquals(systemData.getMaxNonHeapMemorySize(), maxNonHeapMemorySize);
		assertEquals(systemData.getOsName(), osName);
		assertEquals(systemData.getOsVersion(), osVersion);
		assertEquals(systemData.getTotalPhysMemory(), totalPhysMemory);
		assertEquals(systemData.getTotalSwapSpace(), totalSwapSpace);
		assertEquals(systemData.getVmName(), vmName);
		assertEquals(systemData.getVmSpecName(), vmSpecName);
		assertEquals(systemData.getVmVendor(), vmVendor);
		assertEquals(systemData.getVmVersion(), vmVersion);
	}

	@Test
	public void valueTooLong() throws IdNotAvailableException {
		String tooLong = fillString('x', 10001);
		String limit = fillString('x', 10000);

		long totalPhysMemory = 775000L;
		long totalSwapSpace = 555000L;
		int availableProcessors = 4;
		String architecture = "i386";
		String osName = "linux";
		String osVersion = "2.26";
		String jitCompilerName = "HotSpot Client Compiler";
		String vmVendor = "Sun Microsystems";
		String vmVersion = "1.5.0_15";
		String vmName = "inspectit-vm";
		String vmSpecName = "Java Virtual Machine";
		long initHeapMemorySize = 4000L;
		long maxHeapMemorySize = 10000L;
		long initNonHeapMemorySize = 12000L;
		long maxNonHeapMemorySize = 14000L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);
		when(memoryObj.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(memoryObj.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

		when(osObj.getArch()).thenReturn(architecture);
		when(osObj.getAvailableProcessors()).thenReturn(availableProcessors);
		when(osObj.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
		when(osObj.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
		when(osObj.getVersion()).thenReturn(osVersion);
		when(osObj.getName()).thenReturn(osName);
		when(compilationObj.getName()).thenReturn(jitCompilerName);
		when(runtimeObj.getClassPath()).thenReturn(tooLong);
		when(runtimeObj.getBootClassPath()).thenReturn(tooLong);
		when(runtimeObj.getLibraryPath()).thenReturn(tooLong);
		when(runtimeObj.getName()).thenReturn(vmName);
		when(runtimeObj.getVmVendor()).thenReturn(vmVendor);
		when(runtimeObj.getVmVersion()).thenReturn(vmVersion);
		when(runtimeObj.getSpecName()).thenReturn(vmSpecName);
		when(memoryObj.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
		when(memoryObj.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
		when(memoryObj.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		systemInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertTrue(sensorData instanceof SystemInformationData);
		assertEquals(sensorData.getPlatformIdent(), platformIdent);
		assertEquals(sensorData.getSensorTypeIdent(), sensorTypeIdent);

		SystemInformationData systemData = (SystemInformationData) sensorData;

		// as there was only one data object the values must be the
		// same
		assertEquals(systemData.getArchitecture(), architecture);
		assertEquals(systemData.getAvailableProcessors(), availableProcessors);
		assertEquals(systemData.getBootClassPath(), limit);
		assertEquals(systemData.getClassPath(), limit);
		assertEquals(systemData.getInitHeapMemorySize(), initHeapMemorySize);
		assertEquals(systemData.getInitNonHeapMemorySize(), initNonHeapMemorySize);
		assertEquals(systemData.getJitCompilerName(), jitCompilerName);
		assertEquals(systemData.getLibraryPath(), limit);
		assertEquals(systemData.getMaxHeapMemorySize(), maxHeapMemorySize);
		assertEquals(systemData.getMaxNonHeapMemorySize(), maxNonHeapMemorySize);
		assertEquals(systemData.getOsName(), osName);
		assertEquals(systemData.getOsVersion(), osVersion);
		assertEquals(systemData.getTotalPhysMemory(), totalPhysMemory);
		assertEquals(systemData.getTotalSwapSpace(), totalSwapSpace);
		assertEquals(systemData.getVmName(), vmName);
		assertEquals(systemData.getVmSpecName(), vmSpecName);
		assertEquals(systemData.getVmVendor(), vmVendor);
		assertEquals(systemData.getVmVersion(), vmVersion);
	}

	/**
	 * Creates a new String with the specified length.
	 * 
	 * @param character
	 * @param count
	 * @return
	 */
	private String fillString(char character, int count) {
		// creates a string of 'x' repeating characters
		char[] chars = new char[count];
		while (count > 0) {
			chars[--count] = character;
		}
		return new String(chars);
	}

	protected Level getLogLevel() {
		return Level.FINEST;
	}
}
