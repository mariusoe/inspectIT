package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.communication.data.RuntimeInformationData;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides dynamic information about the runtime of the Virtual Machine through MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class RuntimeInformation implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(RuntimeInformation.class.getName());

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	private final IIdManager idManager;

	/**
	 * The MXBean used to retrieve information from the runtime system of the underlying Virtual
	 * Machine.
	 */
	private RuntimeMXBean runtimeObj = ManagementFactory.getRuntimeMXBean();

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public RuntimeInformation(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Returns the uptime of the virtual machine in milliseconds.
	 * 
	 * @return the uptime in milliseconds.
	 */
	public long getUptime() {
		return runtimeObj.getUptime();
	}

	/**
	 * Updates all dynamic runtime informations.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		long uptime = this.getUptime();

		RuntimeInformationData runtimeData = (RuntimeInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (runtimeData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
				runtimeData = new RuntimeInformationData(timestamp, platformId, registeredSensorTypeId);

				runtimeData.incrementCount();
				runtimeData.addUptime(uptime);
				runtimeData.setMinUptime(uptime);
				runtimeData.setMaxUptime(uptime);

				coreService.addPlatformSensorData(sensorTypeIdent, runtimeData);
			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not save the runtime information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {

			runtimeData.incrementCount();
			runtimeData.addUptime(uptime);

			if (uptime < runtimeData.getMinUptime()) {
				runtimeData.setMinUptime(uptime);
			} else if (uptime > runtimeData.getMaxUptime()) {
				runtimeData.setMaxUptime(uptime);
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
