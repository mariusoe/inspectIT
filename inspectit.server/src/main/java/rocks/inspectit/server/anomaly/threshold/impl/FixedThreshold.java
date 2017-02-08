package rocks.inspectit.server.anomaly.threshold.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.processing.ProcessingUnitContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.UnsupportedThresholdTypeException;
import rocks.inspectit.shared.cs.ci.anomaly.definition.threshold.FixedThresholdDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class FixedThreshold extends AbstractThreshold<FixedThresholdDefinition> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ProcessingUnitContext context, long time) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean providesThreshold(ThresholdType type) {
		switch (type) {
		case UPPER_CRITICAL:
			return !Double.isNaN(getDefinition().getUpperCriticalThreshold());
		case UPPER_WARNING:
			return !Double.isNaN(getDefinition().getUpperWarningThreshold());
		case LOWER_WARNING:
			return !Double.isNaN(getDefinition().getLowerWarningThreshold());
		case LOWER_CRITICAL:
			return !Double.isNaN(getDefinition().getLowerCriticalThreshold());
		default:
			throw new UnsupportedThresholdTypeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getThreshold(ProcessingUnitContext context, ThresholdType type) {
		switch (type) {
		case UPPER_CRITICAL:
			return getDefinition().getUpperCriticalThreshold();
		case UPPER_WARNING:
			return getDefinition().getUpperWarningThreshold();
		case LOWER_WARNING:
			return getDefinition().getLowerWarningThreshold();
		case LOWER_CRITICAL:
			return getDefinition().getLowerCriticalThreshold();
		default:
			throw new UnsupportedThresholdTypeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}

}
