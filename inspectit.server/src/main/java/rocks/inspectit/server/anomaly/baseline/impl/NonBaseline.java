package rocks.inspectit.server.anomaly.baseline.impl;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.baseline.AbstractBaseline;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.NonBaselineDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
public class NonBaseline extends AbstractBaseline<NonBaselineDefinition> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingUnitContext context, long time) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaseline() {
		return Double.NaN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
		// nothing to do here
	}
}
