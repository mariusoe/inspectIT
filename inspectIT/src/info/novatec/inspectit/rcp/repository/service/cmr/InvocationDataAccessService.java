package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.InspectIT;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

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

	private XStream xstream = new XStream(new JettisonMappedXmlDriver());

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
	public Object getInvocationSequenceDetail(InvocationSequenceData template) {
		try {
			Object object = invocationDataAccessService.getInvocationSequenceDetail(template);
			
			if (null == object) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Invocation sequence details", "Requested invocation sequence data is not in the buffer any more.");
					}
				});
				return null;
			}

			if (object instanceof InvocationSequenceData) {
				// directly return the object
				return (InvocationSequenceData) object;
			} else {
				InputStream stream = getStreamForInvocationSequence(template);
				InputStream input = null;

				try {
					input = new GZIPInputStream(stream);

					InvocationSequenceData invocation = (InvocationSequenceData) xstream.fromXML(input);
					return invocation;
				} catch (IOException e) {
					InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence detail from the CMR!", e, -1);
					return null;
				} finally {
					try {
						if (null != input) {
							input.close();
						}
						if (null != stream) {
							stream.close();
						}
					} catch (IOException e) {
						// we do not care about that one
					}
				}
			}
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the invocation sequence detail from the CMR!", e, -1);
			return null;
		}
	}

	/**
	 * Returns a stream for an invocation sequence object which can be persisted
	 * later for example.
	 * 
	 * @param template
	 *            The template to search for on the server.
	 * @return the stream.
	 */
	public InputStream getStreamForInvocationSequence(InvocationSequenceData template) {
		Object object = invocationDataAccessService.getInvocationSequenceDetail(template);

		if (object instanceof InputStream) {
			// read from the remote stream
			InputStream stream = (InputStream) object;

			return new BufferedInputStream(stream);
		} else {
			InspectIT.getDefault().createErrorDialog("Unexpected object retrieved, expected RemoteInputStream but was: " + object.getClass(), null, -1);
			throw new RuntimeException("Unexpected object retrieved, expected RemoteInputStream but was: " + object.getClass());
		}

	}

}
