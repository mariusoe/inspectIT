package rocks.inspectit.shared.cs.anomaly.classification.classifier;

import rocks.inspectit.shared.cs.anomaly.classification.HealthState;
import rocks.inspectit.shared.cs.anomaly.classification.context.TubeContext;

/**
 * @author Marius Oehler
 *
 */
public class TubeClassifier extends AbstractClassifier<TubeContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HealthState classify(double value) {
		return null;
	}

}
