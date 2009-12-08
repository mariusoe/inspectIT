package info.novatec.novaspy.agent.sensor.platform;

import info.novatec.novaspy.agent.core.ICoreService;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IdNotAvailableException;
import info.novatec.novaspy.agent.sensor.platform.IPlatformSensor;
import info.novatec.novaspy.communication.data.MemoryInformationData;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.management.OperatingSystemMXBean;

/**
 * This class provide dynamic informations about memory through MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class MemoryInformation implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	private static Logger logger = Logger.getLogger(MemoryInformation.class.getName());

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	private final IIdManager idManager;

	/**
	 * The MXBean for heap memory informations.
	 */
	private MemoryMXBean memoryObj = ManagementFactory.getMemoryMXBean();

	/**
	 * The MXBean for os memory informations.
	 */
	private OperatingSystemMXBean osObj = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public MemoryInformation(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Returns the amount of free physical memory.
	 * 
	 * @return the free physical memory.
	 */
	public long getFreePhysMemory() {
		return osObj.getFreePhysicalMemorySize();
	}

	/**
	 * Returns the amount of free swap space.
	 * 
	 * @return the free swap space.
	 */
	public long getFreeSwapSpace() {
		return osObj.getFreeSwapSpaceSize();
	}

	/**
	 * Returns the amount of virtual memory that is guaranteed to be available
	 * to the running process.
	 * 
	 * @return the virtual memory size.
	 */
	public long getComittedVirtualMemSize() {
		return osObj.getCommittedVirtualMemorySize();
	}

	/**
	 * Returns the memory usage of the heap that is used for object allocation.
	 * 
	 * @return the memory usage of the heap for object allocation.
	 */
	public long getUsedHeapMemorySize() {
		return memoryObj.getHeapMemoryUsage().getUsed();
	}

	/**
	 * Returns the amount of memory that is guaranteed to be available for use
	 * by the virtual machine for heap memory usage.
	 * 
	 * @return the amount of guaranteed to be available memory for heap memory
	 *         usage.
	 */
	public long getComittedHeapMemorySize() {
		return memoryObj.getHeapMemoryUsage().getCommitted();
	}

	/**
	 * Returns the amount of memory for non-heap memory usage of the virtual
	 * machine.
	 * 
	 * @return the amount of memory for non-heap memory usage.
	 */
	public long getUsedNonHeapMemorySize() {
		return memoryObj.getNonHeapMemoryUsage().getUsed();
	}

	/**
	 * Returns the amount of memory that is guaranteed to be available for use
	 * by the virtual machine for non-heap memory usage.
	 * 
	 * @return the guaranteed to be available memory for non-heap memory usage.
	 */
	public long getComittedNonHeapMemoryUsage() {
		return memoryObj.getNonHeapMemoryUsage().getCommitted();
	}

	/**
	 * Updates all dynamic memory informations.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		long freePhysMemory = this.getFreePhysMemory();
		long freeSwapSpace = this.getFreeSwapSpace();
		long comittedVirtualMemSize = this.getComittedVirtualMemSize();
		long usedHeapMemorySize = this.getUsedHeapMemorySize();
		long comittedHeapMemorySize = this.getComittedHeapMemorySize();
		long usedNonHeapMemorySize = this.getUsedNonHeapMemorySize();
		long comittedNonHeapMemorySize = this.getComittedNonHeapMemoryUsage();

		MemoryInformationData memoryData = (MemoryInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (memoryData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				memoryData = new MemoryInformationData(timestamp, platformId, registeredSensorTypeId);
				memoryData.incrementCount();

				memoryData.addFreePhysMemory(freePhysMemory);
				memoryData.setMinFreePhysMemory(freePhysMemory);
				memoryData.setMaxFreePhysMemory(freePhysMemory);

				memoryData.addFreeSwapSpace(freeSwapSpace);
				memoryData.setMinFreeSwapSpace(freeSwapSpace);
				memoryData.setMaxFreeSwapSpace(freeSwapSpace);

				memoryData.addComittedVirtualMemSize(comittedVirtualMemSize);
				memoryData.setMinComittedVirtualMemSize(comittedVirtualMemSize);
				memoryData.setMaxComittedVirtualMemSize(comittedVirtualMemSize);

				memoryData.addUsedHeapMemorySize(usedHeapMemorySize);
				memoryData.setMinUsedHeapMemorySize(usedHeapMemorySize);
				memoryData.setMaxUsedHeapMemorySize(usedHeapMemorySize);

				memoryData.addComittedHeapMemorySize(comittedHeapMemorySize);
				memoryData.setMinComittedHeapMemorySize(comittedHeapMemorySize);
				memoryData.setMaxComittedHeapMemorySize(comittedHeapMemorySize);

				memoryData.addUsedNonHeapMemorySize(usedNonHeapMemorySize);
				memoryData.setMinUsedNonHeapMemorySize(usedNonHeapMemorySize);
				memoryData.setMaxUsedNonHeapMemorySize(usedNonHeapMemorySize);

				memoryData.addComittedNonHeapMemorySize(comittedNonHeapMemorySize);
				memoryData.setMinComittedNonHeapMemorySize(comittedNonHeapMemorySize);
				memoryData.setMaxComittedNonHeapMemorySize(comittedNonHeapMemorySize);

				coreService.addPlatformSensorData(sensorTypeIdent, memoryData);
			} catch (IdNotAvailableException e) {
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Could not save the memory information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			memoryData.incrementCount();
			memoryData.addFreePhysMemory(freePhysMemory);
			memoryData.addFreeSwapSpace(freeSwapSpace);
			memoryData.addComittedVirtualMemSize(comittedVirtualMemSize);
			memoryData.addUsedHeapMemorySize(usedHeapMemorySize);
			memoryData.addComittedHeapMemorySize(comittedHeapMemorySize);
			memoryData.addUsedNonHeapMemorySize(usedNonHeapMemorySize);
			memoryData.addComittedNonHeapMemorySize(comittedNonHeapMemorySize);

			if (freePhysMemory < memoryData.getMinFreePhysMemory()) {
				memoryData.setMinFreePhysMemory(freePhysMemory);
			} else if (freePhysMemory > memoryData.getMaxFreePhysMemory()) {
				memoryData.setMaxFreePhysMemory(freePhysMemory);
			}

			if (freeSwapSpace < memoryData.getMinFreeSwapSpace()) {
				memoryData.setMinFreeSwapSpace(freeSwapSpace);
			} else if (freeSwapSpace > memoryData.getMaxFreeSwapSpace()) {
				memoryData.setMaxFreeSwapSpace(freeSwapSpace);
			}

			if (comittedVirtualMemSize < memoryData.getMinComittedVirtualMemSize()) {
				memoryData.setMinComittedVirtualMemSize(comittedVirtualMemSize);
			} else if (comittedVirtualMemSize > memoryData.getMaxComittedVirtualMemSize()) {
				memoryData.setMaxComittedVirtualMemSize(comittedVirtualMemSize);
			}

			if (usedHeapMemorySize < memoryData.getMinUsedHeapMemorySize()) {
				memoryData.setMinUsedHeapMemorySize(usedHeapMemorySize);
			} else if (usedHeapMemorySize > memoryData.getMaxUsedHeapMemorySize()) {
				memoryData.setMaxUsedHeapMemorySize(usedHeapMemorySize);
			}

			if (comittedHeapMemorySize < memoryData.getMinComittedHeapMemorySize()) {
				memoryData.setMinComittedHeapMemorySize(comittedHeapMemorySize);
			} else if (comittedHeapMemorySize > memoryData.getMaxComittedHeapMemorySize()) {
				memoryData.setMaxComittedHeapMemorySize(comittedHeapMemorySize);
			}

			if (usedNonHeapMemorySize < memoryData.getMinUsedNonHeapMemorySize()) {
				memoryData.setMinUsedNonHeapMemorySize(usedNonHeapMemorySize);
			} else if (usedNonHeapMemorySize > memoryData.getMaxUsedNonHeapMemorySize()) {
				memoryData.setMaxUsedNonHeapMemorySize(usedNonHeapMemorySize);
			}

			if (comittedNonHeapMemorySize < memoryData.getMinComittedNonHeapMemorySize()) {
				memoryData.setMinComittedNonHeapMemorySize(comittedNonHeapMemorySize);
			} else if (comittedNonHeapMemorySize > memoryData.getMaxComittedNonHeapMemorySize()) {
				memoryData.setMaxComittedNonHeapMemorySize(comittedNonHeapMemorySize);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void init(Map parameter) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean automaticUpdate() {
		return true;
	}

}
