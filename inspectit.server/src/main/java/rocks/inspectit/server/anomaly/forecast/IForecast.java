/**
 *
 */
package rocks.inspectit.server.anomaly.forecast;

/**
 * @author Marius Oehler
 *
 */
public interface IForecast {

	void fit(double value);

	double forecast();

}
