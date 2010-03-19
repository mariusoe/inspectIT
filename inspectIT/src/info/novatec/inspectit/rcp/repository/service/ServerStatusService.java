package info.novatec.inspectit.rcp.repository.service;

import info.novatec.inspectit.cmr.service.IServerStatusService;
import info.novatec.inspectit.rcp.InspectIT;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * @author Patrice Bouillet
 * 
 */
public class ServerStatusService implements IServerStatusService {

	/**
	 * The server status service name.
	 */
	private static final String SERVER_STATUS_SERVICE = "ServerStatusService";

	/**
	 * The proxy factory bean by Spring which initializes the data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The server status service exposed by the CMR and initialized by Spring.
	 */
	private final IServerStatusService serverStatusService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public ServerStatusService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IServerStatusService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + SERVER_STATUS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		serverStatusService = (IServerStatusService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getServerStatus() {
		return serverStatusService.getServerStatus();
	}

	/**
	 * Returns if the CMR is online and ready to return some data.
	 * 
	 * @return The state of the CMR. Returns <code>true</code> if the server is
	 *         online, <code>false</code> otherwise.
	 */
	public boolean isOnline() {
		try {
			int status = getServerStatus();

			if (IServerStatusService.SERVER_ONLINE == status) {
				return true;
			}

			return false;
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("Could net get online status!", e, -1);
			return false;
		}
	}

	@Override
	public String getVersion() {
		return serverStatusService.getVersion();
	}

}
