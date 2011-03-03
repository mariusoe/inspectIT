package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.util.aop.Log;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Ivan Senic
 * 
 */
@Service
public class BufferService implements IBufferService {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(BufferService.class);

	/**
	 * Buffer data dao.
	 */
	@Autowired
	private IBuffer<?> buffer;

	/**
	 * {@inheritDoc}
	 */
	@Log
	public void clearBuffer() {
		buffer.clearAll();
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Buffer Service active...");
		}
	}

}
