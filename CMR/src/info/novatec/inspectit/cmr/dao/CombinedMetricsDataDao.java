package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.TimerData;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface CombinedMetricsDataDao {

	/**
	 * 
	 * @param template
	 * @return List of {@link TimerData} objects.
	 */
	List<TimerData> getCombinedMetrics(TimerData template);

	/**
	 * 
	 * @param template
	 * @return List of {@link TimerData} objects.
	 */
	List<TimerData> getCombinedMetricsIgnoreMethodId(TimerData template);

	/**
	 * 
	 * @param template
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	List getCombinedMetricsFromToDate(TimerData template, String workflowName, String activityName, Date fromDate, Date toDate);

	/**
	 * 
	 * @param template
	 * @return
	 */
	List getWorkflows(TimerData template);

	/**
	 * 
	 * @param template
	 * @param workflow
	 * @return
	 */
	List getActivities(TimerData template, String workflow);

}
