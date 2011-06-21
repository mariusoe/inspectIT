package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface ICombinedMetricsDataAccessService {

	List<TimerData> getCombinedMetrics(TimerData template, String workflowName, String activityName);

	List<TimerData> getCombinedMetricsIgnoreMethodId(TimerData template, String workflowName, String activityName);

	List<TimerData> getCombinedMetricsFromToDate(TimerData template, Date fromDate, Date toDate, String workflowName, String activityName);

	List<ParameterContentData> getWorkflows(TimerData template);

	List<ParameterContentData> getActivities(TimerData template, String workflow);

}
