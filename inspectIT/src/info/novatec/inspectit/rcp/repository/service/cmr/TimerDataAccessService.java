package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * Implementation of the timer data access service for the UI.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataAccessService implements ITimerDataAccessService {

	/**
	 * The timer data access service name.
	 */
	private static final String TIMER_DATA_ACCESS_SERVICE = "TimerDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the timer data access service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The timer data access service exposed by the CMR and initialized by Spring.
	 */
	private final ITimerDataAccessService timerDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public TimerDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(ITimerDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + TIMER_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		timerDataAccessService = (ITimerDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public List getAggregatedTimerData(TimerData timerData) {
		try {
			return timerDataAccessService.getAggregatedTimerData(timerData);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the aggregated method timer data from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public List getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate) {
		try {
			return timerDataAccessService.getAggregatedTimerData(timerData, fromDate, toDate);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the aggregated method timer data from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

}
