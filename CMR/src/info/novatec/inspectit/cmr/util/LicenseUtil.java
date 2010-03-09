package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.cmr.CMR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseContentException;
import de.schlichtherle.license.LicenseManager;
import de.schlichtherle.license.LicenseParam;
import de.schlichtherle.util.ObfuscatedString;

/**
 * This license util holds the informations about the license file, license
 * manager, license content, license extras and the registered agents.
 * 
 * @author Dirk Maucher
 * @author Patrice Bouillet
 * 
 */
public class LicenseUtil {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LicenseUtil.class);

	/**
	 * The license file which should be read. This will also accessed by other
	 * classes who need that information.
	 */
	public static final String LICENSE_FILE = "license/license.lic";

	/**
	 * The public key store file which should be used. This will also accessed
	 * by other classes who need that information.
	 */
	public static final String PUBLIC_KEY_STORE = "license/publicCerts.store";

	/**
	 * The key which is used to describe the allowed amount of agents.
	 */
	public static final String ALLOWED_AGENT_AMOUNT_KEY = "AGENTS";

	/**
	 * The license file used for licensing.
	 */
	private File licenseFile = new File(LICENSE_FILE);

	/**
	 * The license content which reflects the content of the license file.
	 */
	private LicenseContent licenseContent;

	/**
	 * Additional parameters of the license. In this case the key "AGENTS" with
	 * the value of the allowed agent amount.
	 */
	private Map<String, String> licenseExtras;

	/**
	 * A set of currently registered agents that are covered by the license and
	 * allowed to run.
	 */
	private Set<LicenseUtilData> registeredAgents = new HashSet<LicenseUtilData>();

	/**
	 * The inner class to retrieve the publicCerts.store file.
	 */
	private final KeyStoreParam publicKeyStoreParam = new KeyStoreParam() {
		public InputStream getStream() throws IOException {
			File certificateFile = new File(PUBLIC_KEY_STORE);
			final InputStream in = new FileInputStream(certificateFile);
			return in;
		}

		public String getAlias() {
			// the name of the certificate within the certificate store
			return "publiccert";
		}

		public String getStorePwd() {
			// this method returns the obfuscated key for the store
			return new ObfuscatedString(new long[] { 0xBDED01C9A633A4AAL, 0xBA751FB48635B1C4L, 0xE3B17777D6F02DFAL, 0xFE9AA9840B0D3E96L }).toString();
		}

		public String getKeyPwd() {
			// this method MUST return null because this is the private key and
			// should NOT be delivered with the application
			return null;
		}
	};

	/**
	 * The key password for encryption.
	 */
	private final CipherParam cipherParam = new CipherParam() {
		public String getKeyPwd() {
			// this is the obfuscated key to decrypt the license file
			return new ObfuscatedString(new long[] { 0x8333FC9A802318CDL, 0x3F87EE971D38A319L, 0xD966F7C536CC74A0L, 0xCD6052D407D82403L }).toString();
		}
	};

	/**
	 * The subject of the software which should be licensed.
	 */
	private final LicenseParam licenseParam = new LicenseParam() {
		public String getSubject() {
			// this is the subject which MUST be exactly the same than in the
			// license file - if not the license file is not valid. This subject
			// is also obfuscated
			return new ObfuscatedString(new long[] {0x7BE0D7A91744BA11L, 0x8EE7897329DAD231L, 0xB882D9CD8679FBC0L, 0xE3EDA6F30961559FL, 0xC86442D44DD50199L, 0xD0B0285B561195C5L}).toString() ;
		}

		public Preferences getPreferences() {
			// the license content will be saved on the system - in this case it
			// will be saved under the package name and the class name of CMR
			return Preferences.userNodeForPackage(CMR.class);
		}

		public KeyStoreParam getKeyStoreParam() {
			return publicKeyStoreParam;
		}

		public CipherParam getCipherParam() {
			return cipherParam;
		}
	};

	/**
	 * The license manager instance to install and verify the license file.
	 */
	private LicenseManager licenseManager = new LicenseManager(licenseParam);

	/**
	 * Responsible for installing the license file and verify if it is valid or
	 * not.
	 * 
	 * @throws Exception
	 *             Exceptions like FileNotFoundException or
	 *             LicenseContentNotValidException
	 */
	@SuppressWarnings("unchecked")
	public void initializeLicense() throws Exception {
		if (!(new File(PUBLIC_KEY_STORE)).exists()) {
			LOGGER.error("||-No public key file available, shutting down CMR! Please contact NovaTec Support to receive your own key file!");
			System.exit(-1);
		}
		// uninstall first the license on the pc, we always want a fresh one
		licenseManager.uninstall();
		licenseManager.install(licenseFile);
		licenseContent = licenseManager.verify();
		licenseExtras = (HashMap<String, String>) licenseContent.getExtra();

		LOGGER.info("||-# of concurrent Agents allowed: " + licenseExtras.get(ALLOWED_AGENT_AMOUNT_KEY));
	}

	/**
	 * This method is called every time a agents connects to the CMR.
	 * 
	 * @param definedIPs
	 *            a List of IPs from the connecting agent
	 * @param agentName
	 *            the name of the connecting agent
	 * 
	 * @throws LicenseContentException
	 *             Every problem regarding reading or processing the license
	 *             file is reported to the caller.
	 */
	public void validateLicense(List<String> definedIPs, String agentName) throws LicenseContentException {
		try {
			// this checks the license for one specific thing: the system time
			// must be between the issued date of the license and the
			// valid-until date of the license. The subject is also checked by
			// this method.
			licenseManager.verify();
		} catch (Exception e) {
			throw new LicenseContentException(e.getMessage());
		}

		// registers the currently connected agent to our local set
		LicenseUtilData licenseUtilData = new LicenseUtilData();
		licenseUtilData.setAgentName(agentName);
		licenseUtilData.setDefinedIPs(definedIPs);

		// I am adding the incoming agent in every case because I am using the
		// function of the set that did not allow a duplicate item. With this
		// way I save the explicit check if the agent is already registered or
		// not.
		registeredAgents.add(licenseUtilData);

		// checks that the amount of allowed agents is higher than the amount of
		// the connected agents
		if (Integer.parseInt(licenseExtras.get(ALLOWED_AGENT_AMOUNT_KEY)) < registeredAgents.size()) {
			// removing the last added agent because it is not allowed to run.
			// As described above - I added this agent before but now I am
			// removing it because the next agent which was already registered
			// and is trying to "re-register" should be allowed to do so.
			registeredAgents.remove(licenseUtilData);

			// if not we throw a exception to the caller that he knows that the
			// license is not valid
			throw new LicenseContentException("Maximum Agent Count Reached");
		}

		LOGGER.info("Valid license for Agent '" + agentName + "'");
		LOGGER.info("Remaining Agent slots: " + (Integer.parseInt(licenseExtras.get(ALLOWED_AGENT_AMOUNT_KEY)) - registeredAgents.size()));
	}

}
