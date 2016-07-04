/**
 *
 */
package rocks.inspectit.server.anomaly.forecast;

/**
 * @author Marius Oehler
 *
 */
public abstract class ForecastFactory {

	public abstract HoltWintersForecast createHoltWinters();

	public abstract DoubleExponentialSmoothing createDoubleExponentialSmoothing();

}
