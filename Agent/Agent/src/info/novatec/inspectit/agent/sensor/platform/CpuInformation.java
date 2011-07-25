package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import info.novatec.inspectit.communication.data.CpuInformationData;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides dynamic information about the underlying operating system through MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CpuInformation implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CpuInformation.class.getName());

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	private final IIdManager idManager;

	/**
	 * The {@link OperatingSystemInfoProvider} used to retrieve information from the operating
	 * system.
	 */
	private OperatingSystemInfoProvider osBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getOperatingSystemInfoProvider();

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
		return osBean.getProcessCpuTime();
	}

	/**
	 * Updates all dynamic cpu information.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		long processCpuTime = this.getProcessCpuTime();
		float cpuUsage = osBean.retrieveCpuUsage();

		CpuInformationData osData = (CpuInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (osData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				osData = new CpuInformationData(timestamp, platformId, registeredSensorTypeId);
				osData.incrementCount();

				osData.updateProcessCpuTime(processCpuTime);

				osData.addCpuUsage(cpuUsage);
				osData.setMinCpuUsage(cpuUsage);
				osData.setMaxCpuUsage(cpuUsage);

				coreService.addPlatformSensorData(sensorTypeIdent, osData);
			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not save the cpu information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			osData.incrementCount();
			osData.updateProcessCpuTime(processCpuTime);
			osData.addCpuUsage(cpuUsage);

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
	public void init(Map<String, Object> parameter) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean automaticUpdate() {
		return true;
	}
}
