package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.IBufferService;
import info.novatec.inspectit.rcp.InspectIT;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * 
 * @author Ivan Senic
 * 
 */
public class BufferService implements IBufferService {

	/**
	 * The name of the buffer data access service.
	 */
	private static final String BUFFER_SERVICE = "BufferService";

	/**
	 * The proxy factory bean by Spring which initializes the data access service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The buffer data access service exposed by the CMR and initialized by Spring.
	 */
	private final IBufferService bufferDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public BufferService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IBufferService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + BUFFER_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		bufferDataAccessService = (IBufferService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearBuffer() {
		try {
			bufferDataAccessService.clearBuffer();
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error trying to empty the buffer on the CMR!", e, -1);
		}

	}

}
