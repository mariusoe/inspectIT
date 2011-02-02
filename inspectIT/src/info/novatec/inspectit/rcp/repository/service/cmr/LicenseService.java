package info.novatec.inspectit.rcp.repository.service.cmr;

import java.util.Collections;

import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.communication.data.LicenseInfoData;
import info.novatec.inspectit.rcp.InspectIT;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * @author Dirk Maucher
 * 
 */
public class LicenseService implements ILicenseService {

	/**
	 * The license service name.
	 */
	private static final String LICENSE_SERVICE = "LicenseService";

	/**
	 * The proxy factory bean by Spring which initializes the data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The license service exposed by the CMR and initialized by Spring.
	 */
	private final ILicenseService licenseService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public LicenseService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(ILicenseService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + LICENSE_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		licenseService = (ILicenseService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public void receiveLicenseContent(byte[] licenseContent) throws Exception {
		licenseService.receiveLicenseContent(licenseContent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LicenseInfoData getLicenseInfoData() {
		try {
			return licenseService.getLicenseInfoData();
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the licence information from the CMR!", e, -1);
			return null;
		}
	}

}
