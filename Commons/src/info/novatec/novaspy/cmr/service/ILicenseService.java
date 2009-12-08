package info.novatec.novaspy.cmr.service;

/**
 * This is used for communication between the GUI and CMR regarding the license
 * management. It is responsible for transferring the license file from the GUI
 * to the CMR.
 * 
 * @author Dirk Maucher
 */
public interface ILicenseService {

	/**
	 * Receives a byte[] of a license file content that should stored within the
	 * CMR directory.
	 * 
	 * @param licenseContent
	 *            The license file content.
	 */
	public void receiveLicenseContent(byte[] licenseContent) throws Exception;

}
