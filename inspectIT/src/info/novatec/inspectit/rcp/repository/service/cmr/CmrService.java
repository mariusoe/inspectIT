package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * Abstract class for all {@link CmrRepositoryDefinition} service classes.
 * 
 * @author Ivan Senic
 * 
 */
public class CmrService implements ICmrService {

	/**
	 * Protocol used.
	 */
	private static final String PROTOCOL = "http://";

	/**
	 * Remoting path.
	 */
	private static final String REMOTING = "/remoting/";

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Real service where calls will be executed.
	 */
	private Object service;

	/**
	 * Service interface.
	 */
	private Class<?> serviceInterface;

	/**
	 * Service name.
	 */
	private String serviceName;

	/**
	 * {@inheritDoc}
	 */
	public void initService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();

		// we need to set the class loader on our own
		// the problems is that the service interface class can not be found
		// I am not quite sure why, but this is suggested on several places as a patch
		httpInvokerProxyFactoryBean.setBeanClassLoader(getClass().getClassLoader());

		httpInvokerProxyFactoryBean.setServiceInterface(serviceInterface);
		httpInvokerProxyFactoryBean.setServiceUrl(PROTOCOL + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + REMOTING + serviceName);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		service = httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getService() {
		return service;
	}

	/**
	 * @param serviceInterface
	 *            the serviceInterface to set
	 */
	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	/**
	 * @return Returns the service interface class.
	 */
	public Class<?> getServiceInterface() {
		return serviceInterface;
	}

	/**
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
