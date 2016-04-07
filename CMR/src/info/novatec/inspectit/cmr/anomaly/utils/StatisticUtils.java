/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.utils;

import info.novatec.inspectit.cmr.anomaly.utils.processor.DoubleData;

import java.util.List;

/**
 * Collection of useful methods for calculations of statistics.
 *
 * @author Marius Oehler
 *
 */
public final class StatisticUtils {

	/**
	 *
	 */
	private StatisticUtils() {
	}

	/**
	 * Calculates the mean of the values of the given list.
	 *
	 * @param dataList
	 *            list containing the values
	 * @return the mean
	 */
	public static double mean(List<DoubleData> dataList) {
		if (dataList.isEmpty()) {
			return 0;
		}

		double sum = 0;
		for (DoubleData data : dataList) {
			sum += data.getData();
		}

		return sum / dataList.size();
	}
}