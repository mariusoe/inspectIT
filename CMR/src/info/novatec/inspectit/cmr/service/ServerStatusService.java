package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.versioning.IVersioningService;

import javax.annotation.PostConstruct;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link IServerStatusService} interface to provide information about the
 * current status of the CMR.
 * 
 * @author Patrice Bouillet
 * 
 */
@Service
public class ServerStatusService implements IServerStatusService {

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
	@Autowired
	private IVersioningService versioning;

	/**
	 * {@inheritDoc}
	 */
	public int getServerStatus() {
		return status;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
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

}
