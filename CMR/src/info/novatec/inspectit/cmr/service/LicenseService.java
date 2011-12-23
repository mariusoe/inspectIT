package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.spring.logger.Logger;
import info.novatec.inspectit.cmr.util.LicenseUtil;
import info.novatec.inspectit.communication.data.LicenseInfoData;

import java.io.File;
import java.io.FileOutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the {@link ILicenseService} interface.
 * 
 * @author Dirk Maucher
 */
@Service
public class LicenseService implements ILicenseService {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The license utility for reinitializing.
	 */
	@Autowired
	private LicenseUtil licenseUtil;

	/**
	 * {@inheritDoc}
	 */
	public void receiveLicenseContent(byte[] licenseContent) throws Exception {
		FileOutputStream licenseFileContent = new FileOutputStream(new File(LicenseUtil.LICENSE_FILE));
		licenseFileContent.write(licenseContent);
		licenseFileContent.close();

		// initialize again
		log.info("Restarting licensing module with new content...");
		licenseUtil.initializeLicense();
		log.info("Licensing module restarted.");
	}

	/**
	 * {@inheritDoc}
	 */
	public LicenseInfoData getLicenseInfoData() {
		return licenseUtil.getLicenceInfoData();
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		// initialize the license
		try {
			if (log.isInfoEnabled()) {
				log.info("|-Starting licensing module...");
			}

			licenseUtil.initializeLicense();

			if (log.isInfoEnabled()) {
				log.info("|-License Service active...");
			}
		} catch (Exception exception) {
			if (null == exception.getMessage() || "".equals(exception.getMessage())) {
				log.error("||-Licensing module could not be started, reason: " + exception.getClass().getName());
			} else {
				log.error("||-Licensing module could not be started, reason: " + exception.getMessage());
			}
			log.error("||-Please make sure that a license is properly imported");
			log.error("||-A license can be imported through the User Interface");
			log.error("||-Please contact NovaTec Support or visit http://www.inspectit.eu to receive your own key file!");
		}
	}
}
