package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.TimerData;

import java.util.List;

/**
 * The DAO for timer data objects.
 * 
 * @author Ivan Senic
 * 
 */
public interface TimerDataDao {

	/**
	 * Returns a list of the aggregated timer data for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @return The list of the aggregated timer data object.
	 */
	List<TimerData> getAggregatedTimerData(TimerData timerData);
}
