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

	/**
	 * Reports that an amount of data has been dropped.
	 * 
	 * @param count
	 *            Dropped amount.
	 */
	void addDroppedDataCount(int count);

	/**
	 * Returns the number of data objects that have been dropped on the CMR, due to the high
	 * incoming load.
	 * 
	 * @return Returns the number of data objects that have been dropped on the CMR, due to the high
	 *         incoming load.
	 */
	int getDroppedDataCount();
}
