package info.novatec.inspectit.cmr.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import org.apache.log4j.Logger;

/**
 * This service is used to check the health of the CMR in terms of cpu, memory,
 * some overall statistics etc.
 * 
 * @author Patrice Bouillet
 * 
 */
public class HealthStatus {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(HealthStatus.class);

	/**
	 * The memory mx bean used to extract information about the memory of the
	 * system.
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
	 * For the visualization of the memory and load average, a graphical
	 * visualization is put into the log for easier analysis. This char is used
	 * as the start and the end of the printed lines.
	 */
	private static final char START_END_CHAR = '+';

	/**
	 * The width of the visualization of the memory and load average.
	 */
	private static final int WIDTH = 30;

	/**
	 * Log all the statistics.
	 */
	public void logStatistics() {
		if (LOGGER.isInfoEnabled()) {
			logOperatingSystemStatistics();
			logRuntimeStatistics();
			logMemoryStatistics();
			logThreadStatistics();
			LOGGER.info("\n");
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
	 * @param loadAverage
	 *            The current load average over the last 60 seconds.
	 * @param availCpus
	 *            The available cpus.
	 * 
	 * @see OperatingSystemMXBean#getSystemLoadAverage()
	 */
	private void logGraphicalLoadAverage(double loadAverage, int availCpus) {
		if (loadAverage < 0) {
			loadAverage = 0;
		}
		double value = (WIDTH + 2.0d) / availCpus;
		long load = Math.round(loadAverage * value);

		// print first line
		StringBuilder sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < WIDTH; i++) {
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
		logGraphicalMemoryUsage(heapMemoryUsage);
		LOGGER.info("Non Heap: " + nonHeapMemoryUsage);
		logGraphicalMemoryUsage(nonHeapMemoryUsage);
		LOGGER.info("Pending finalizations: " + memoryMXBean.getObjectPendingFinalizationCount());
	}

	/**
	 * Log a graphical version of the memory usage object..
	 * 
	 * @param memoryUsage
	 *            The memory usage object to log.
	 * 
	 * @see MemoryUsage
	 */
	private void logGraphicalMemoryUsage(MemoryUsage memoryUsage) {
		double value = (WIDTH + 2.0d) / memoryUsage.getMax();
		long used = Math.round(memoryUsage.getUsed() * value);
		long committed = Math.round(memoryUsage.getCommitted() * value);

		// print first line
		StringBuilder sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < WIDTH; i++) {
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
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Health Service active...");
		}
	}

}
