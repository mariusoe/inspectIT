package info.novatec.inspectit.rcp.editor.preferences.control.samplingrate;

import info.novatec.inspectit.communication.DefaultData;

import java.util.Date;
import java.util.List;

/**
 * The interface for the sampling rate modes.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface ISamplingRateMode {

	/**
	 * Adjusts the sampling rate with the given sampling rate mode and returns a {@link List} with
	 * the aggregated {@link DefaultData} objects.
	 * 
	 * @param defaultData
	 *            The {@link List} with {@link DefaultData} objects.
	 * @param from
	 *            The start time.
	 * @param to
	 *            The end time.
	 * @param samplingRate
	 *            The sampling rate.
	 * @return A {@link List} with the aggregated {@link DefaultData}.
	 */
	List<? extends DefaultData> adjustSamplingRate(List<? extends DefaultData> defaultData, Date from, Date to, int samplingRate);
}
