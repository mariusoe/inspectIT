package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.TimerData;

import java.util.List;

/**
 * Service for providing general timer data objects.
 * 
 * @author Ivan Senic
 * 
 */
public interface ITimerDataAccessService {

	/**
	 * Returns a list of the timer data for a given template. In this template, only the platform id
	 * is extracted.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @return The list of the timer data object.
	 */
	List getAggregatedTimerData(TimerData timerData);
}
