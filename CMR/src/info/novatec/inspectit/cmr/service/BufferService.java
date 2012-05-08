package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.BufferStatusData;
import info.novatec.inspectit.spring.logger.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Ivan Senic
 *
 */
@Service
public class BufferService implements IBufferService {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * Buffer data dao.
	 */
	@Autowired
	private IBuffer<DefaultData> buffer;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void clearBuffer() {
		buffer.clearAll();
	}


	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public BufferStatusData getBufferStatusData() {
		BufferStatusData bufferStatusData = new BufferStatusData();
		bufferStatusData.setCurrentBufferSize(buffer.getCurrentSize());
		bufferStatusData.setMaxBufferSize(buffer.getMaxSize());
		bufferStatusData.setBufferOldestElement(buffer.getOldestElement());
		bufferStatusData.setBufferNewestElement(buffer.getNewestElement());
		return bufferStatusData;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 *
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Buffer Service active...");
		}
	}

}
