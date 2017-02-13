package rocks.inspectit.server.anomaly.forecast;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Marius Oehler
 *
 */
@Component
public class ForecastFactory {

	@Autowired
	BeanFactory factory;

	public HoltWintersForecast createHoltWinters() {
		HoltWintersForecast forecast = factory.getBean(HoltWintersForecast.class);
		forecast.setSeasonalLength(96);
		return forecast;
	}

	/**
	 * Tries to find the best parameter for the HoltWinter algorithms to fit the given data. This
	 * approach is a work around. A better solution would be to use an algorithm like LBFGS.
	 *
	 * @param factory
	 *            instance of {@link ForecastFactory}
	 * @param data
	 *            the data
	 * @return array containing the minimal RSME, smoothing factor, trend smoothing factor and
	 *         seasonal smoothing factor
	 */
	public double[] bruteForceParameterDetermination(double[] data) {
		double stepSize = 0.05D;

		double minRMSE = Double.MAX_VALUE;
		double alpha = 0;
		double beta = 0;
		double gamma = 0;

		for (double x = 0; x <= 1; x += stepSize) {
			for (double y = 0; y <= 1; y += stepSize) {
				for (double z = 0; z <= 1; z += stepSize) {
					HoltWintersForecast holtWinters = createHoltWinters();
					holtWinters.setSmoothingFactor(x);
					holtWinters.setTrendSmoothingFactor(y);
					holtWinters.setSeasonalSmoothingFactor(z);

					holtWinters.train(data);

					if (holtWinters.getRootMeanSquaredError() < minRMSE) {
						minRMSE = holtWinters.getRootMeanSquaredError();

						alpha = x;
						beta = y;
						gamma = z;
					}
				}
			}
		}

		return new double[] { minRMSE, alpha, beta, gamma };
	}
}
