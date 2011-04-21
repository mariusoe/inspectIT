package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * @author Patrice Bouillet
 * 
 */
public class InvocationDataAccessService implements IInvocationDataAccessService {

	/**
	 * The name of the invocation data access service.
	 */
	private static final String INVOCATION_DATA_ACCESS_SERVICE = "InvocationDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The invocation data access service exposed by the CMR and initialized by
	 * Spring.
	 */
	private final IInvocationDataAccessService invocationDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public InvocationDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IInvocationDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + INVOCATION_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		invocationDataAccessService = (IInvocationDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		try {
			return invocationDataAccessService.getInvocationSequenceOverview(platformId, methodId, limit);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit) {
		try {
			return invocationDataAccessService.getInvocationSequenceOverview(platformId, limit);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate) {
		try {
			return invocationDataAccessService.getInvocationSequenceOverview(platformId, limit, fromDate, toDate);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate) {
		try {
			return invocationDataAccessService.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection invocationIdCollection, int limit) {
		try {
			return invocationDataAccessService.getInvocationSequenceOverview(platformId, invocationIdCollection, limit);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		try {
			InvocationSequenceData object = invocationDataAccessService.getInvocationSequenceDetail(template);
			if (null == object) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Invocation sequence details", "Requested invocation sequence data is not in the buffer any more.");
					}
				});
				return null;
			}
			return object;
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence detail from the CMR!", e, -1);
			return null;
		}
	}

}
