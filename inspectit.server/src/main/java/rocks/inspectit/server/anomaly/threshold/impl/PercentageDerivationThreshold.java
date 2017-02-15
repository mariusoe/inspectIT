package rocks.inspectit.server.anomaly.threshold.impl;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.definition.threshold.PercentageDerivationThresholdDefinition;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;
import rocks.inspectit.server.anomaly.threshold.AbstractThreshold;
import rocks.inspectit.server.anomaly.threshold.UnsupportedThresholdTypeException;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public class PercentageDerivationThreshold extends AbstractThreshold<PercentageDerivationThresholdDefinition> {

	private double upperCriticalThreshold = Double.NaN;

	private double upperWarningThreshold = Double.NaN;

	private double lowerWarningThreshold = Double.NaN;

	private double lowerCriticalThreshold = Double.NaN;

	private DescriptiveStatistics statistics = new DescriptiveStatistics();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AnomalyProcessingContext context, long time) {
		double baseline = context.getBaseline().getBaseline();

		if (Double.isNaN(baseline)) {
			return;
		}

		// if (getDefinition().isExcludeCriticalData()) {
		// if (!context.isWarmedUp() || ((baseline >= getThreshold(context,
		// ThresholdType.LOWER_CRITICAL)) && (baseline <= getThreshold(context,
		// ThresholdType.UPPER_CRITICAL)))) {
		statistics.addValue(baseline);
		// }
		// }

		baseline = statistics.getMean();

		upperCriticalThreshold = baseline + (baseline * getDefinition().getPercentageDerivationCritical());
		upperWarningThreshold = baseline + (baseline * getDefinition().getPercentageDerivationWarning());
		lowerWarningThreshold = baseline - (baseline * getDefinition().getPercentageDerivationWarning());
		lowerCriticalThreshold = baseline - (baseline * getDefinition().getPercentageDerivationCritical());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean providesThreshold(rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType type) {
		switch (type) {
		case LOWER_CRITICAL:
		case LOWER_WARNING:
		case UPPER_CRITICAL:
		case UPPER_WARNING:
			return true;
		default:
			throw new UnsupportedThresholdTypeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getThreshold(AnomalyProcessingContext context, rocks.inspectit.server.anomaly.threshold.AbstractThreshold.ThresholdType type) {
		switch (type) {
		case UPPER_CRITICAL:
			return upperCriticalThreshold;
		case UPPER_WARNING:
			return upperWarningThreshold;
		case LOWER_WARNING:
			return lowerWarningThreshold;
		case LOWER_CRITICAL:
			return lowerCriticalThreshold;
		default:
			throw new UnsupportedThresholdTypeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
		statistics.setWindowSize(getDefinition().getWindowSize());
	}

}
