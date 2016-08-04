/**
 *
 */
package rocks.inspectit.server.anomaly._a;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.forecast.impl.DoubleExponentialSmoothing;

/**
 * @author Marius Oehler
 *
 */
public class Test2 {

	public static DoubleExponentialSmoothing getForecast(double x, double y) {
		DoubleExponentialSmoothing forecast = new DoubleExponentialSmoothing();

		try {
			Logger logger = LoggerFactory.getLogger(forecast.getClass());

			Field field = forecast.getClass().getDeclaredField("log");
			field.setAccessible(true);
			field.set(forecast, logger);

			Field field2 = forecast.getClass().getDeclaredField("smoothingFactor");
			field2.setAccessible(true);
			field2.set(forecast, x);

			Field field3 = forecast.getClass().getDeclaredField("trendSmoothingFactor");
			field3.setAccessible(true);
			field3.set(forecast, y);

			return forecast;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}

	static double[] data = new double[] { 350, 50.71786844, 64.55179666, 51.1322097, 51.79894369, 50.27948995, 62.01967087, 63.70620311, 55.80371043, 59.93387877, 70.992365, 66.02395786, 71.57881965,
			250, 66.01241032, 52.51625478, 51.27656992, 70.5524446, 61.03459655, 74.9428594, 53.66069551, 55.75940034, 70.19163811, 65.72633095, 61.03428535, 150, 52.82719036, 70.43756029,
			72.63839445, 50.48953896, 53.06450685, 65.67399593, 60.47463683, 72.88880616, 70.80020936, 73.12805698, 57.14765826, 61.00105227, 67.55442167, 56.11194901, 54.59013692, 69.10798046,
			72.40510827, 64.4879031, 58.68834855, 55.44921433, 72.69797375, 51.07322781, 73.56915662, 67.42363198 };

	public static void main(String[] args) {
		test();
	}

	static double[] getFilteredData() {
		DescriptiveStatistics statistics = new DescriptiveStatistics(data);
		double mean = statistics.getMean();
		double stddev = statistics.getStandardDeviation();

		double[] clone = ArrayUtils.clone(data);
		Arrays.sort(clone);

		double median = new Median().evaluate(data);

		ArrayList<Double> list = new ArrayList<>();
		for (double val : data) {
			if (Math.abs(val - mean) < 3 * stddev) {
				list.add(val);
			} else {
				list.add(median);
			}
		}

		return ArrayUtils.toPrimitive(list.toArray(new Double[0]));
	}

	/**
	 *
	 */
	private static void test() {
		DoubleExponentialSmoothing forecast = getForecast(0.1D, 0.1D);

		for (double val : getFilteredData()) {
			forecast.fit(val);
			double forecastValue = forecast.forecast();
			System.out.println(String.format("%f", forecastValue));
		}
	}

}
