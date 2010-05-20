package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.ILicenseService;

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

}
