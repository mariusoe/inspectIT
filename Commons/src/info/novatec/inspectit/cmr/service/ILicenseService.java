package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.cmr.LicenseInfoData;

/**
 * This is used for communication between the GUI and CMR regarding the license management. It is
 * responsible for transferring the license file from the GUI to the CMR.
 * 
 * @author Dirk Maucher
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ILicenseService {

	/**
	 * Receives a byte[] of a license file content that should stored within the CMR directory.
	 * 
	 * @param licenseContent
	 *            The license file content.
	 * @throws Exception
	 *             in case of problems.
	 */
	void receiveLicenseContent(byte[] licenseContent) throws Exception;

	/**
	 * Returns the license information for the CMR.
	 * 
	 * @return This method returns the {@link LicenseInfoData} object filled with license
	 *         information, or null if no license is imported.
	 */
	LicenseInfoData getLicenseInfoData();

}
