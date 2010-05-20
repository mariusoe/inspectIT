package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.service.AbstractCachedGlobalDataAccessService;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * The data access service accessing the remote server.
 * 
 * @author Patrice Bouillet
 * 
 */
public class GlobalDataAccessService extends AbstractCachedGlobalDataAccessService {

	/**
	 * The Name of the global data access service.
	 */
	private static final String GLOBAL_DATA_ACCESS_SERVICE = "GlobalDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The global data access service exposed by the CMR and initialized by
	 * Spring.
	 */
	private final IGlobalDataAccessService globalDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public GlobalDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IGlobalDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + GLOBAL_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		globalDataAccessService = (IGlobalDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformIdent> getConnectedAgents() {
		try {
			final List<PlatformIdent> platformIdents = globalDataAccessService.getConnectedAgents();

			putIntoCache(platformIdents);

			return platformIdents;
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the agent details from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		try {
			return globalDataAccessService.getLastDataObjects(template, timeInterval);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the last data objects (time) from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		try {
			return globalDataAccessService.getDataObjectsFromToDate(template, fromDate, toDate);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving data objects from->to from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData getLastDataObject(DefaultData template) {
		try {
			return globalDataAccessService.getLastDataObject(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the last data object from the CMR!", e, -1);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getDataObjectsSinceId(DefaultData template) {
		try {
			return globalDataAccessService.getDataObjectsSinceId(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the data objects since id from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		try {
			return globalDataAccessService.getDataObjectsSinceIdIgnoreMethodId(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the data objects since id (ignore method id) from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

}
