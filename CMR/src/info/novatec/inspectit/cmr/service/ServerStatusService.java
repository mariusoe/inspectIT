package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.versioning.IVersioningService;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
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

	/** The logger of this class. */
	@Logger
	Log log;

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
	@MethodLog
	public int getServerStatus() {
		return status;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		status = IServerStatusService.SERVER_ONLINE;

		if (log.isInfoEnabled()) {
			log.info("|-Server Status Service active...");
		}
	}

	@Override
	public String getVersion() {
		try {
			return versioning.getVersion();
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("Cannot obtain current version", e);
			}
			return IServerStatusService.VERSION_NOT_AVAILABLE;
		}
	}

}
