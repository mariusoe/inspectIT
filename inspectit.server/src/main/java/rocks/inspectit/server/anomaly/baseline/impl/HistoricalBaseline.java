package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
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

	// private double currentValue = Double.NaN;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingContext context, long time) {
		double value = getValue(context, time);

		if (Double.isNaN(value)) {
			nextIndex();
			return;
		}

		// if (Double.isNaN(currentValue)) {
		// currentValue = value;
		// } else {
		// currentValue = (getDefinition().getSmoothingFactor() * value) + ((1 -
		// getDefinition().getSmoothingFactor()) * currentValue);
		// }

		if (Double.isNaN(valueStore[currentIndex])) {
			valueStore[currentIndex] = value;
		} else {
			valueStore[currentIndex] = (getDefinition().getSmoothingFactor() * value) + ((1 - getDefinition().getSmoothingFactor()) * valueStore[currentIndex]);
		}

		nextIndex();
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
