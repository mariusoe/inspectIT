package info.novatec.novaspy.rcp.repository.service;

import info.novatec.novaspy.cmr.service.IInvocationDataAccessService;
import info.novatec.novaspy.communication.data.InvocationSequenceData;
import info.novatec.novaspy.rcp.NovaSpy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
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
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the invocation sequence overview from the CMR!", e, -1);
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
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the invocation sequence overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getInvocationSequenceDetail(InvocationSequenceData template) {
		try {
			return invocationDataAccessService.getInvocationSequenceDetail(template);
		} catch (Exception e) {
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the invocation sequence detail from the CMR!", e, -1);
			return null;
		}
	}

	public InvocationSequenceData getInvocationSequence(InvocationSequenceData template) {
		Object object = this.getInvocationSequenceDetail(template);
		if (object instanceof InvocationSequenceData) {
			// directly return the object
			return (InvocationSequenceData) object;
		} else if (object instanceof RemoteInputStream) {
			// read from the remote stream
			RemoteInputStream stream = (RemoteInputStream) object;

			InputStream istream = null;
			InputStream bis = null;
			InputStream input = null;
			try {
				istream = RemoteInputStreamClient.wrap(stream);
				bis = new BufferedInputStream(istream);
				input = new GZIPInputStream(bis);

				InvocationSequenceData invocation = (InvocationSequenceData) xstream.fromXML(input);
				return invocation;
			} catch (IOException e) {
				NovaSpy.getDefault().createErrorDialog("There was an error retrieving the invocation sequence detail from the CMR!", e, -1);
				return null;
			} finally {
				try {
					if (null != istream) {
						istream.close();
					}
					if (null != bis) {
						bis.close();
					}
					if (null != input) {
						input.close();
					}
				} catch (IOException e) {
					// we do not care about that one
				}
			}
		} else {
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the invocation sequence detail from the CMR!", null, -1);
			return null;
		}
	}

}
