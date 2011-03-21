package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.cmr.BufferStatusData;

/**
 * Service that defines the possible general requests to the buffer.
 * 
 * @author Ivan Senic
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IBufferService {

	/**
	 * Clear whole buffer.
	 */
	void clearBuffer();

	/**
	 * Returns the current buffer status via {@link BufferStatusData}.
	 * 
	 * @return {@link BufferStatusData}.
	 */
	BufferStatusData getBufferStatusData();
}
