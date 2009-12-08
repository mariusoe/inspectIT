package info.novatec.novaspy.rcp.repository.service;

import info.novatec.novaspy.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.novaspy.communication.data.ParameterContentData;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.rcp.NovaSpy;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CombinedMetricsDataAccessService implements ICombinedMetricsDataAccessService {
	/**
	 * The combined metrics data access service name.
	 */
	private static final String COMBINED_METRICS_DATA_ACCESS_SERVICE = "CombinedMetricsDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The combined metrics data access service exposed by the CMR and
	 * initialized by Spring.
	 */
	private final ICombinedMetricsDataAccessService combinedMetricsDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public CombinedMetricsDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(ICombinedMetricsDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + COMBINED_METRICS_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		combinedMetricsDataAccessService = (ICombinedMetricsDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<TimerData> getCombinedMetrics(TimerData template, String workflowName, String activityName) {
		try {
			return combinedMetricsDataAccessService.getCombinedMetrics(template, workflowName, activityName);
		} catch (Exception e) {
			// TODO ET: remove stack trace printing
			e.printStackTrace();
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the combined metrics from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	@SuppressWarnings("unchecked")
	public List<TimerData> getCombinedMetricsIgnoreMethodId(TimerData template, String workflowName, String activityName) {
		try {
			return combinedMetricsDataAccessService.getCombinedMetricsIgnoreMethodId(template, workflowName, activityName);
		} catch (Exception e) {
			// TODO ET: remove stack trace printing
			e.printStackTrace();
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the combined metrics from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<TimerData> getCombinedMetricsFromToDate(TimerData template, Date fromDate, Date toDate, String workflowName, String activityName) {
		try {
			return combinedMetricsDataAccessService.getCombinedMetricsFromToDate(template, fromDate, toDate, workflowName, activityName);
		} catch (Exception e) {
			// TODO ET: remove stack trace printing
			e.printStackTrace();
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the combined metrics from to date from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ParameterContentData> getWorkflows(TimerData template) {
		try {
			return combinedMetricsDataAccessService.getWorkflows(template);
		} catch (Exception e) {
			// TODO ET: remove stack trace printing
			e.printStackTrace();
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the workflows from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ParameterContentData> getActivities(TimerData template, String workflow) {
		try {
			return combinedMetricsDataAccessService.getActivities(template, workflow);
		} catch (Exception e) {
			// TODO ET: remove stack trace printing
			e.printStackTrace();
			NovaSpy.getDefault().createErrorDialog("There was an error retrieving the activities from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

}
