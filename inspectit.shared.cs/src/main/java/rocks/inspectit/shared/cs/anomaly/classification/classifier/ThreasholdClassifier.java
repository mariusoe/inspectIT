package rocks.inspectit.shared.cs.anomaly.classification.classifier;

import rocks.inspectit.shared.cs.anomaly.classification.HealthState;
import rocks.inspectit.shared.cs.anomaly.classification.context.ThresholdContext;

/**
 * @author Marius Oehler
 *
 */
public class ThreasholdClassifier extends AbstractClassifier<ThresholdContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthState classify(double value) {
		if (getContext().isUpperThreshold()) {
			if (value > getContext().getCriticalLevel()) {
				return HealthState.CRITICAL;
			} else if (value > getContext().getWarningLevel()) {
				return HealthState.WARNING;
			} else {
				return HealthState.NORMAL;
			}
		} else {
			if (value < getContext().getCriticalLevel()) {
				return HealthState.CRITICAL;
			} else if (value < getContext().getWarningLevel()) {
				return HealthState.WARNING;
			} else {
				return HealthState.NORMAL;
			}
		}
	}
}
