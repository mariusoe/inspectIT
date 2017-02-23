package rocks.inspectit.server.anomaly.baseline.impl;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.processing.ProcessingContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.MovingAverageBaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class MovingAverageBaseline extends AbstractBaseline<MovingAverageBaselineDefinition> {

	private DescriptiveStatistics statistics = new DescriptiveStatistics();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
		statistics.setWindowSize(getDefinition().getWindowSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingContext context, long time) {
		double value = context.getMetricProvider().getIntervalValue();

		if (!Double.isNaN(value)) {
			statistics.addValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		return statistics.getMean();
	}
}
