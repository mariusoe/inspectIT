package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.service.IServerStatusService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of the {@link IServerStatusService} interface to provide
 * information about the current status of the CMR.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ServerStatusService implements IServerStatusService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ServerStatusService.class);

	/**
	 * The status of the CMR.
	 */
	private int status = IServerStatusService.SERVER_STARTING;

	/**
	 * {@inheritDoc}
	 */
	public int getServerStatus() {
		return status;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		status = IServerStatusService.SERVER_ONLINE;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Server Status Service active...");
		}
	}

}
