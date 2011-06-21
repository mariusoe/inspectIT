package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.communication.data.SystemInformationData;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.management.OperatingSystemMXBean;

/**
 * This class provides static information about heap memory/operating system/runtime through
 * MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class SystemInformation implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(SystemInformation.class.getName());

	/**
	 * The maximum length of the fields saved into the database.
	 */
	private static final int MAX_LENGTH = 10000;

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	private final IIdManager idManager;

	/**
	 * After the first update()-call the static information will only be updated when the update is
	 * requested by the user.
	 */
	private boolean updateRequested = true;

	/**
	 * The MXBean used to retrieve information from the operating system.
	 */
	private OperatingSystemMXBean osObj = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	/**
	 * The MXBean used to retrieve heap memory information.
	 */
	private MemoryMXBean memoryObj = ManagementFactory.getMemoryMXBean();

	/**
	 * The MXBean used to retrieve information from the runtime system of the underlying Virtual
	 * Machine.
	 */
	private RuntimeMXBean runtimeObj = ManagementFactory.getRuntimeMXBean();

	/**
	 * The MXBean used to retrieve information from the compilation system.
	 */
	private CompilationMXBean compilationObj = ManagementFactory.getCompilationMXBean();

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public SystemInformation(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Returns the total amount of physical memory.
	 * 
	 * @return The total amount of physical memory.
	 */
	public long getTotalPhysMemory() {
		return osObj.getTotalPhysicalMemorySize();
	}

	/**
	 * Returns the total amount of swap space.
	 * 
	 * @return The total amount of swap space.
	 */
	public long getTotalSwapSpace() {
		return osObj.getTotalSwapSpaceSize();
	}

	/**
	 * Returns the number of processors available to the virtual machine.
	 * 
	 * @return The number of processors available to the virtual machine.
	 */
	public int getAvailableProcessors() {
		return osObj.getAvailableProcessors();
	}

	/**
	 * Returns the operating system architecture.
	 * 
	 * @return The operating system architecture.
	 */
	public String getArchitecture() {
		try {
			return osObj.getArch();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the name of the operating system.
	 * 
	 * @return The name of the operating system.
	 */
	public String getOsName() {
		try {
			return osObj.getName();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the version of the operating system.
	 * 
	 * @return The version of the operating system.
	 */
	public String getOsVersion() {
		try {
			return osObj.getVersion();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Return the name of the Just-in-time (JIT) compiler.
	 * 
	 * @return The name of the Just-in-time (JIT) compiler.
	 */
	public String getJitCompilerName() {
		return compilationObj.getName();
	}

	/**
	 * Returns the java class path that is used by the system class loader to search for class
	 * files.
	 * 
	 * @return The java class path that is used by the system class loader to search for class
	 *         files.
	 */
	public String getClassPath() {
		try {
			return runtimeObj.getClassPath();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the boot class path that is used by the bootstrap class loader to search for class
	 * files.
	 * 
	 * @return The boot class path that is used by the bootstrap class loader to search for class
	 *         files.
	 */
	public String getBootClassPath() {
		try {
			return runtimeObj.getBootClassPath();
		} catch (UnsupportedOperationException ex) {
			return "";
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the java library path.
	 * 
	 * @return The java library path.
	 */
	public String getLibraryPath() {
		try {
			return runtimeObj.getLibraryPath();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the vendor of the virtual machine.
	 * 
	 * @return The vendor of the virtual machine.
	 */
	public String getVmVendor() {
		try {
			return runtimeObj.getVmVendor();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the name of the virtual machine.
	 * 
	 * @return The name of the virtual machine.
	 */
	public String getVmName() {
		return runtimeObj.getName();
	}

	/**
	 * Returns the version of the virtual machine.
	 * 
	 * @return The version of the virtual machine.
	 */
	public String getVmVersion() {
		try {
			return runtimeObj.getVmVersion();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the name representing the running virtual machine. for example: 12456@pc-name.
	 * 
	 * @return The name representing the running virtual machine.
	 */
	public String getVmSpecName() {
		try {
			return runtimeObj.getSpecName();
		} catch (SecurityException ex) {
			return "";
		}
	}

	/**
	 * Returns the initial amount of memory that the virtual machine requests from the operating
	 * system for heap memory management during startup.
	 * 
	 * @return The initial amount of memory that the virtual machine requests from the operating
	 *         system for heap memory management during startup.
	 */
	public long getInitHeapMemorySize() {
		return memoryObj.getHeapMemoryUsage().getInit();
	}

	/**
	 * Returns the maximum amount of memory that can be used for heap memory management.
	 * 
	 * @return The maximum amount of memory that can be used for heap memory management.
	 */
	public long getMaxHeapMemorySize() {
		return memoryObj.getHeapMemoryUsage().getMax();
	}

	/**
	 * Returns the initial amount of memory that the virtual machine requests from the operating
	 * system for non-heap memory management during startup.
	 * 
	 * @return The initial amount of memory that the virtual machine requests from the operating
	 *         system for non-heap memory management during startup.
	 */
	public long getInitNonHeapMemorySize() {
		return memoryObj.getNonHeapMemoryUsage().getInit();
	}

	/**
	 * Returns the maximum amount of memory that can be used for non-heap memory management.
	 * 
	 * @return The maximum amount of memory that can be used for non-heap memory management.
	 */
	public long getMaxNonHeapMemorySize() {
		return memoryObj.getNonHeapMemoryUsage().getMax();
	}

	/**
	 * Updates all static information.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		try {
			long platformId = idManager.getPlatformId();
			long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
			Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

			SystemInformationData systemData = new SystemInformationData(timestamp, platformId, registeredSensorTypeId);

			updateRequested = false;
			String vmArgumentName = "";
			String vmArgumentValue = "";
			long totalPhysMemory = this.getTotalPhysMemory();
			long totalSwapSpace = this.getTotalSwapSpace();
			int availableProcessors = this.getAvailableProcessors();
			String architecture = this.getArchitecture();
			String osName = this.getOsName();
			String osVersion = this.getOsVersion();
			String jitCompilerName = this.getJitCompilerName();
			String classPath = crop(this.getClassPath());
			String bootClassPath = crop(this.getBootClassPath());
			String libraryPath = crop(this.getLibraryPath());
			String vmVendor = this.getVmVendor();
			String vmVersion = this.getVmVersion();
			String vmName = this.getVmName();
			String vmSpecName = this.getVmSpecName();
			long initHeapMemorySize = this.getInitHeapMemorySize();
			long maxHeapMemorySize = this.getMaxHeapMemorySize();
			long initNonHeapMemorySize = this.getInitNonHeapMemorySize();
			long maxNonHeapMemorySize = this.getMaxNonHeapMemorySize();

			systemData.setTotalPhysMemory(totalPhysMemory);
			systemData.setTotalSwapSpace(totalSwapSpace);
			systemData.setAvailableProcessors(availableProcessors);
			systemData.setArchitecture(architecture);
			systemData.setOsName(osName);
			systemData.setOsVersion(osVersion);
			systemData.setJitCompilerName(jitCompilerName);
			systemData.setClassPath(classPath);
			systemData.setBootClassPath(bootClassPath);
			systemData.setLibraryPath(libraryPath);
			systemData.setVmVendor(vmVendor);
			systemData.setVmVersion(vmVersion);
			systemData.setVmName(vmName);
			systemData.setVmSpecName(vmSpecName);
			systemData.setInitHeapMemorySize(initHeapMemorySize);
			systemData.setMaxHeapMemorySize(maxHeapMemorySize);
			systemData.setInitNonHeapMemorySize(initNonHeapMemorySize);
			systemData.setMaxNonHeapMemorySize(maxNonHeapMemorySize);

			Properties properties = System.getProperties();
			for (Map.Entry<Object, Object> property : properties.entrySet()) {
				vmArgumentName = (String) property.getKey();
				vmArgumentValue = (String) property.getValue();
				systemData.addVMArguments(vmArgumentName, vmArgumentValue);
			}

			coreService.addPlatformSensorData(sensorTypeIdent, systemData);
		} catch (IdNotAvailableException e) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("Could not save the system information because of an unavailable id. " + e.getMessage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean automaticUpdate() {
		return updateRequested;
	}

	/**
	 * Crops a string if it is longer than the specified MAX_LENGTH.
	 * 
	 * @param value
	 *            The value to crop.
	 * @return A cropped string which length is smaller than the MAX_LENGTH.
	 */
	private String crop(String value) {
		if (null != value && value.length() > MAX_LENGTH) {
			return value.substring(0, MAX_LENGTH);
		}
		return value;
	}
}
