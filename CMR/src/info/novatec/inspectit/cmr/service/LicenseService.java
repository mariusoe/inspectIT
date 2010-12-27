package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.util.LicenseUtil;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Default implementation of the {@link ILicenseService} interface.
 * 
 * @author Dirk Maucher
 */
public class LicenseService implements ILicenseService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LicenseService.class);

	/**
	 * The license utility for reinitializing.
	 */
	private LicenseUtil licenseUtil;

	/**
	 * {@inheritDoc}
	 */
	public void receiveLicenseContent(byte[] licenseContent) throws Exception {
		FileOutputStream licenseFileContent = new FileOutputStream(new File(LicenseUtil.LICENSE_FILE));
		licenseFileContent.write(licenseContent);
		licenseFileContent.close();

		// initialize again
		LOGGER.info("Restarting licensing module with new content...");
		licenseUtil.initializeLicense();
		LOGGER.info("Licensing module restarted.");
	}

	/**
	 * @param licenseUtil
	 *            the licenseUtil to set
	 */
	public void setLicenseUtil(LicenseUtil licenseUtil) {
		this.licenseUtil = licenseUtil;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		// initialize the license
		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("|-Starting licensing module...");
			}

			licenseUtil.initializeLicense();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("|-License Service active...");
			}
		} catch (Exception exception) {
			if (null == exception.getMessage() || "".equals(exception.getMessage())) {
				LOGGER.error("||-Licensing module could not be started, reason: " + exception.getClass().getName());
			} else {
				LOGGER.error("||-Licensing module could not be started, reason: " + exception.getMessage());
			}
			LOGGER.error("||-Please make sure that a license is properly imported");
			LOGGER.error("||-A license can be imported through the User Interface");
			LOGGER.error("||-Contact NovaTec Support for further help!");
		}
	}
}
