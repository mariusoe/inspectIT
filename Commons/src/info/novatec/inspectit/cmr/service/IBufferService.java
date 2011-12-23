package info.novatec.inspectit.cmr.service;

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

}
