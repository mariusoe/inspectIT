package info.novatec.inspectit.cmr.cache.impl;

import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

/**
 * Set of properties for one buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferProperties implements InitializingBean {

	/**
	 * Buffer eviction occupancy percentage.
	 */
	private float evictionOccupancyPercentage;

	/**
	 * Maximum heap occupancy percentage.
	 */
	private float maxHeapSizeOccupancy;

	/**
	 * Minimum heap occupancy percentage.
	 */
	private float minHeapSizeOccupancy;

	/**
	 * Maximum heap occupancy percentage active from this heap size.
	 */
	private long maxHeapSizeOccupancyActiveFromHeapSize;

	/**
	 * Minimum heap occupancy percentage active till this heap size.
	 */
	private long minHeapSizeOccupancyActiveTillHeapSize;

	/**
	 * Minimum memory size that needs always to be available to CMR regardless of buffer.
	 */
	private long minMemoryDelta;

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
	 * Number of elements that need to be processed, so that the maintenance is done.
	 */
	private long elementsCountForMaintenance;

	/**
	 * Size of the eviction fragment in percentages, in relation to the max buffer size.
	 */
	private float evictionFragmentSizePercentage;

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
	 * Returns maximum heap occupancy percentage.
	 * 
	 * @return Maximum heap occupancy percentage as float.
	 */
	public float getMaxHeapSizeOccupancy() {
		return maxHeapSizeOccupancy;
	}

	/**
	 * Sets maximum heap occupancy percentage.
	 * 
	 * @param maxHeapSizeOccupancy
	 *            Maximum heap occupancy percentage as float.
	 */
	public void setMaxHeapSizeOccupancy(float maxHeapSizeOccupancy) {
		this.maxHeapSizeOccupancy = maxHeapSizeOccupancy;
	}

	/**
	 * Returns minimum heap occupancy percentage.
	 * 
	 * @return Minimum heap occupancy percentage as float.
	 */
	public float getMinHeapSizeOccupancy() {
		return minHeapSizeOccupancy;
	}

	/**
	 * Sets minimum heap occupancy percentage.
	 * 
	 * @param minHeapSizeOccupancy
	 *            Minimum heap occupancy percentage as float.
	 */
	public void setMinHeapSizeOccupancy(float minHeapSizeOccupancy) {
		this.minHeapSizeOccupancy = minHeapSizeOccupancy;
	}

	/**
	 * Returns the heap size from which maximum heap occupancy percentage is active.
	 * 
	 * @return Heap size in bytes.
	 */
	public long getMaxHeapSizeOccupancyActiveFromHeapSize() {
		return maxHeapSizeOccupancyActiveFromHeapSize;
	}

	/**
	 * Sets the heap size from which maximum heap occupancy percentage is active.
	 * 
	 * @param maxHeapSizeOccupancyActiveFromHeapSize
	 *            Heap size in bytes.
	 */
	public void setMaxHeapSizeOccupancyActiveFromHeapSize(long maxHeapSizeOccupancyActiveFromHeapSize) {
		this.maxHeapSizeOccupancyActiveFromHeapSize = maxHeapSizeOccupancyActiveFromHeapSize;
	}

	/**
	 * Returns the heap size till which minimum heap occupancy percentage is active.
	 * 
	 * @return Heap size in bytes.
	 */
	public long getMinHeapSizeOccupancyActiveTillHeapSize() {
		return minHeapSizeOccupancyActiveTillHeapSize;
	}

	/**
	 * Sets the heap size till which minimum heap occupancy percentage is active.
	 * 
	 * @param minHeapSizeOccupancyActiveTillHeapSize
	 *            Heap size in bytes.
	 */
	public void setMinHeapSizeOccupancyActiveTillHeapSize(long minHeapSizeOccupancyActiveTillHeapSize) {
		this.minHeapSizeOccupancyActiveTillHeapSize = minHeapSizeOccupancyActiveTillHeapSize;
	}

	/**
	 * Returns minimum memory size that needs always to be available to CMR regardless of buffer.
	 * 
	 * @return Memory size in bytes.
	 */
	public long getMinMemoryDelta() {
		return minMemoryDelta;
	}

	/**
	 * Sets minimum memory size that needs always to be available to CMR regardless of buffer.
	 * 
	 * @param minMemoryDelta
	 *            memory size in bytes.
	 */
	public void setMinMemoryDelta(long minMemoryDelta) {
		this.minMemoryDelta = minMemoryDelta;
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
	 * Returns number of elements that need to be processed, so that the buffer maintenance is done.
	 * 
	 * @return Number of buffer elements.
	 */
	public long getElementsCountForMaintenance() {
		return elementsCountForMaintenance;
	}

	/**
	 * Sets number of elements that need to be processed, so that the buffer maintenance is done.
	 * 
	 * @param elementsCountForMaintenance
	 *            Number of buffer elements.
	 */
	public void setElementsCountForMaintenance(long elementsCountForMaintenance) {
		this.elementsCountForMaintenance = elementsCountForMaintenance;
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
	 * Returns the initial buffer size based on the property set.
	 * 
	 * @return Size in bytes.
	 */
	public long getInitialBufferSize() {
		long maxMemory = Runtime.getRuntime().maxMemory();
		long bufferSize = 0;
		if (maxMemory > maxHeapSizeOccupancyActiveFromHeapSize) {
			bufferSize = (long) (maxMemory * maxHeapSizeOccupancy);
		} else if (maxMemory < minHeapSizeOccupancyActiveTillHeapSize) {
			bufferSize = (long) (maxMemory * minHeapSizeOccupancy);
		} else {
			// delta is the value that defines how much we can extend the minimum heap occupancy
			// percentage by analyzing the max memory size
			// delta is actually representing additional percentage of heap we can take
			// it is always thru that: minHeapSizeOccupancy + delta < maxHeapSizeOccupancy
			float delta = (maxHeapSizeOccupancy - minHeapSizeOccupancy)
					* ((float) (maxMemory - minHeapSizeOccupancyActiveTillHeapSize) / (maxHeapSizeOccupancyActiveFromHeapSize - minHeapSizeOccupancyActiveTillHeapSize));
			bufferSize = (long) (maxMemory * (minHeapSizeOccupancy + delta));
		}

		if (bufferSize + minMemoryDelta < maxMemory) {
			return bufferSize;
		} else {
			bufferSize = maxMemory - minMemoryDelta;
			if (bufferSize < 0) {
				bufferSize = 0;
			}
			return bufferSize;
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
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Buffer properties initialized with following values:");
			LOGGER.info("||-Eviction occupancy percentage: " + NumberFormat.getInstance().format(evictionOccupancyPercentage * 100) + "%");
			LOGGER.info("||-Eviction fragment size percentage: " + NumberFormat.getInstance().format(evictionFragmentSizePercentage * 100) + "%");
			LOGGER.info("||-Max heap size occupancy: " + NumberFormat.getInstance().format(maxHeapSizeOccupancy * 100) + "%");
			LOGGER.info("||-Min heap size occupancy: " + NumberFormat.getInstance().format(minHeapSizeOccupancy * 100) + "%");
			LOGGER.info("||-Max heap size occupancy active from heap size: " + NumberFormat.getInstance().format(maxHeapSizeOccupancyActiveFromHeapSize) + " bytes");
			LOGGER.info("||-Min heap size occupancy active till heap size: " + NumberFormat.getInstance().format(minHeapSizeOccupancyActiveTillHeapSize) + " bytes");
			LOGGER.info("||-Max object size expansion: " + NumberFormat.getInstance().format(maxObjectExpansionRate * 100) + "%");
			LOGGER.info("||-Min object size expansion: " + NumberFormat.getInstance().format(minObjectExpansionRate * 100) + "%");
			LOGGER.info("||-Max object size expansion active till buffer size: " + NumberFormat.getInstance().format(maxObjectExpansionRateActiveTillBufferSize) + " bytes");
			LOGGER.info("||-Min object size expansion active from buffer size: " + NumberFormat.getInstance().format(minObjectExpansionRateActiveFromBufferSize) + " bytes");

		}
		if (this.evictionOccupancyPercentage < 0 || this.evictionOccupancyPercentage > 1) {
			throw new BeanInitializationException("Buffer properties initialization error: Eviction occupancy must be a percentage value between 0 and 1. Initialization value is: " + evictionOccupancyPercentage);
		}
		if (this.evictionFragmentSizePercentage < 0.01 || this.evictionFragmentSizePercentage > 0.5) {
			throw new BeanInitializationException("Buffer properties initialization error: Eviction fragment size must be a percentage value between 0.01 and 0.5. Initialization value is: "
					+ evictionFragmentSizePercentage);
		}
		if (this.minHeapSizeOccupancy < 0 || this.minHeapSizeOccupancy > 1) {
			throw new BeanInitializationException("Buffer properties initialization error: Minimum heap size occupancy must be a percentage value between 0 and 1. Initialization value is: " + minHeapSizeOccupancy);
		}
		if (this.maxHeapSizeOccupancy < 0 || this.maxHeapSizeOccupancy > 1) {
			throw new BeanInitializationException("Buffer properties initialization error: Maximum heap size occupancy must be a percentage value between 0 and 1. Initialization value is: " + maxHeapSizeOccupancy);
		}
		if (this.maxHeapSizeOccupancy < this.minHeapSizeOccupancy) {
			throw new BeanInitializationException("Buffer properties initialization error: Maximum heap size occupancy can not be lower than minimum heap size occupancy. Initialization values are: "
					+ maxHeapSizeOccupancy + " (max) and " + minHeapSizeOccupancy + " (min)");
		}
		if (this.maxObjectExpansionRate < this.minObjectExpansionRate) {
			throw new BeanInitializationException("Buffer properties initialization error: Maximum object expansion rate can not be lower than minimum object expansion rate. Initialization values are: "
					+ maxObjectExpansionRate + " (max) and " + minObjectExpansionRate + " (min)");
		}
		if (this.maxHeapSizeOccupancyActiveFromHeapSize < this.minHeapSizeOccupancyActiveTillHeapSize) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: Heap size from which maximum heap occupancy is active can not be lower than heap size till which minimum heap occupancy is active. Initialization values are: "
							+ maxHeapSizeOccupancyActiveFromHeapSize + " (heap size for max heap occupancy) and " + minHeapSizeOccupancyActiveTillHeapSize + " (heap size for min heap occupancy)");
		}
		if (this.minObjectExpansionRateActiveFromBufferSize < this.maxObjectExpansionRateActiveTillBufferSize) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: Buffer size from which minimum object expansion rate is active can not be lower than buffer size till which maximum object expansion rate is active. Initialization values are: "
							+ minObjectExpansionRateActiveFromBufferSize
							+ " (buffer size for min object expansion rate) and "
							+ maxObjectExpansionRateActiveTillBufferSize
							+ " (buffer size for max object expansion rate)");
		}
	}
}
