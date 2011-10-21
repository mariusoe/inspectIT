package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.service.AgentStorageService;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * This service is used to check the health of the CMR in terms of cpu, memory, some overall
 * statistics etc.
 * 
 * @author Patrice Bouillet
 * 
 */
public class HealthStatus implements InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(HealthStatus.class);

	/**
	 * Are the beans that are responsible for creating the Health Status available.
	 */
	private boolean beansAvailable = false;

	/**
	 * The memory mx bean used to extract information about the memory of the system.
	 */
	private MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

	/**
	 * The operating system mx bean.
	 */
	private OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

	/**
	 * The thread mx bean.
	 */
	private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	/**
	 * The runtime mx bean.
	 */
	private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

	/**
	 * For the visualization of the memory and load average, a graphical visualization is put into
	 * the log for easier analysis. This char is used as the start and the end of the printed lines.
	 */
	private static final char START_END_CHAR = '+';

	/**
	 * The width of the visualization of the memory and load average.
	 */
	private static final int WIDTH = 30;

	/**
	 * Buffer that reports status.
	 */
	private IBuffer<?> buffer;

	/**
	 * {@link AgentStorageService} for reporting the amount of dropped data on the CMR.
	 */
	private AgentStorageService agentStorageService;

	/**
	 * Log all the statistics.
	 */
	public void logStatistics() {
		if (beansAvailable) {
			if (LOGGER.isInfoEnabled()) {
				logOperatingSystemStatistics();
				logRuntimeStatistics();
				logMemoryStatistics();
				logThreadStatistics();
				LOGGER.info("\n");
			}
		}
		if (LOGGER.isInfoEnabled()) {
			logDroppedData();
			logBufferStatistics();
		}
	}

	/**
	 * Log the operating system statistics.
	 */
	private void logOperatingSystemStatistics() {
		String arch = operatingSystemMXBean.getArch();
		String name = operatingSystemMXBean.getName();
		String version = operatingSystemMXBean.getVersion();
		int availCpus = operatingSystemMXBean.getAvailableProcessors();
		double loadAverage = operatingSystemMXBean.getSystemLoadAverage();

		StringBuilder sb = new StringBuilder();
		sb.append("System: ");
		sb.append(name);
		sb.append(" ");
		sb.append(version);
		sb.append(" ");
		sb.append(arch);
		sb.append(" (");
		sb.append(availCpus);
		sb.append(" cpu(s) load average: ");
		sb.append(loadAverage);
		LOGGER.info(sb.toString());

		logGraphicalLoadAverage(loadAverage, availCpus);
	}

	/**
	 * Log a graphical version of the load average.
	 * 
	 * @param loadAvg
	 *            The current load average over the last 60 seconds.
	 * @param availCpus
	 *            The available cpus.
	 * 
	 * @see OperatingSystemMXBean#getSystemLoadAverage()
	 */
	private void logGraphicalLoadAverage(double loadAvg, int availCpus) {
		double loadAverage = loadAvg;
		if (loadAverage < 0) {
			loadAverage = 0;
		}
		double value = (double) WIDTH / availCpus;
		long load = Math.round(loadAverage * value);
		if (load > WIDTH) {
			// Necessary so that we don't brake the limit in graphical representation
			load = WIDTH;
		}
		String title = "CPU load";

		// print first line
		StringBuilder sb = new StringBuilder();
		sb.append(START_END_CHAR);
		sb.append("-");
		sb.append(title);
		for (int i = title.length() + 1; i < WIDTH; i++) {
			sb.append("-");
		}
		sb.append(START_END_CHAR);
		LOGGER.info(sb.toString());

		// now create the middle line with the status.
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < load; i++) {
			sb.append("/");
		}
		// now fill up the remaining space
		for (long i = load; i < WIDTH; i++) {
			sb.append(" ");
		}
		sb.append(START_END_CHAR);
		LOGGER.info(sb.toString());

		// print last line
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < WIDTH; i++) {
			sb.append("-");
		}
		sb.append(START_END_CHAR);
		LOGGER.info(sb.toString());
	}

	/**
	 * Log the runtime statistics.
	 */
	private void logRuntimeStatistics() {
		String name = runtimeMXBean.getName();
		// String specName = runtimeMXBean.getSpecName();
		// String specVendor = runtimeMXBean.getSpecVendor();
		// String specVersion = runtimeMXBean.getSpecVersion();
		long uptime = runtimeMXBean.getUptime();
		String vmName = runtimeMXBean.getVmName();
		String vmVendor = runtimeMXBean.getVmVendor();

		StringBuilder sb = new StringBuilder();
		sb.append("VM: ");
		sb.append(vmName);
		sb.append(" (");
		sb.append(vmVendor);
		sb.append(") process: ");
		sb.append(name);
		sb.append(" uptime: ");
		sb.append(uptime);
		sb.append(" ms");
		LOGGER.info(sb.toString());
	}

	/**
	 * Log the memory statistics.
	 */
	private void logMemoryStatistics() {
		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
		MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

		LOGGER.info("Heap: " + heapMemoryUsage);
		logGraphicalMemoryUsage(heapMemoryUsage, "Heap");
		LOGGER.info("Non Heap: " + nonHeapMemoryUsage);
		logGraphicalMemoryUsage(nonHeapMemoryUsage, "Non-Heap");
		LOGGER.info("Pending finalizations: " + memoryMXBean.getObjectPendingFinalizationCount());
	}

	/**
	 * Log a graphical version of the memory usage object..
	 * 
	 * @param memoryUsage
	 *            The memory usage object to log.
	 * @param title
	 *            Title of graphical box.
	 * 
	 * @see MemoryUsage
	 */
	private void logGraphicalMemoryUsage(MemoryUsage memoryUsage, String title) {
		if (areMemoryUsageValuesCorrect(memoryUsage)) {
			double value = (double) WIDTH / memoryUsage.getMax();
			long used = Math.round(memoryUsage.getUsed() * value);
			long committed = Math.round(memoryUsage.getCommitted() * value);

			// print first line
			StringBuilder sb = new StringBuilder();
			sb.append(START_END_CHAR);
			sb.append("-");
			sb.append(title);
			for (int i = title.length() + 1; i < WIDTH; i++) {
				sb.append("-");
			}

			sb.append(START_END_CHAR);
			LOGGER.info(sb.toString());

			// now create the middle line with the status.
			sb = new StringBuilder();
			sb.append(START_END_CHAR);
			for (int i = 0; i < used; i++) {
				sb.append("/");
			}
			long pos = used;
			if (pos <= committed) {
				// only print the char if committed is greater or equal than the
				// current position.
				for (long i = pos; i < committed; i++) {
					sb.append(" ");
				}
				sb.append("|");
				pos = committed + 1L;
			}
			// now fill up the remaining space
			for (long i = pos; i < WIDTH; i++) {
				sb.append(" ");
			}

			// only print last char if committed is smaller
			if (committed < WIDTH) {
				sb.append(START_END_CHAR);
			}
			LOGGER.info(sb.toString());

			// print last line
			sb = new StringBuilder();
			sb.append(START_END_CHAR);
			for (int i = 0; i < WIDTH; i++) {
				sb.append("-");
			}
			sb.append(START_END_CHAR);
			LOGGER.info(sb.toString());
		}
	}

	/**
	 * Checks if the values in {@link MemoryUsage} are OK for the graphical memory logging.
	 * 
	 * @param memoryUsage
	 *            {@link MemoryUsage}
	 * @return True if values are OK.
	 */
	private boolean areMemoryUsageValuesCorrect(MemoryUsage memoryUsage) {
		if (memoryUsage.getCommitted() < 0 || memoryUsage.getUsed() < 0 || memoryUsage.getMax() < 0) {
			return false;
		}
		if (memoryUsage.getUsed() > memoryUsage.getMax()) {
			return false;
		}
		if (memoryUsage.getUsed() > memoryUsage.getCommitted()) {
			return false;
		}
		if (memoryUsage.getCommitted() > memoryUsage.getMax()) {
			return false;
		}
		return true;
	}

	/**
	 * Log the thread statistics.
	 */
	private void logThreadStatistics() {
		int threadCount = threadMXBean.getThreadCount();
		long totalStartedThreads = threadMXBean.getTotalStartedThreadCount();

		StringBuilder sb = new StringBuilder();
		sb.append("Threads: ");
		sb.append(threadCount);
		sb.append(" total started: ");
		sb.append(totalStartedThreads);
		LOGGER.info(sb.toString());
	}

	/**
	 * Log buffer statistic.
	 */
	private void logBufferStatistics() {
		String[] lines = buffer.toString().split("\n");
		for (String str : lines) {
			LOGGER.info(str);
		}
		logGraphicalBufferOccupancy(buffer.getOccupancyPercentage());
	}

	/**
	 * Log a graphical version of buffer occupancy.
	 * 
	 * @param bufferOccupancy
	 *            Current buffer occupancy in percentages.
	 */
	private void logGraphicalBufferOccupancy(float bufferOccupancy) {
		String title = "Buffer";
		int used = (int) (bufferOccupancy * WIDTH);

		// print first line
		StringBuilder sb = new StringBuilder();
		sb.append(START_END_CHAR);
		sb.append("-");
		sb.append(title);
		for (int i = title.length() + 1; i < WIDTH; i++) {
			sb.append("-");
		}
		sb.append(START_END_CHAR);
		LOGGER.info(sb.toString());

		// now create the middle line with the status.
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < used; i++) {
			sb.append("/");
		}
		for (int j = used; j < WIDTH; j++) {
			sb.append(" ");
		}
		sb.append(START_END_CHAR);
		LOGGER.info(sb.toString());

		// print last line
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < WIDTH; i++) {
			sb.append("-");
		}
		sb.append(START_END_CHAR);
		LOGGER.info(sb.toString());
	}

	/**
	 * Logs the amount of dropped data on CMR.
	 */
	private void logDroppedData() {
		LOGGER.info("Dropped elements due to the high load on the CMR (total count): " + agentStorageService.getDroppedDataCount());
	}

	/**
	 * Checks if the beans are available and sets the {@link #beansAvailable} depending on the
	 * result of check.
	 */
	private void startUpCheck() {
		try {
			operatingSystemMXBean.getArch();
			operatingSystemMXBean.getName();
			operatingSystemMXBean.getVersion();
			operatingSystemMXBean.getAvailableProcessors();
			operatingSystemMXBean.getSystemLoadAverage();

			runtimeMXBean.getName();
			runtimeMXBean.getUptime();
			runtimeMXBean.getVmName();
			runtimeMXBean.getVmVendor();

			memoryMXBean.getHeapMemoryUsage();
			memoryMXBean.getNonHeapMemoryUsage();

			threadMXBean.getThreadCount();
			threadMXBean.getTotalStartedThreadCount();

			beansAvailable = true;
		} catch (Exception e) {
			beansAvailable = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		startUpCheck();
		if (beansAvailable) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Health Service active...");
			}
		} else {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Health Service not active...");
			}
		}
	}

	/**
	 * 
	 * @param buffer
	 *            buffer to be set
	 */
	public void setBuffer(IBuffer<?> buffer) {
		this.buffer = buffer;
	}

	/**
	 * @param agentStorageService
	 *            the agentStorageService to set
	 */
	public void setAgentStorageService(AgentStorageService agentStorageService) {
		this.agentStorageService = agentStorageService;
	}

}
