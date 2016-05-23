/**
 *
 */
package rocks.inspectit.server.anomaly.utils;

import java.util.List;

import rocks.inspectit.server.anomaly.utils.processor.DoubleData;

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

	public static double mean(double[] data) {
		return mean(data, data.length);
	}

	public static double mean(double[] data, int elements) {
		if (elements <= 0 || elements > data.length) {
			return Double.NaN;
		}

		double mean = 0D;

		for (int i = 0; i < elements; i++) {
			mean += data[i];
		}

		return mean / elements;
	}

}
