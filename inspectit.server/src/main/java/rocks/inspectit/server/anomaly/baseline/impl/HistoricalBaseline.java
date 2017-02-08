package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.HealthStatus;
import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HistoricalBaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class HistoricalBaseline extends AbstractBaseline<HistoricalBaselineDefinition> {

	private double[] valueStore;

	private int currentIndex = 0;

	private double currentValue = Double.NaN;

	private double trendValue = Double.NaN;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingUnitContext context, long time) {
		double value = getValue(context);

		if (Double.isNaN(value)) {
			nextIndex();
			return;
		}

		if (Double.isNaN(valueStore[currentIndex])) {
			valueStore[currentIndex] = value;
		} else {
			double smoothingFactor = getDefinition().getSmoothingFactor();
			if (context.getGroupContext().getHealthStatus() != HealthStatus.NORMAL) {
				smoothingFactor /= context.getGroupContext().getGroupConfiguration().getFaultSuppressionFactor();
			}
			valueStore[currentIndex] = (smoothingFactor * value) + ((1 - smoothingFactor) * valueStore[currentIndex]);
		}

		nextIndex();
	}

	private double getValue(ProcessingUnitContext context) {
		double value = context.getMetricProvider().getIntervalValue();
		if (isFirstSeasons(context)) {
			if ((context.getGroupContext().getHealthStatus() == HealthStatus.WARNING) || (context.getGroupContext().getHealthStatus() == HealthStatus.CRITICAL)) {
				return currentValue;
			}
			if (Double.isNaN(currentValue)) {
				currentValue = value;
			} else {
				double smoothingFactor = 0.1D;
				if (context.getGroupContext().getHealthStatus() != HealthStatus.NORMAL) {
					smoothingFactor /= context.getGroupContext().getGroupConfiguration().getFaultSuppressionFactor();
				}
				currentValue = (smoothingFactor * value) + ((1 - smoothingFactor) * currentValue);
			}
			return currentValue;
		} else if (getDefinition().isSmoothValue()) {
			if (Double.isNaN(currentValue)) {
				currentValue = value;
			} else if (Double.isNaN(trendValue)) {
				trendValue = value - currentValue;
				currentValue = value;
			} else {
				double nextValue;
				nextValue = (getDefinition().getValueSmoothingFactor() * value) + ((1 - getDefinition().getValueSmoothingFactor()) * (currentValue + trendValue));
				trendValue = (getDefinition().getTrendSmoothingFactor() * (nextValue - currentValue)) + ((1 - getDefinition().getTrendSmoothingFactor()) * trendValue);
				currentValue = nextValue;
			}
			return currentValue;
		} else {
			return value;
		}
	}

	private boolean isFirstSeasons(ProcessingUnitContext context) {
		return context.getIterationCounter() < (getDefinition().getSeasonLength() * context.getConfiguration().getIntervalLongProcessingMultiplier());
	}

	private void nextIndex() {
		currentIndex = (currentIndex + 1) % getDefinition().getSeasonLength();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		double baseline = valueStore[currentIndex];

		// return previous baseline if one exists
		if (Double.isNaN(baseline)) {
			int targetIndex = (currentIndex - 1) % getDefinition().getSeasonLength();
			if (targetIndex < 0) {
				targetIndex += getDefinition().getSeasonLength();
			}
			baseline = valueStore[targetIndex];
		}

		return baseline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
		valueStore = new double[getDefinition().getSeasonLength()];
		Arrays.fill(valueStore, Double.NaN);
	}

}
