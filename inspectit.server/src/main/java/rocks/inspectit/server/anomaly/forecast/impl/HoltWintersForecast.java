/**
 *
 */
package rocks.inspectit.server.anomaly.forecast.impl;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.forecast.ForecastFactory;
import rocks.inspectit.server.anomaly.forecast.IForecast;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Forecasting using the Holt-Winter algorithm. This implementation is based on an implementation by
 * Nishant Chandra [https://github.com/cmdrkeene/holt_winters/blob/master/ext/holtWinters.java]
 * which was released under the license "Apache License, Version 2.0" in 2011.
 *
 * The calculations are based on the following equations:
 *
 * St[i] (smoothed value) = alpha * y[i] / It[i - period] + (1.0 - alpha) * (St[i - 1] + Bt[i - 1])
 *
 * Bt[i] (trend value) = beta * (St[i] - St[i - 1]) + (1 - beta) * Bt[i - 1]
 *
 * It[i] (seasonal indices) = gamma * y[i] / St[i] + (1.0 - gamma) * It[i - period]
 *
 * Ft[i + m] (forecast) = (St[i] + (m * Bt[i])) * It[i - period + m]
 *
 * @author Marius Oehler
 *
 */
public class HoltWintersForecast implements IForecast {

	/**
	 * This exception will be thrown if an untrained instance of {@link HoltWintersForecast} is
	 * used.
	 *
	 * @author Marius Oehler
	 *
	 */
	class UntrainedHoltWintersException extends RuntimeException {

		private static final long serialVersionUID = -5119819955712036572L;

	}

	/**
	 * The logger.
	 */
	@Log
	private Logger log;

	/**
	 * The trend smoothing factor.
	 */
	private double smoothingFactor;

	/**
	 * The trend smoothing factor.
	 */
	private double trendSmoothingFactor;

	/**
	 * The seasonal smoothing factor.
	 */
	private double seasonalSmoothingFactor;

	/**
	 * The current seasonal index.
	 */
	private int seasonalIndex;

	/**
	 * The season length (number of elements).
	 */
	@Value("#{${anomaly.settings.forecast.seasonalDuration} * 3600 / ${anomaly.settings.confidenceBandUpdateInterval}}")
	private int seasonalLength;

	/**
	 * Marker for first training stage. Enables seasonal indices smoothing.
	 */
	private boolean trainedFirstStage = false;

	/**
	 * Marker for second training stage. Enables forecasting.
	 */
	private boolean trainedSecondStage = false;

	/**
	 * Indicates whether the model has been trained.
	 */
	private boolean trained = false;

	/**
	 * The seasonal indices.
	 */
	private double[] seasonalIndices;

	/**
	 * The smoothed value.
	 */
	private double smoothedValue;

	/**
	 * The trend value.
	 */
	private double trendValue;

	/**
	 * The forecasted value.
	 */
	private double forecastedValue = Double.NaN;

	/**
	 * The mean squared error sum.
	 */
	private double meanSquaredErrorSum = 0D;

	/**
	 * Counter how many error sums have been added.
	 */
	private long meanSquaredErrorCounter = 0L;

	@Value("${anomaly.settings.confidenceBandUpdateInterval}")
	private long updateInterval;

	@Value("${anomaly.settings.forecast.seasonalDuration}")
	private long seasonalDuration;

	/**
	 * Constructor.
	 */
	public HoltWintersForecast() {
	}

	/**
	 * Constructor.
	 *
	 * @param smoothingFactor
	 *            the used value smoothing factor
	 * @param trendSmoothingFactor
	 *            the used trend smoothing factor
	 * @param seasonalSmoothingFactor
	 *            the used seasonal index smoothing factor
	 * @param seasonLength
	 *            the length of a season
	 */
	public HoltWintersForecast(double smoothingFactor, double trendSmoothingFactor, double seasonalSmoothingFactor, int seasonLength) {
		this.smoothingFactor = smoothingFactor;
		this.trendSmoothingFactor = trendSmoothingFactor;
		this.seasonalSmoothingFactor = seasonalSmoothingFactor;
		this.seasonalLength = seasonLength;
	}

	/**
	 * See: http://robjhyndman.com/researchtips/hw-initialization/ 1st period's average can be
	 * taken. But data[0] works better.
	 **
	 * @param data
	 *            the input data
	 * @return initial level value i.e. St[1]
	 */
	private double calculateInitialLevel(double[] data) {
		/**
		 * double sum = 0; for (int i = 0; i < seasonLength; i++) { sum += y[i]; }
		 *
		 * return sum / seasonLength;
		 **/
		return data[0];
	}

	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm for more information.
	 **
	 * @param data
	 *            the input data
	 * @return initial trend - Bt[1]
	 */
	private double calculateInitialTrend(double[] data) {
		double sum = 0;

		for (int i = 0; i < seasonalLength; i++) {
			sum += (data[seasonalLength + i] - data[i]);
		}

		return sum / (seasonalLength * seasonalLength);
	}

	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm for more information.
	 *
	 * @param data
	 *            the input data
	 * @return seasonal indices
	 */
	private double[] calculateSeasonalIndices(double[] data) {
		int seasons = data.length / seasonalLength;

		double[] seasonalAverage = new double[seasons];
		double[] seasonalIndices = new double[seasonalLength];

		double[] averagedObservations = new double[data.length];

		for (int i = 0; i < seasons; i++) {
			for (int j = 0; j < seasonalLength; j++) {
				seasonalAverage[i] += data[(i * seasonalLength) + j];
			}
			seasonalAverage[i] /= seasonalLength;
		}

		for (int i = 0; i < seasons; i++) {
			for (int j = 0; j < seasonalLength; j++) {
				averagedObservations[(i * seasonalLength) + j] = data[(i * seasonalLength) + j] / seasonalAverage[i];
			}
		}

		for (int i = 0; i < seasonalLength; i++) {
			for (int j = 0; j < seasons; j++) {
				seasonalIndices[i] += averagedObservations[(j * seasonalLength) + i];
			}
			seasonalIndices[i] /= seasons;
		}

		return seasonalIndices;
	}

	/**
	 * Adds a new value to the model.
	 *
	 * @param newValue
	 *            the new value
	 * @exception UntrainedHoltWintersException
	 *                is thrown if the model has not been trained
	 */
	@Override
	public void fit(double newValue) {
		fit(newValue, false);
	}

	/**
	 * Adds a new value to the model.
	 *
	 * @param newValue
	 *            the new value
	 * @param force
	 *            adds the value even the model has not been trained
	 */
	private void fit(double newValue, boolean force) {
		if (!trained && !force) {
			throw new UntrainedHoltWintersException();
		}
		final int m = 1;

		if (!Double.isNaN(forecastedValue)) {
			meanSquaredErrorSum += Math.pow(forecastedValue - newValue, 2);
			meanSquaredErrorCounter++;
		}

		double previousSmoothedValue = smoothedValue;

		// Calculate overall smoothing
		if (trainedFirstStage) {
			smoothedValue = smoothingFactor * newValue / seasonalIndices[seasonalIndex] + (1.0 - smoothingFactor) * (previousSmoothedValue + trendValue);
		} else {
			smoothedValue = smoothingFactor * newValue + (1.0 - smoothingFactor) * (previousSmoothedValue + trendValue);
		}

		// Calculate trend smoothing
		trendValue = trendSmoothingFactor * (smoothedValue - previousSmoothedValue) + (1 - trendSmoothingFactor) * trendValue;

		// Calculate seasonal smoothing
		if (trainedFirstStage) {
			seasonalIndices[seasonalIndex] = seasonalSmoothingFactor * newValue / smoothedValue + (1.0 - seasonalSmoothingFactor) * seasonalIndices[seasonalIndex];
		}

		if (log.isDebugEnabled()) {
			log.debug("New value is {} and its forecast has been {}", newValue, forecastedValue);
		}

		// Calculate forecast
		if (trainedSecondStage) {
			int nextSeasonalIndex = (seasonalIndex + 1) % seasonalLength;
			forecastedValue = (smoothedValue + (m * trendValue)) * seasonalIndices[nextSeasonalIndex];
		}

		seasonalIndex = (seasonalIndex + 1) % seasonalLength;
	}

	/**
	 * Returns a forecast for the next value.
	 *
	 * @return the forecasted value
	 */
	@Override
	public double forecast() {
		return forecastedValue;
	}

	/**
	 * Returns the root mean squared error of the model.
	 *
	 * @return the RMSE
	 */
	public double getRootMeanSquaredError() {
		return Math.sqrt(meanSquaredErrorSum / meanSquaredErrorCounter);
	}

	/**
	 * Gets {@link #seasonalIndex}.
	 *
	 * @return {@link #seasonalIndex}
	 */
	public int getSeasonIndex() {
		return seasonalIndex;
	}

	/**
	 * Sets {@link #seasonalSmoothingFactor}.
	 *
	 * @param seasonalSmoothingFactor
	 *            New value for {@link #seasonalSmoothingFactor}
	 */
	public void setSeasonalSmoothingFactor(double seasonalSmoothingFactor) {
		this.seasonalSmoothingFactor = seasonalSmoothingFactor;
	}

	/**
	 * Sets {@link #smoothingFactor}.
	 *
	 * @param smoothingFactor
	 *            New value for {@link #smoothingFactor}
	 */
	public void setSmoothingFactor(double smoothingFactor) {
		this.smoothingFactor = smoothingFactor;
	}

	/**
	 * Sets {@link #trendSmoothingFactor}.
	 *
	 * @param trendSmoothingFactor
	 *            New value for {@link #trendSmoothingFactor}
	 */
	public void setTrendSmoothingFactor(double trendSmoothingFactor) {
		this.trendSmoothingFactor = trendSmoothingFactor;
	}

	/**
	 * Trains the model with the given input data.
	 *
	 * @param inputData
	 *            the input data
	 */
	public void train(double[] inputData) {
		if (inputData == null || inputData.length < 2 * seasonalLength) {
			throw new IllegalArgumentException(String.format("Training data has to contain at least %d data elements (2 times the season length).", 2 * seasonalLength));
		}

		// Initialize base values
		smoothedValue = calculateInitialLevel(inputData);
		trendValue = calculateInitialTrend(inputData);
		seasonalIndices = calculateSeasonalIndices(inputData);

		// this is necessary, because we have a trend after two elements
		seasonalIndex = 2;

		// Start calculations and skip first two elements
		for (int i = 2; i < inputData.length; i++) {
			// forecasting is enabled, now
			if (i - seasonalLength >= 0) {
				trainedFirstStage = true;
			}

			// forecasting is enabled, now
			if (i + 1 > seasonalLength) {
				trainedSecondStage = true;
			}

			fit(inputData[i], true);
		}

		trained = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("HoltWintersForecast [valueSmoothing=%f, trendSmoothing=%f, seasonalSmoothing=%f, seasonalLength=%d, currentRSME=%f]", smoothingFactor, trendSmoothingFactor,
				seasonalSmoothingFactor, seasonalLength, getRootMeanSquaredError());
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
	public static double[] bruteForceParameterDetermination(ForecastFactory factory, double[] data) {
		double stepSize = 0.05D;

		double minRMSE = Double.MAX_VALUE;
		double alpha = 0;
		double beta = 0;
		double gamma = 0;

		for (double x = 0; x <= 1; x += stepSize) {
			for (double y = 0; y <= 1; y += stepSize) {
				for (double z = 0; z <= 1; z += stepSize) {
					HoltWintersForecast holtWinters = factory.createHoltWinters();
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
