package info.novatec.inspectit.cmr.cache.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.text.NumberFormat;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * Set of properties for one buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferProperties {

	/**
	 * Name of the memory pool for old generation.
	 */
	private static final String OLD_GEN_POOL_NAME = "Old Gen";

	/**
	 * Name of the memory pool for tenured generation. Some JVM name like this the old generation
	 * space.
	 */
	private static final String TENURED_GEN_POOL_NAME = "Tenured";

	/**
	 * Buffer eviction occupancy percentage.
	 */
	private float evictionOccupancyPercentage;

	/**
	 * Maximum security object expansion rate in percentages.
	 */
	private float maxObjectExpansionRate;

	/**
	 * Minimum security object expansion rate in percentages.
	 */
	private float minObjectExpansionRate;

	/**
	 * Maximum security object expansion rate active till this buffer size.
	 */
	private long maxObjectExpansionRateActiveTillBufferSize;

	/**
	 * Minimum security object expansion rate active from this buffer size.
	 */
	private long minObjectExpansionRateActiveFromBufferSize;

	/**
	 * Size of the eviction fragment in percentages, in relation to the max buffer size.
	 */
	private float evictionFragmentSizePercentage;

	/**
	 * Number of bytes in % relative to the buffer size that need to be added or removed for the
	 * buffer so that update and clean of the indexing tree is performed - 5%.
	 */
	private float bytesMaintenancePercentage;

	/**
	 * Number of threads that are cleaning the indexing tree.
	 */
	private int indexingTreeCleaningThreads;

	/**
	 * Time in milliseconds that the indexing thread will wait for the object to be analyzed first.
	 */
	private long indexingWaitTime;

	/**
	 * Size of old space occupancy till which min occupancy will be active.
	 */
	private long minOldSpaceOccupancyActiveTillOldGenSize;

	/**
	 * Size of old space occupancy from which max occupancy will be active.
	 */
	private long maxOldSpaceOccupancyActiveFromOldGenSize;

	/**
	 * Percentage of the min old generation heap space buffer can occupy.
	 */
	private float minOldSpaceOccupancy;

	/**
	 * Percentage of the max old generation heap space buffer can occupy.
	 */
	private float maxOldSpaceOccupancy;

	/**
	 * Logger for buffer properties.
	 */
	private static final Logger LOGGER = Logger.getLogger(BufferProperties.class);

	/**
	 * Returns buffer eviction occupancy percentage.
	 * 
	 * @return Buffer eviction occupancy percentage as float.
	 */
	public float getEvictionOccupancyPercentage() {
		return evictionOccupancyPercentage;
	}

	/**
	 * Sets buffer eviction occupancy percentage.
	 * 
	 * @param evictionOccupancyPercentage
	 *            Buffer eviction occupancy percentage as float.
	 */
	public void setEvictionOccupancyPercentage(float evictionOccupancyPercentage) {
		this.evictionOccupancyPercentage = evictionOccupancyPercentage;
	}

	/**
	 * Returns maximum security object expansion rate in percentages.
	 * 
	 * @return Maximum security object expansion rate in percentages as float.
	 */
	public float getMaxObjectExpansionRate() {
		return maxObjectExpansionRate;
	}

	/**
	 * Sets maximum security object expansion rate in percentages.
	 * 
	 * @param maxObjectExpansionRate
	 *            Maximum security object expansion rate in percentages as float.
	 */
	public void setMaxObjectExpansionRate(float maxObjectExpansionRate) {
		this.maxObjectExpansionRate = maxObjectExpansionRate;
	}

	/**
	 * Returns minimum security object expansion rate in percentages.
	 * 
	 * @return Minimum security object expansion rate in percentages as float.
	 */
	public float getMinObjectExpansionRate() {
		return minObjectExpansionRate;
	}

	/**
	 * Sets minimum security object expansion rate in percentages.
	 * 
	 * @param minObjectExpansionRate
	 *            Minimum security object expansion rate in percentages as float.
	 */
	public void setMinObjectExpansionRate(float minObjectExpansionRate) {
		this.minObjectExpansionRate = minObjectExpansionRate;
	}

	/**
	 * Returns buffer size till which maximum object expansion rate is active.
	 * 
	 * @return Buffer size in bytes.
	 */
	public long getMaxObjectExpansionRateActiveTillBufferSize() {
		return maxObjectExpansionRateActiveTillBufferSize;
	}

	/**
	 * Sets buffer size till which maximum object expansion rate is active.
	 * 
	 * @param maxObjectExpansionRateActiveTillBufferSize
	 *            Buffer size in bytes.
	 */
	public void setMaxObjectExpansionRateActiveTillBufferSize(long maxObjectExpansionRateActiveTillBufferSize) {
		this.maxObjectExpansionRateActiveTillBufferSize = maxObjectExpansionRateActiveTillBufferSize;
	}

	/**
	 * Returns buffer size from which minimum object expansion rate is active.
	 * 
	 * @return Buffer size in bytes.
	 */
	public long getMinObjectExpansionRateActiveFromBufferSize() {
		return minObjectExpansionRateActiveFromBufferSize;
	}

	/**
	 * Sets buffer size from which minimum object expansion rate is active.
	 * 
	 * @param minObjectExpansionRateActiveFromBufferSize
	 *            Buffer size in bytes.
	 */
	public void setMinObjectExpansionRateActiveFromBufferSize(long minObjectExpansionRateActiveFromBufferSize) {
		this.minObjectExpansionRateActiveFromBufferSize = minObjectExpansionRateActiveFromBufferSize;
	}

	/**
	 * Returns size of the eviction fragment in percentages, in relation to the max buffer size.
	 * 
	 * @return Eviction fragment in percentages as float.
	 */
	public float getEvictionFragmentSizePercentage() {
		return evictionFragmentSizePercentage;
	}

	/**
	 * Sets size of the eviction fragment in percentages, in relation to the max buffer size.
	 * 
	 * @param evictionFragmentSizePercentage
	 *            Eviction fragment in percentages as float.
	 */
	public void setEvictionFragmentSizePercentage(float evictionFragmentSizePercentage) {
		this.evictionFragmentSizePercentage = evictionFragmentSizePercentage;
	}

	/**
	 * Number of bytes that need to be added or removed for the buffer so that update and clean of
	 * the indexing tree is performed.
	 * 
	 * @param bufferSize
	 *            Size of the buffer.
	 * @return Number of bytes that need to be added or removed for the buffer so that update and
	 *         clean of the indexing tree is performed.
	 */
	public long getFlagsSetOnBytes(long bufferSize) {
		return (long) (bytesMaintenancePercentage * bufferSize);
	}

	/**
	 * @return the bytesMaintenancePercentage
	 */
	public float getBytesMaintenancePercentage() {
		return bytesMaintenancePercentage;
	}

	/**
	 * @param bytesMaintenancePercentage
	 *            the bytesMaintenancePercentage to set
	 */
	public void setBytesMaintenancePercentage(float bytesMaintenancePercentage) {
		this.bytesMaintenancePercentage = bytesMaintenancePercentage;
	}

	/**
	 * @return Number of indexing tree cleaning threads.
	 */
	public int getIndexingTreeCleaningThreads() {
		return indexingTreeCleaningThreads;
	}

	/**
	 * @param indexingTreeCleaningThreads
	 *            Number of indexing tree cleaning threads.
	 */
	public void setIndexingTreeCleaningThreads(int indexingTreeCleaningThreads) {
		this.indexingTreeCleaningThreads = indexingTreeCleaningThreads;
	}

	/**
	 * @return the indexingWaitTime
	 */
	public long getIndexingWaitTime() {
		return indexingWaitTime;
	}

	/**
	 * @param indexingWaitTime
	 *            the indexingWaitTime to set
	 */
	public void setIndexingWaitTime(long indexingWaitTime) {
		this.indexingWaitTime = indexingWaitTime;
	}

	/**
	 * @return the minOldSpaceOccupancyActiveTillOldGenSize
	 */
	public long getMinOldSpaceOccupancyActiveTillOldGenSize() {
		return minOldSpaceOccupancyActiveTillOldGenSize;
	}

	/**
	 * @param minOldSpaceOccupancyActiveTillOldGenSize
	 *            the minOldSpaceOccupancyActiveTillOldGenSize to set
	 */
	public void setMinOldSpaceOccupancyActiveTillOldGenSize(long minOldSpaceOccupancyActiveTillOldGenSize) {
		this.minOldSpaceOccupancyActiveTillOldGenSize = minOldSpaceOccupancyActiveTillOldGenSize;
	}

	/**
	 * @return the maxOldSpaceOccupancyActiveFromOldGenSize
	 */
	public long getMaxOldSpaceOccupancyActiveFromOldGenSize() {
		return maxOldSpaceOccupancyActiveFromOldGenSize;
	}

	/**
	 * @param maxOldSpaceOccupancyActiveFromOldGenSize
	 *            the maxOldSpaceOccupancyActiveFromOldGenSize to set
	 */
	public void setMaxOldSpaceOccupancyActiveFromOldGenSize(long maxOldSpaceOccupancyActiveFromOldGenSize) {
		this.maxOldSpaceOccupancyActiveFromOldGenSize = maxOldSpaceOccupancyActiveFromOldGenSize;
	}

	/**
	 * @return the minOldSpaceOccupancy
	 */
	public float getMinOldSpaceOccupancy() {
		return minOldSpaceOccupancy;
	}

	/**
	 * @param minOldSpaceOccupancy
	 *            the minOldSpaceOccupancy to set
	 */
	public void setMinOldSpaceOccupancy(float minOldSpaceOccupancy) {
		this.minOldSpaceOccupancy = minOldSpaceOccupancy;
	}

	/**
	 * @return the oldSpaceOccupancy
	 */
	public float getMaxOldSpaceOccupancy() {
		return maxOldSpaceOccupancy;
	}

	/**
	 * @param oldSpaceOccupancy
	 *            the oldSpaceOccupancy to set
	 */
	public void setMaxOldSpaceOccupancy(float oldSpaceOccupancy) {
		this.maxOldSpaceOccupancy = oldSpaceOccupancy;
	}

	/**
	 * Returns the initial buffer size based on the property set.
	 * 
	 * @return Size in bytes.
	 */
	public long getInitialBufferSize() {
		long bufferSize = 0;
		long oldGenMax = 0;

		// try with Memory pool beans
		try {
			List<MemoryPoolMXBean> memBeans = ManagementFactory.getMemoryPoolMXBeans();
			for (MemoryPoolMXBean memBean : memBeans) {
				if (memBean.getName().indexOf(OLD_GEN_POOL_NAME) != -1 || memBean.getName().indexOf(TENURED_GEN_POOL_NAME) != -1) {
					MemoryUsage memUsage = memBean.getUsage();
					oldGenMax = memUsage.getMax();
					break;
				}
			}
		} catch (Exception e) {
			oldGenMax = 0;
		}

		// fall back to the Runtime bean for arguments
		try {
			if (oldGenMax == 0) {
				long maxHeap = 0;
				long newGen = 0;
				RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
				List<String> arguments = runtimeMXBean.getInputArguments();
				for (String arg : arguments) {
					if (arg.length() > 4) {
						String subedArg = arg.substring(0, 4);
						if ("-Xmx".equalsIgnoreCase(subedArg)) {
							maxHeap = getMemorySizeFromArgument(arg, subedArg);
						}
						if ("-Xmn".equalsIgnoreCase(subedArg)) {
							newGen = getMemorySizeFromArgument(arg, subedArg);
						}
					}
				}
				if (maxHeap != 0 && newGen != 0 && maxHeap > newGen) {
					oldGenMax = maxHeap - newGen;
				}
			}
		} catch (Exception e) {
			oldGenMax = 0;
		}

		// If we did not get the value, throw exception
		if (oldGenMax == 0) {
			throw new RuntimeException("Could not calculate the old generation heap space. Please make sure CMR is running on the provided JVM.");
		}

		// Otherwise calculate now
		if (oldGenMax > maxOldSpaceOccupancyActiveFromOldGenSize) {
			bufferSize = (long) (oldGenMax * maxOldSpaceOccupancy);
		} else if (oldGenMax < minOldSpaceOccupancyActiveTillOldGenSize) {
			bufferSize = (long) (oldGenMax * minOldSpaceOccupancy);
		} else {
			// delta is the value that defines how much we can extend the minimum heap
			// occupancy
			// percentage by analyzing the max memory size
			// delta is actually representing additional percentage of heap we can take
			// it is always thru that: minHeapSizeOccupancy + delta <
			// maxHeapSizeOccupancy
			float delta = (maxOldSpaceOccupancy - minOldSpaceOccupancy)
					* ((float) (oldGenMax - minOldSpaceOccupancyActiveTillOldGenSize) / (maxOldSpaceOccupancyActiveFromOldGenSize - minOldSpaceOccupancyActiveTillOldGenSize));
			bufferSize = (long) (oldGenMax * (minOldSpaceOccupancy + delta));
		}
		return bufferSize;
	}

	/**
	 * Returns memory in bytes for the given argument.
	 * 
	 * @param argument
	 *            Complete argument value.
	 * @param memoryToken
	 *            Memory token that is contained in argument. For example 'Xmx' or similar.
	 * @return Memory value in bytes.
	 */
	private long getMemorySizeFromArgument(String argument, String memoryToken) {
		try {
			int index = argument.indexOf(memoryToken) + memoryToken.length();

			String number = argument.substring(index, argument.length() - 1);
			String typeOfMemory = argument.substring(index + number.length());

			double value = Double.parseDouble(number);
			if (typeOfMemory.equalsIgnoreCase("K")) {
				value *= 1024;
			} else if (typeOfMemory.equalsIgnoreCase("M")) {
				value *= 1024 * 1024;
			} else if (typeOfMemory.equalsIgnoreCase("G")) {
				value *= 1024 * 1024 * 1024;
			} else {
				value *= 1;
			}

			return (long) value;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Returns object security expansion rate based on the property set and given buffer size.
	 * 
	 * @param bufferSize
	 *            Buffer's size that expansion rate has to be calculated for.
	 * @return Expansion rate in percentages.
	 */
	public float getObjectSecurityExpansionRate(long bufferSize) {
		if (bufferSize > minObjectExpansionRateActiveFromBufferSize) {
			return minObjectExpansionRate;
		} else if (bufferSize < maxObjectExpansionRateActiveTillBufferSize) {
			return maxObjectExpansionRate;
		} else {
			// delta is the value that defines how much we can lower the maximum object security
			// rate by analyzing the given buffer size
			// it is always true that: maxObjectExpansionRate - delta > minObjectExpansionRate
			float delta = (maxObjectExpansionRate - minObjectExpansionRate)
					* ((float) (bufferSize - maxObjectExpansionRateActiveTillBufferSize) / (minObjectExpansionRateActiveFromBufferSize - maxObjectExpansionRateActiveTillBufferSize));
			return maxObjectExpansionRate - delta;
		}
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Buffer properties initialized with following values:");
			LOGGER.info("||-Eviction occupancy percentage: " + NumberFormat.getInstance().format(evictionOccupancyPercentage * 100) + "%");
			LOGGER.info("||-Eviction fragment size percentage: " + NumberFormat.getInstance().format(evictionFragmentSizePercentage * 100) + "%");
			LOGGER.info("||-Max object size expansion: " + NumberFormat.getInstance().format(maxObjectExpansionRate * 100) + "%");
			LOGGER.info("||-Min object size expansion: " + NumberFormat.getInstance().format(minObjectExpansionRate * 100) + "%");
			LOGGER.info("||-Max object size expansion active till buffer size: " + NumberFormat.getInstance().format(maxObjectExpansionRateActiveTillBufferSize) + " bytes");
			LOGGER.info("||-Min object size expansion active from buffer size: " + NumberFormat.getInstance().format(minObjectExpansionRateActiveFromBufferSize) + " bytes");
			LOGGER.info("||-Indexing tree cleaning threads: " + NumberFormat.getInstance().format(indexingTreeCleaningThreads));
			LOGGER.info("||-Indexing waiting time: " + NumberFormat.getInstance().format(indexingWaitTime) + " ms");
			LOGGER.info("||-Min old generation occupancy percentage active till: " + NumberFormat.getInstance().format(minOldSpaceOccupancyActiveTillOldGenSize) + " bytes");
			LOGGER.info("||-Max old generation occupancy percentage active from: " + NumberFormat.getInstance().format(maxOldSpaceOccupancyActiveFromOldGenSize) + " bytes");
			LOGGER.info("||-Min old generation occupancy percentage: " + NumberFormat.getInstance().format(minOldSpaceOccupancy * 100) + "%");
			LOGGER.info("||-Max old generation occupancy percentage: " + NumberFormat.getInstance().format(maxOldSpaceOccupancy * 100) + "%");
		}
		if (this.evictionOccupancyPercentage < 0 || this.evictionOccupancyPercentage > 1) {
			throw new BeanInitializationException("Buffer properties initialization error: Eviction occupancy must be a percentage value between 0 and 1. Initialization value is: "
					+ evictionOccupancyPercentage);
		}
		if (this.evictionFragmentSizePercentage < 0.01 || this.evictionFragmentSizePercentage > 0.5) {
			throw new BeanInitializationException("Buffer properties initialization error: Eviction fragment size must be a percentage value between 0.01 and 0.5. Initialization value is: "
					+ evictionFragmentSizePercentage);
		}
		if (this.minObjectExpansionRateActiveFromBufferSize < this.maxObjectExpansionRateActiveTillBufferSize) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: Buffer size from which minimum object expansion rate is active can not be lower than buffer size till which maximum object expansion rate is active. Initialization values are: "
							+ minObjectExpansionRateActiveFromBufferSize
							+ " (buffer size for min object expansion rate) and "
							+ maxObjectExpansionRateActiveTillBufferSize
							+ " (buffer size for max object expansion rate)");
		}
		if (this.getBytesMaintenancePercentage() <= 0 && this.getBytesMaintenancePercentage() > this.getEvictionOccupancyPercentage()) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The buffer bytes maintenance percentage that activate the clean and update of the indexing tree can not be less or equal than zero nor bigger that eviction occupancy percentage. Initialization value is: "
							+ this.getBytesMaintenancePercentage());
		}
		if (this.getIndexingTreeCleaningThreads() <= 0) {
			throw new BeanInitializationException("Buffer properties initialization error: The number of indexing tree cleaning threads can not be less or equal than zero. Initialization value is: "
					+ this.getIndexingTreeCleaningThreads());
		}
		if (this.indexingWaitTime <= 0) {
			throw new BeanInitializationException("Buffer properties initialization error: The indexing wait time can not be less or equal than zero. Initialization value is: "
					+ this.indexingWaitTime);
		}
		if (this.minOldSpaceOccupancyActiveTillOldGenSize <= 0) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The min buffer occupancy percentage of the old generation heap space active till old generation size value can not be less or equal than zero. Initialization value is: "
							+ this.minOldSpaceOccupancyActiveTillOldGenSize);
		}
		if (this.maxOldSpaceOccupancyActiveFromOldGenSize <= 0) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The max buffer occupancy percentage of the old generation heap space active till old generation size value can not be less or equal than zero. Initialization value is: "
							+ this.maxOldSpaceOccupancyActiveFromOldGenSize);
		}
		if (this.minOldSpaceOccupancy > this.maxOldSpaceOccupancy) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The min buffer occupancy percentage of the old generation heap space can not be higer than max buffer occupancy percentage of the old generation. Initialization values are: "
							+ this.minOldSpaceOccupancy + "(min), " + this.maxOldSpaceOccupancy + "(max)");
		}
		if (this.minOldSpaceOccupancy <= 0 || this.minOldSpaceOccupancy > 1) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The min buffer occupancy percentage of the old generation heap space can not be less or equal than zero, nor greater that one. Initialization value is: "
							+ this.minOldSpaceOccupancy);
		}
		if (this.maxOldSpaceOccupancy <= 0 || this.maxOldSpaceOccupancy > 1) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The max buffer occupancy percentage of the old generation heap space can not be less or equal than zero, nor greater that one. Initialization value is: "
							+ this.maxOldSpaceOccupancy);
		}
	}

}
