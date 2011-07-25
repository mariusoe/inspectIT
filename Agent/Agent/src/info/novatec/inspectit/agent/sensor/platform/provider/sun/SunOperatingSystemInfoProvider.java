package info.novatec.inspectit.agent.sensor.platform.provider.sun;

import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.sun.management.OperatingSystemMXBean;

/**
 * This class retrieves all the data as {@link OperatingSystemInfoProvider} from
 * {@link OperatingSystemMXBean} from Sun.
 * 
 * @see com.sun.management.OperatingSystemMXBean
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class SunOperatingSystemInfoProvider implements OperatingSystemInfoProvider {

	/**
	 * The managed bean to retrieve the OS information from.
	 */
	private OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	/**
	 * The managed bean to retrieve information about the uptime of the JVM.
	 */
	private RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

	/**
	 * The calculator used to calculate and retrieve the current CPU usage of the underlying JVM.
	 */
	private CpuUsageCalculator cpuCalculator = new CpuUsageCalculator();

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return osBean.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getArch() {
		return osBean.getArch();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return osBean.getVersion();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getAvailableProcessors() {
		return osBean.getAvailableProcessors();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getCommittedVirtualMemorySize() {
		return osBean.getCommittedVirtualMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreePhysicalMemorySize() {
		return osBean.getFreePhysicalMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreeSwapSpaceSize() {
		return osBean.getFreeSwapSpaceSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getProcessCpuTime() {
		return osBean.getProcessCpuTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalPhysicalMemorySize() {
		return osBean.getTotalPhysicalMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalSwapSpaceSize() {
		return osBean.getTotalSwapSpaceSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public float retrieveCpuUsage() {
		cpuCalculator.setUptime(runtimeBean.getUptime());
		cpuCalculator.setProcessCpuTime(this.getProcessCpuTime());
		cpuCalculator.setAvailableProcessors(this.getAvailableProcessors());
		cpuCalculator.updateCpuUsage();

		return cpuCalculator.getCpuUsage();
	}

	/**
	 * This class is used to calculate the cpu usage of the underlying Virtual Machine.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static class CpuUsageCalculator {
		/**
		 * The uptime.
		 */
		private long uptime = -1L;

		/**
		 * The process cpu time.
		 */
		private long processCpuTime = -1L;

		/**
		 * The available processors.
		 */
		private int availableProcessors = 0;

		/**
		 * The previous uptime of the Virtual Machine.
		 */
		private long prevUptime = 0L;

		/**
		 * The previous processCpuTime.
		 */
		private long prevProcessCpuTime = 0L;

		/**
		 * The cpu usage.
		 */
		private float cpuUsage = 0.0F;

		/**
		 * Gets {@link #cpuUsage}.
		 * 
		 * @return {@link #cpuUsage}
		 */
		public float getCpuUsage() {
			return cpuUsage;
		}

		/**
		 * Sets {@link #uptime}.
		 * 
		 * @param uptime
		 *            New value for {@link #uptime}
		 */
		public void setUptime(long uptime) {
			this.uptime = uptime;
		}

		/**
		 * Sets {@link #processCpuTime}.
		 * 
		 * @param processCpuTime
		 *            New value for {@link #processCpuTime}
		 */
		public void setProcessCpuTime(long processCpuTime) {
			this.processCpuTime = processCpuTime;
		}

		/**
		 * Sets {@link #availableProcessors}.
		 * 
		 * @param availableProcessors
		 *            New value for {@link #availableProcessors}
		 */
		public void setAvailableProcessors(int availableProcessors) {
			this.availableProcessors = availableProcessors;
		}

		/**
		 * Calculates the current cpuUsage in percent.
		 * 
		 * elapsedCpu is in ns and elapsedTime is in ms. cpuUsage could go higher than 100% because
		 * elapsedTime and elapsedCpu are not fetched simultaneously. Limit to 99% to avoid showing
		 * a scale from 0% to 200%.
		 * 
		 */
		public void updateCpuUsage() {
			if (prevUptime > 0L && this.uptime > prevUptime) {
				long elapsedCpu = this.processCpuTime - prevProcessCpuTime;
				long elapsedTime = this.uptime - prevUptime;

				cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * this.availableProcessors));
			}
			this.prevUptime = this.uptime;
			this.prevProcessCpuTime = this.processCpuTime;
		}
	}
}
