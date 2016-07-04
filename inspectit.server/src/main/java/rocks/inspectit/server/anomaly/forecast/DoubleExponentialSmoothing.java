/**
 *
 */
package rocks.inspectit.server.anomaly.forecast;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class DoubleExponentialSmoothing {

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	/**
	 * The trend smoothing factor.
	 */
	@Value("${anomaly.settings.forecast.smoothingFactor}")
	private double smoothingFactor;

	/**
	 * The trend smoothing factor.
	 */
	@Value("${anomaly.settings.forecast.trendSmoothingFactor}")
	private double trendSmoothingFactor;

	/**
	 * The current value.
	 */
	private double value = Double.NaN;

	/**
	 * The current trend value.
	 */
	private double trend = Double.NaN;

	public void push(double newValue) {
		if (Double.isNaN(value)) {
			value = newValue;
		} else if (Double.isNaN(trend)) {
			trend = newValue - value;
			value = newValue;
		} else {
			double nextValue = smoothingFactor * newValue + (1 - smoothingFactor) * (value + trend);
			trend = trendSmoothingFactor * (nextValue - value) + (1 - trendSmoothingFactor) * trend;

			value = nextValue;
		}
	}

	public double forecast() {
		if (Double.isNaN(trend)) {
			return value;
		} else {
			return value + trend;
		}
	}

}
