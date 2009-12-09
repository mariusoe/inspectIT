package info.novatec.inspectit.rcp.repository.service;

import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Collections;
import java.util.List;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionDataAccessService implements IExceptionDataAccessService {

	/**
	 * The exception data access service name.
	 */
	private static final String EXCEPTION_DATA_ACCESS_SERVICE = "ExceptionDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The exception data access service exposed by the CMR and initialized by
	 * Spring.
	 */
	private final IExceptionDataAccessService exceptionDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public ExceptionDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IExceptionDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + EXCEPTION_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		exceptionDataAccessService = (IExceptionDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getExceptionTreeDetails(ExceptionSensorData template) {
		try {
			return exceptionDataAccessService.getExceptionTreeDetails(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the exception tree details from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template, int limit) {
		try {
			return exceptionDataAccessService.getExceptionTreeOverview(template, limit);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the exception tree overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template) {
		try {
			return exceptionDataAccessService.getExceptionTreeOverview(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the exception tree overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

}
