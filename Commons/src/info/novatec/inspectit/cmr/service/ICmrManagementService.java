package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.cmr.CmrStatusData;

/**
 * Service that provides general management of the CMR.
 * 
 * @author Ivan Senic
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ICmrManagementService {

	/**
	 * Clear whole buffer.
	 */
	void clearBuffer();

	/**
	 * Returns the current buffer status via {@link CmrStatusData}.
	 * 
	 * @return {@link CmrStatusData}.
	 */
	CmrStatusData getCmrStatusData();
}
