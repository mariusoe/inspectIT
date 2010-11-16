package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.util.aop.Log;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Ivan Senic
 * 
 */
public class BufferService implements IBufferService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(BufferService.class);

	/**
	 * Buffer data dao.
	 */
	private IBuffer<?> buffer;

	/**
	 * {@inheritDoc}
	 */
	@Log
	public void clearBuffer() {
		buffer.clearAll();
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Buffer Data Access Service active...");
		}
	}

	/**
	 * 
	 * @param buffer
	 *            buffer to set
	 */
	public void setBuffer(IBuffer<?> buffer) {
		this.buffer = buffer;
	}

}
