package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

public class HttpTimerDataAccessService implements IHttpTimerDataAccessService {

	/**
	 * The timer data access service name.
	 */
	private static final String HTTP_TIMER_DATA_ACCESS_SERVICE = "HttpTimerDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the timer data access service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The timer data access service exposed by the CMR and initialized by Spring.
	 */
	private final IHttpTimerDataAccessService httptimerDataAccessService;

	public HttpTimerDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IHttpTimerDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + HTTP_TIMER_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		httptimerDataAccessService = (IHttpTimerDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	@Override
	public List getAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod) {
		try {
			return httptimerDataAccessService.getAggregatedTimerData(timerData, includeRequestMethod);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the aggregated method http timer data from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public List getAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		try {
			return httptimerDataAccessService.getAggregatedTimerData(timerData, includeRequestMethod, fromDate, toDate);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the aggregated method http timer data from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public List getTaggedAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod) {
		try {
			return httptimerDataAccessService.getTaggedAggregatedTimerData(timerData, includeRequestMethod);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the tagged aggregated method http timer data from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public List getTaggedAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		try {
			return httptimerDataAccessService.getTaggedAggregatedTimerData(timerData, includeRequestMethod, fromDate, toDate);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the tagged aggregated method http timer data from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

}
