package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.IPlatformSensor;
import info.novatec.inspectit.communication.data.CompilationInformationData;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provide dynamic informations about compilation through MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CompilationInformation implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	private static Logger logger = Logger.getLogger(CompilationInformation.class.getName());

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	private final IIdManager idManager;

	/**
	 * The MXBean for compilation informations.
	 */
	private CompilationMXBean compilationObj = ManagementFactory.getCompilationMXBean();

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public CompilationInformation(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Returns the approximate accumulated elapsed time (milliseconds) spent in
	 * compilation.
	 * 
	 * @return the compilation time in milliseconds.
	 */
	public long getTotalCompilationTime() {
		try {
			return compilationObj.getTotalCompilationTime();
		} catch (UnsupportedOperationException ex) {
			return 0;
		}
	}

	/**
	 * Updates all dynamic compilation informations.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		long totalCompilationTime = this.getTotalCompilationTime();

		CompilationInformationData compilationData = (CompilationInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (compilationData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				compilationData = new CompilationInformationData(timestamp, platformId, registeredSensorTypeId);
				compilationData.incrementCount();

				compilationData.addTotalCompilationTime(totalCompilationTime);
				compilationData.setMinTotalCompilationTime(totalCompilationTime);
				compilationData.setMaxTotalCompilationTime(totalCompilationTime);

				coreService.addPlatformSensorData(sensorTypeIdent, compilationData);
			} catch (IdNotAvailableException e) {
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Could not save the compilation information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			compilationData.incrementCount();
			compilationData.addTotalCompilationTime(totalCompilationTime);

			if (totalCompilationTime < compilationData.getMinTotalCompilationTime()) {
				compilationData.setMinTotalCompilationTime(totalCompilationTime);
			} else if (totalCompilationTime > compilationData.getMaxTotalCompilationTime()) {
				compilationData.setMaxTotalCompilationTime(totalCompilationTime);
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
