package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.HttpTimerData;

import java.util.Date;
import java.util.List;

/**
 * Service to access the HttpTimerData.
 * 
 * @author Stefan Siegl
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IHttpTimerDataAccessService {

	/**
	 * Returns a list of the http timer data for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 * @return The list of the timer data object.
	 */
	List<HttpTimerData> getAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod);

	/**
	 * Returns a list of the http timer data for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the timer data object.
	 */
	List<HttpTimerData> getAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod, Date fromDate, Date toDate);

	/**
	 * Returns a list of http timer data that is aggregated the value of the given http request
	 * parameter. For this purpose the <code>uri</code> field of the http timer data is re-used to
	 * store this value.
	 * 
	 * @param timerData
	 *            the template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 * 
	 * @return The list of the timer data objects that are aggregated by the tagged value.
	 */
	List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod);

	/**
	 * Returns a list of http timer data that is aggregated the value of the given http request
	 * parameter. For this purpose the <code>uri</code> field of the http timer data is re-used to
	 * store this value.
	 * 
	 * @param timerData
	 *            the template containing the platform id.
	 * @param includeRequestMethod
	 *            whether or not the request method should be include in the categorization.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * 
	 * @return The list of the timer data objects that are aggregated by the tagged value.
	 */
	List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData timerData, boolean includeRequestMethod, Date fromDate, Date toDate);

}
