/**
 *
 */
package rocks.inspectit.server.anomaly.forecast;

import rocks.inspectit.server.anomaly.forecast.impl.DoubleExponentialSmoothing;
import rocks.inspectit.server.anomaly.forecast.impl.HoltWintersForecast;

/**
 * @author Marius Oehler
 *
 */
public abstract class ForecastFactory {

	public abstract HoltWintersForecast createHoltWinters();

	public abstract DoubleExponentialSmoothing createDoubleExponentialSmoothing();

}
