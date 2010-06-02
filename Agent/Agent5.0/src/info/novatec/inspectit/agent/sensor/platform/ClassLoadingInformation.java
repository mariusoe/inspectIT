package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.IPlatformSensor;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides dynamic information about the class loading system
 * through MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ClassLoadingInformation implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	private static Logger logger = Logger.getLogger(ClassLoadingInformation.class.getName());

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	private final IIdManager idManager;

	/**
	 * The MXBean used to retrieve information from the class loading system.
	 */
	private ClassLoadingMXBean classLoadingObj = ManagementFactory.getClassLoadingMXBean();

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public ClassLoadingInformation(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Returns the number of loaded classes in the virtual machine.
	 * 
	 * @return The number of loaded classes.
	 */
	public int getLoadedClassCount() {
		return classLoadingObj.getLoadedClassCount();
	}

	/**
	 * Returns the total number of loaded classes since the virtual machine
	 * started.
	 * 
	 * @return The total number of loaded classes.
	 */
	public long getTotalLoadedClassCount() {
		return classLoadingObj.getTotalLoadedClassCount();
	}

	/**
	 * Returns the number of unloaded classes since the virtual machine started.
	 * 
	 * @return The number of unloaded classes.
	 */
	public long getUnloadedClassCount() {
		return classLoadingObj.getUnloadedClassCount();
	}

	/**
	 * Updates all dynamic class loading information.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		int loadedClassCount = this.getLoadedClassCount();
		long totalLoadedClassCount = this.getTotalLoadedClassCount();
		long unloadedClassCount = this.getUnloadedClassCount();

		ClassLoadingInformationData classLoadingData = (ClassLoadingInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (classLoadingData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				classLoadingData = new ClassLoadingInformationData(timestamp, platformId, registeredSensorTypeId);
				classLoadingData.incrementCount();

				classLoadingData.addLoadedClassCount(loadedClassCount);
				classLoadingData.setMinLoadedClassCount(loadedClassCount);
				classLoadingData.setMaxLoadedClassCount(loadedClassCount);

				classLoadingData.addTotalLoadedClassCount(totalLoadedClassCount);
				classLoadingData.setMinTotalLoadedClassCount(totalLoadedClassCount);
				classLoadingData.setMaxTotalLoadedClassCount(totalLoadedClassCount);

				classLoadingData.addUnloadedClassCount(unloadedClassCount);
				classLoadingData.setMinUnloadedClassCount(unloadedClassCount);
				classLoadingData.setMaxUnloadedClassCount(unloadedClassCount);

				coreService.addPlatformSensorData(sensorTypeIdent, classLoadingData);
			} catch (IdNotAvailableException e) {
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Could not save the class loading information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			classLoadingData.incrementCount();
			classLoadingData.addLoadedClassCount(loadedClassCount);
			classLoadingData.addTotalLoadedClassCount(totalLoadedClassCount);
			classLoadingData.addUnloadedClassCount(unloadedClassCount);

			if (loadedClassCount < classLoadingData.getMinLoadedClassCount()) {
				classLoadingData.setMinLoadedClassCount(loadedClassCount);
			} else if (loadedClassCount > classLoadingData.getMaxLoadedClassCount()) {
				classLoadingData.setMaxLoadedClassCount(loadedClassCount);
			}

			if (totalLoadedClassCount < classLoadingData.getMinTotalLoadedClassCount()) {
				classLoadingData.setMinTotalLoadedClassCount(totalLoadedClassCount);
			} else if (totalLoadedClassCount > classLoadingData.getMaxTotalLoadedClassCount()) {
				classLoadingData.setMaxTotalLoadedClassCount(totalLoadedClassCount);
			}

			if (unloadedClassCount < classLoadingData.getMinUnloadedClassCount()) {
				classLoadingData.setMinUnloadedClassCount(unloadedClassCount);
			} else if (unloadedClassCount > classLoadingData.getMaxUnloadedClassCount()) {
				classLoadingData.setMaxUnloadedClassCount(unloadedClassCount);
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
