package info.novatec.novaspy.cmr.service;

import info.novatec.novaspy.communication.data.TimerData;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface ICombinedMetricsDataAccessService {

	List getCombinedMetrics(TimerData template, String workflowName, String activityName);

	List getCombinedMetricsIgnoreMethodId(TimerData template, String workflowName, String activityName);

	List getCombinedMetricsFromToDate(TimerData template, Date fromDate, Date toDate, String workflowName, String activityName);

	List getWorkflows(TimerData template);

	List getActivities(TimerData template, String workflow);

}
