package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.versioning.IVersioningService;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of the {@link IServerStatusService} interface to provide information about the
 * current status of the CMR.
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
	 * The versioning Service.
	 */
	private IVersioningService versioning;

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

	@Override
	public String getVersion() {
		try {
			return versioning.getVersion();
		} catch (IOException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Cannot obtain current version", e);
			}
			return "n/a";
		}
	}

	public void setVersioning(IVersioningService service) {
		this.versioning = service;
	}

}
