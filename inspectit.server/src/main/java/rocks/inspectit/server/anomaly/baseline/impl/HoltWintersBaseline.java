package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HoltWintersBaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class HoltWintersBaseline extends AbstractBaseline<HoltWintersBaselineDefinition> {

	List<Double> values = new ArrayList<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingContext context, long time) {
		double value = context.getMetricProvider().getIntervalValue();

		if (Double.isNaN(value)) {
			return;
		}

		if (!trained && (values.size() < (getDefinition().getSeasonalLength() * 2))) {
			values.add(value);
			return;
		} else if (!trained) {
			train(values);
			return;
		}

		fit(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		return forecastedValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}

	/**
	 * The logger.
	 */
	@Log
	private Logger log;

	/**
	 * The current seasonal index.
	 */
	private int seasonalIndex;

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
	 * See: http://robjhyndman.com/researchtips/hw-initialization/ 1st period's average can be
	 * taken. But data[0] works better.
	 **
	 * @param data
	 *            the input data
	 * @return initial level value i.e. St[1]
	 */
	private double calculateInitialLevel(List<Double> data) {
		/**
		 * double sum = 0; for (int i = 0; i < seasonLength; i++) { sum += y[i]; }
		 *
		 * return sum / seasonLength;
		 **/
		return data.get(0);
	}

	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm for more information.
	 **
	 * @param data
	 *            the input data
	 * @return initial trend - Bt[1]
	 */
	private double calculateInitialTrend(List<Double> data) {
		double sum = 0;

		for (int i = 0; i < getDefinition().getSeasonalLength(); i++) {
			sum += (data.get(getDefinition().getSeasonalLength() + i) - data.get(i));
		}

		return sum / (getDefinition().getSeasonalLength() * getDefinition().getSeasonalLength());
	}

	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm for more information.
	 *
	 * @param data
	 *            the input data
	 * @return seasonal indices
	 */
	private double[] calculateSeasonalIndices(List<Double> data) {
		int seasons = data.size() / getDefinition().getSeasonalLength();

		double[] seasonalAverage = new double[seasons];
		double[] seasonalIndices = new double[getDefinition().getSeasonalLength()];

		double[] averagedObservations = new double[data.size()];

		for (int i = 0; i < seasons; i++) {
			for (int j = 0; j < getDefinition().getSeasonalLength(); j++) {
				seasonalAverage[i] += data.get((i * getDefinition().getSeasonalLength()) + j);
			}
			seasonalAverage[i] /= getDefinition().getSeasonalLength();
		}

		for (int i = 0; i < seasons; i++) {
			for (int j = 0; j < getDefinition().getSeasonalLength(); j++) {
				averagedObservations[(i * getDefinition().getSeasonalLength()) + j] = data.get((i * getDefinition().getSeasonalLength()) + j) / seasonalAverage[i];
			}
		}

		for (int i = 0; i < getDefinition().getSeasonalLength(); i++) {
			for (int j = 0; j < seasons; j++) {
				seasonalIndices[i] += averagedObservations[(j * getDefinition().getSeasonalLength()) + i];
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
			return;
		}
		final int m = 1;

		double previousSmoothedValue = smoothedValue;

		// Calculate overall smoothing
		if (trainedFirstStage) {
			smoothedValue = ((getDefinition().getSmoothingFactor() * newValue) / seasonalIndices[seasonalIndex])
					+ ((1.0 - getDefinition().getSmoothingFactor()) * (previousSmoothedValue + trendValue));
		} else {
			smoothedValue = (getDefinition().getSmoothingFactor() * newValue) + ((1.0 - getDefinition().getSmoothingFactor()) * (previousSmoothedValue + trendValue));
		}

		// Calculate trend smoothing
		trendValue = (getDefinition().getTrendSmoothingFactor() * (smoothedValue - previousSmoothedValue)) + ((1 - getDefinition().getTrendSmoothingFactor()) * trendValue);

		// Calculate seasonal smoothing
		if (trainedFirstStage) {
			seasonalIndices[seasonalIndex] = ((getDefinition().getSeasonalSmoothingFactor() * newValue) / smoothedValue)
					+ ((1.0 - getDefinition().getSeasonalSmoothingFactor()) * seasonalIndices[seasonalIndex]);
		}

		if (log.isDebugEnabled()) {
			log.debug("New value is {} and its forecast has been {}", newValue, forecastedValue);
		}

		// Calculate forecast
		if (trainedSecondStage) {
			int nextSeasonalIndex = (seasonalIndex + 1) % getDefinition().getSeasonalLength();
			forecastedValue = (smoothedValue + (m * trendValue)) * seasonalIndices[nextSeasonalIndex];
		}

		seasonalIndex = (seasonalIndex + 1) % getDefinition().getSeasonalLength();
	}

	private void train(List<Double> inputData) {
		if ((inputData == null) || (inputData.size() < (2 * getDefinition().getSeasonalLength()))) {
			throw new IllegalArgumentException(String.format("Training data has to contain at least %d data elements (2 times the season length).", 2 * getDefinition().getSeasonalLength()));
		}

		// Initialize base values
		smoothedValue = calculateInitialLevel(inputData);
		trendValue = calculateInitialTrend(inputData);
		seasonalIndices = calculateSeasonalIndices(inputData);

		// this is necessary, because we have a trend after two elements
		seasonalIndex = 2;

		// Start calculations and skip first two elements
		for (int i = 2; i < inputData.size(); i++) {
			// forecasting is enabled, now
			if ((i - getDefinition().getSeasonalLength()) >= 0) {
				trainedFirstStage = true;
			}

			// forecasting is enabled, now
			if ((i + 1) > getDefinition().getSeasonalLength()) {
				trainedSecondStage = true;
			}

			fit(inputData.get(i), true);
		}

		trained = true;
	}
}
