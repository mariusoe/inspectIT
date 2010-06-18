package info.novatec.inspectit.rcp.repository.service.cmr;

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
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		try {
			return exceptionDataAccessService.getExceptionTree(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the exception tree from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit) {
		try {
			return exceptionDataAccessService.getUngroupedExceptionOverview(template, limit);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the ungrouped exception overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template) {
		try {
			return exceptionDataAccessService.getUngroupedExceptionOverview(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the ungrouped exception overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		try {
			return exceptionDataAccessService.getDataForGroupedExceptionOverview(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the data for the grouped exception overview from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getStackTracesForErrorMessage(ExceptionSensorData template) {
		try {
			return exceptionDataAccessService.getStackTracesForErrorMessage(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the stack traces from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

}
