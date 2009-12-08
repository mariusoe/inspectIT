package info.novatec.novaspy.agent.sensor.platform;

import info.novatec.novaspy.agent.core.ICoreService;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IdNotAvailableException;
import info.novatec.novaspy.agent.sensor.platform.IPlatformSensor;
import info.novatec.novaspy.communication.data.CpuInformationData;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.management.OperatingSystemMXBean;

/**
 * This class provide dynamic informations about the underlying operating system
 * through MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CpuInformation implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	private static Logger logger = Logger.getLogger(CpuInformation.class.getName());

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	private final IIdManager idManager;

	/**
	 * Reference for cpu calculation stuff.
	 */
	private CpuCalculation cpuCalc = new CpuCalculation();

	/**
	 * The previous uptime of the virtual machine.
	 */
	private long prevUptime = 0;

	/**
	 * The previous processCpuTime.
	 */
	private long prevProcessCpuTime = 0;

	/**
	 * The cpu usage.
	 */
	private float cpuUsage = 0;

	/**
	 * The MXBean for the operating system informations.
	 */
	private OperatingSystemMXBean osObj = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	/**
	 * The MXBean for runtime informations.
	 */
	private RuntimeMXBean runtimeObj = ManagementFactory.getRuntimeMXBean();

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public CpuInformation(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Returns the process cpu time.
	 * 
	 * @return the process cpu time.
	 */
	public long getProcessCpuTime() {
		return osObj.getProcessCpuTime();
	}

	/**
	 * Returns the cpu usage of the Virtual Machine.
	 * 
	 * @return the cpu usage.
	 */
	public float getCpuUsage() {
		return cpuUsage;
	}

	/**
	 * Updates all dynamic cpu informations.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		cpuCalc.uptime = runtimeObj.getUptime();
		long processCpuTime = this.getProcessCpuTime();
		cpuCalc.processCpuTime = processCpuTime;
		cpuCalc.availableProcessors = osObj.getAvailableProcessors();
		this.updateCpuInfo(cpuCalc);
		float cpuUsage = this.getCpuUsage();

		CpuInformationData osData = (CpuInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (osData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				osData = new CpuInformationData(timestamp, platformId, registeredSensorTypeId);
				osData.incrementCount();

				osData.addProcessCpuTime(processCpuTime);
				osData.setMinProcessCpuTime(processCpuTime);
				osData.setMaxProcessCpuTime(processCpuTime);

				osData.addCpuUsage(cpuUsage);
				osData.setMinCpuUsage(cpuUsage);
				osData.setMaxCpuUsage(cpuUsage);

				coreService.addPlatformSensorData(sensorTypeIdent, osData);
			} catch (IdNotAvailableException e) {
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Could not save the cpu information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			osData.incrementCount();
			osData.addProcessCpuTime(processCpuTime);
			osData.addCpuUsage(cpuUsage);

			if (processCpuTime < osData.getMinProcessCpuTime()) {
				osData.setMinProcessCpuTime(processCpuTime);
			} else if (processCpuTime > osData.getMaxProcessCpuTime()) {
				osData.setMaxProcessCpuTime(processCpuTime);
			}

			if (cpuUsage < osData.getMinCpuUsage()) {
				osData.setMinCpuUsage(cpuUsage);
			} else if (cpuUsage > osData.getMaxCpuUsage()) {
				osData.setMaxCpuUsage(cpuUsage);
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

	/**
	 * Inner Class for cpuUsage calculation stuff
	 * 
	 * @author Eduard Tudenhoefner
	 */
	private static class CpuCalculation {
		/**
		 * The uptime.
		 */
		long uptime = -1L;

		/**
		 * The process cpu time.
		 */
		long processCpuTime = -1L;

		/**
		 * The available processors.
		 */
		int availableProcessors = 0;
	}

	/**
	 * Calculates the current cpuUsage in percent.
	 * 
	 * elapsedCpu is in ns and elapsedTime is in ms. cpuUsage could go higher
	 * than 100% because elapsedTime and elapsedCpu are not fetched
	 * simultaneously. Limit to 99% to avoid showing a scale from 0% to 200%.
	 * 
	 * @param cpuCalc
	 */
	public void updateCpuInfo(CpuCalculation cpuCalc) {
		if (prevUptime > 0L && cpuCalc.uptime > prevUptime) {

			long elapsedCpu = cpuCalc.processCpuTime - prevProcessCpuTime;
			long elapsedTime = cpuCalc.uptime - prevUptime;

			cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * cpuCalc.availableProcessors));
		}
		this.prevUptime = cpuCalc.uptime;
		this.prevProcessCpuTime = cpuCalc.processCpuTime;
	}

}
