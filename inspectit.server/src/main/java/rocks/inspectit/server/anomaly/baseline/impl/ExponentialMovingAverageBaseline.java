package rocks.inspectit.server.anomaly.baseline.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.ExponentialMovingAverageBaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class ExponentialMovingAverageBaseline extends AbstractBaseline<ExponentialMovingAverageBaselineDefinition> {

	private double baseline = Double.NaN;

	private double trend = Double.NaN;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingUnitContext context, long time) {
		double value = context.getMetricProvider().getIntervalValue();

		if (Double.isNaN(value)) {
			return;
		}

		if (Double.isNaN(baseline)) {
			baseline = value;
		} else if (Double.isNaN(trend)) {
			trend = value - baseline;
			baseline = value;
		} else {
			double nextValue;
			if (context.isWarmedUp()) {
				nextValue = (getDefinition().getSmoothingFactor() * value) + ((1 - getDefinition().getSmoothingFactor()) * (baseline + trend));
				trend = (getDefinition().getTrendSmoothingFactor() * (nextValue - baseline)) + ((1 - getDefinition().getTrendSmoothingFactor()) * trend);
			} else {
				nextValue = (getDefinition().getSmoothingFactor() * value) + ((1 - getDefinition().getSmoothingFactor()) * baseline);
				trend = 0;
			}
			baseline = nextValue;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		return baseline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}

}
